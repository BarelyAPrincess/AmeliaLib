/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import javax.annotation.Nullable;

import io.amelia.support.SupplierWithException;

public class StorageException
{
	public static Error error( String message )
	{
		return new Error( message );
	}

	public static Error error( String message, Throwable cause )
	{
		return new Error( message, cause );
	}

	public static Error error( Throwable cause )
	{
		return new Error( cause );
	}

	public static Ignorable ignorable( String message )
	{
		return new Ignorable( message );
	}

	public static Ignorable ignorable( String message, Throwable cause )
	{
		return new Ignorable( message, cause );
	}

	public static Ignorable ignorable( Throwable cause )
	{
		return new Ignorable( cause );
	}

	private StorageException()
	{
		// Static Access
	}

	public static class Error extends ApplicationException.Error
	{
		public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn ) throws Error
		{
			return tryCatch( fn, null );
		}

		public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn, @Nullable String detailMessage ) throws Error
		{
			try
			{
				return fn.get();
			}
			catch ( Exception e )
			{
				if ( detailMessage == null )
					throw new Error( e );
				else
					throw new Error( detailMessage, e );
			}
		}

		public Error()
		{
			super();
		}

		public Error( String message )
		{
			super( message );
		}

		public Error( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Error( Throwable cause )
		{
			super( cause );
		}
	}

	public static class Ignorable extends ApplicationException.Ignorable
	{
		public Ignorable()
		{
			super();
		}

		public Ignorable( String message )
		{
			super( message );
		}

		public Ignorable( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Ignorable( Throwable cause )
		{
			super( cause );
		}
	}
}
