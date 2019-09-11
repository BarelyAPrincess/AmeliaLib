package io.amelia.lang;

public class PacketEncodeException extends Exception
{
	public PacketEncodeException( String message )
	{
		super( message );
	}

	public PacketEncodeException( Exception exp )
	{
		super( exp );
	}
}
