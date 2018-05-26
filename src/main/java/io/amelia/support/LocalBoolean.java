package io.amelia.support;

public class LocalBoolean extends LocalObject<Boolean>
{
	public LocalBoolean( boolean def )
	{
		super( () -> def );
	}

	public LocalBoolean()
	{
		super();
	}
}
