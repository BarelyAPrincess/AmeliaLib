package io.amelia.net.tcp;

class PacketEncodeException extends Exception
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
