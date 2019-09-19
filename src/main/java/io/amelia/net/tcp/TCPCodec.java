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

import java.util.List;

import io.amelia.lang.PacketDecodeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

class TCPCodec extends ByteToMessageCodec<Packet>
{
	@Override
	protected void decode( ChannelHandlerContext ctx, ByteBuf in, List<Object> out ) throws Exception
	{
		try
		{
			out.add( Packet.decode( in ) );
		}
		catch ( PacketDecodeException e )
		{
			// Temp?
			throw e;
		}
	}

	@Override
	protected void encode( ChannelHandlerContext ctx, Packet msg, ByteBuf out ) throws Exception
	{
		out.writeInt( msg.getPacketId() );
		msg.writeToByteBuf( out );
	}
}
