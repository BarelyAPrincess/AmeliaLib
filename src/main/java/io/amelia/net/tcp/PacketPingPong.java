package io.amelia.net.tcp;

import io.netty.buffer.ByteBuf;

public class PacketPingPong extends Packet
{
	public long epochSent = -1; // -1 Never Sent
	public long epochReceived = -1; // -1 Never Received
	public String serverIdentifier = null;

	public PacketPingPong()
	{

	}

	public PacketPingPong( String serverIdentifier )
	{
		this.serverIdentifier = serverIdentifier;
	}

	@Override
	void readFromByteBuf( ByteBuf in ) throws PacketDecodeException
	{
		epochReceived = System.currentTimeMillis(); // This is when we received the packet
		epochSent = in.readLong(); // This is when the peer sent it
	}

	@Override
	void writeToByteBuf( ByteBuf out ) throws PacketEncodeException
	{
		NetUtilities.writeStringToByteBuf( serverIdentifier, out );
		out.writeLong( System.currentTimeMillis() );
	}

	public long getEpochSent()
	{
		return epochSent;
	}

	public long getEpochReceived()
	{
		return epochReceived;
	}
}
