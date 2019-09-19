/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.tcp;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class TCPInitializer extends ChannelInitializer<SocketChannel>
{
	public static final List<WeakReference<SocketChannel>> activeChannels = new CopyOnWriteArrayList<>();

	@Override
	protected void initChannel( SocketChannel ch ) throws Exception
	{
		ChannelPipeline p = ch.pipeline();

		p.addLast( "codec", new TCPCodec() );
		p.addLast( "handler", new TCPHandler() );

		activeChannels.add( new WeakReference<>( ch ) );
	}
}
