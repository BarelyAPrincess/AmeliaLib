/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
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
