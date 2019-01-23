package io.amelia.support;

import io.amelia.lang.ApplicationException;

public class LooperException extends ApplicationException.Error
{
	private LooperException()
	{
		// Container
	}

	public static class InvalidState extends ApplicationException.Runtime
	{
		public InvalidState()
		{
			super();
		}

		public InvalidState( String message )
		{
			super( message );
		}

		public InvalidState( String message, Throwable cause )
		{
			super( message, cause );
		}

		public InvalidState( Throwable cause )
		{
			super( cause );
		}
	}
}
