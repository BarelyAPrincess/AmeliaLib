/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.wip.udp;

import com.google.common.collect.Queues;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import io.amelia.net.wip.ClusterRole;
import io.amelia.net.wip.packets.RawPacket;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.Objs;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.GenericFutureListener;

public class UDPHandler extends SimpleChannelInboundHandler<RawPacket>
{
	private final Queue<InboundHandlerTuplePacketListener> outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
	private final Queue<RawPacket> receivedPacketsQueue = Queues.newConcurrentLinkedQueue();
	private Channel channel;
	private ClusterRole clusterRole;
	private boolean clusterRoleChanged = false;
	private InetSocketAddress inetSocketAddress;
	private UDPPacketHandler packetHandler;

	public UDPHandler( InetSocketAddress inetSocketAddress )
	{
		this.inetSocketAddress = inetSocketAddress;
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelActive( ctx );

		channel = ctx.channel();
		setClusterRole( ClusterRole.MONITOR );
	}

	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception
	{
		super.channelInactive( ctx );
		closeChannel( "End of Stream" );
	}

	public void closeChannel( String reason )// String reason )
	{
		if ( channel.isOpen() )
		{
			channel.close();
			// terminationReason = reason;
		}
	}

	private void dispatchPacket( final RawPacket packet, final GenericFutureListener[] listeners )
	{
		if ( !packet.checkUDPState( clusterRole ) )
			throw new ApplicationException.Runtime( ReportingLevel.E_ERROR, "Cluster role mismatch for packet " + packet.getClass().getSimpleName() + ", current role " + clusterRole.name() );

		Runnable func = () -> channel.writeAndFlush( packet ).addListeners( listeners ).addListener( ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE );

		if ( channel.eventLoop().inEventLoop() )
			func.run();
		else
			channel.eventLoop().execute( func );
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		super.exceptionCaught( ctx, cause );
		closeChannel( cause instanceof TimeoutException ? "Stream Timeout" : "Stream Exception: " + cause.getMessage() );
	}

	private void flushOutboundQueue()
	{
		if ( channel != null && channel.isOpen() )
			while ( !this.outboundPacketsQueue.isEmpty() )
			{
				InboundHandlerTuplePacketListener packetListener = ( InboundHandlerTuplePacketListener ) outboundPacketsQueue.poll();
				dispatchPacket( packetListener.packet, packetListener.listeners );
			}
	}

	public InetSocketAddress getInetSocketAddress()
	{
		return inetSocketAddress;
	}

	public UDPPacketHandler getPacketHandler()
	{
		return packetHandler;
	}

	public void setPacketHandler( @Nonnull UDPPacketHandler packetHandler )
	{
		Objs.notNull( packetHandler );
		Kernel.L.fine( "Set packet handler of %s to %s", this, packetHandler );
		this.packetHandler = packetHandler;
	}

	@Override
	protected void channelRead0( ChannelHandlerContext ctx, RawPacket packet ) throws Exception
	{
		if ( channel.isOpen() )
		{
			if ( packet.hasPriority() )
				packet.processPacket( packetHandler );
			else
				receivedPacketsQueue.add( packet );
		}
	}

	public void processReceivedPacket()
	{
		flushOutboundQueue();
		if ( clusterRoleChanged )
		{
			packetHandler.onClusterRoleTransition( clusterRole );
			this.packetHandler = clusterRole.newPacketHandler();
		}

		if ( packetHandler != null )
		{
			for ( int i = 1000; !receivedPacketsQueue.isEmpty() && i >= 0; --i )
				receivedPacketsQueue.poll().processPacket( packetHandler );

			packetHandler.onNetworkTick();
		}

		channel.flush();
	}

	public void scheduleOutboundPacket( RawPacket packet, GenericFutureListener... listener )
	{
		if ( channel != null && channel.isOpen() )
		{
			flushOutboundQueue();
			dispatchPacket( packet, listener );
		}
		else
			outboundPacketsQueue.add( new InboundHandlerTuplePacketListener( packet, listener ) );
	}

	public void setClusterRole( ClusterRole clusterRole )
	{
		if ( this.clusterRole != clusterRole )
		{
			this.clusterRole = clusterRole;
			clusterRoleChanged = true;
			channel.config().setAutoRead( true );
		}
	}

	static class InboundHandlerTuplePacketListener
	{
		private final GenericFutureListener[] listeners;
		private final RawPacket packet;

		public InboundHandlerTuplePacketListener( RawPacket packet, GenericFutureListener... listeners )
		{
			this.packet = packet;
			this.listeners = listeners;
		}
	}
}
