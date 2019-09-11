package io.amelia.net.tcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.amelia.lang.PacketDecodeException;
import io.amelia.lang.PacketEncodeException;
import io.netty.buffer.ByteBuf;

public abstract class Packet
{
	public static final AtomicInteger NEXT_PACKET_ID = new AtomicInteger();
	public static final Map<Integer, Class<? extends Packet>> PACKETS = new ConcurrentHashMap<>();

	static
	{
		PACKETS.put( NEXT_PACKET_ID.getAndAdd( 1 ), PacketPingPong.class );
	}

	public static Packet decode( ByteBuf in ) throws PacketDecodeException
	{
		final int packetId = in.readInt();

		if ( packetId < 0 )
			throw new PacketDecodeException( "The provided packet ID was invalid. {packetId=" + packetId + "'" );

		Class<? extends Packet> packetClass = filter( packetId );

		if ( packetClass == null )
			throw new PacketDecodeException( "The provided packet ID did not match any known packets. {packetId=" + packetId + "'" );

		try
		{
			Packet packet = packetClass.newInstance();
			packet.readFromByteBuf( in );
			return packet;
		}
		catch ( InstantiationException e )
		{
			throw new PacketDecodeException( e );
		}
		catch ( IllegalAccessException e )
		{
			throw new PacketDecodeException( e );
		}
	}

	private static Class<? extends Packet> filter( int packetId )
	{
		for ( Map.Entry<Integer, Class<? extends Packet>> entry : PACKETS.entrySet() )
			if ( entry.getKey() == packetId )
				return entry.getValue();
		return null;
	}

	public int getPacketId()
	{
		for ( Map.Entry<Integer, Class<? extends Packet>> entry : PACKETS.entrySet() )
			if ( entry.getValue() == getClass() )
				return entry.getKey();
		return -1;
	}

	abstract void readFromByteBuf( ByteBuf in ) throws PacketDecodeException;

	abstract void writeToByteBuf( ByteBuf out ) throws PacketEncodeException;
}
