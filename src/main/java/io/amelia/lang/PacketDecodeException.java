package io.amelia.lang;

public class PacketDecodeException extends Exception
{
	public PacketDecodeException( String message )
	{
		super( message );
	}

	public PacketDecodeException( Exception exp )
	{
		super( exp );
	}
}
