package io.amelia.net.tcp;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToMessageCodec;

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
