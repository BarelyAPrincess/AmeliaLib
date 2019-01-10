/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import javax.annotation.Nullable;

import io.amelia.support.SupplierWithException;

public class NetworkException
{
	private NetworkException()
	{
		// Static Access
	}

	public static class Error extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;

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

	public static class Ignorable extends ApplicationException.Runtime
	{
		private static final long serialVersionUID = 5522301956671473324L;

		public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn ) throws Ignorable
		{
			return tryCatch( fn, null );
		}

		public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn, @Nullable String detailMessage ) throws Ignorable
		{
			try
			{
				return fn.get();
			}
			catch ( Exception e )
			{
				if ( detailMessage == null )
					throw new Ignorable( e );
				else
					throw new Ignorable( detailMessage, e );
			}
		}

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

	public static class Notice extends ApplicationException.Error
	{
		private static final long serialVersionUID = 5522301956671473324L;

		public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn ) throws Notice
		{
			return tryCatch( fn, null );
		}

		public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn, @Nullable String detailMessage ) throws Notice
		{
			try
			{
				return fn.get();
			}
			catch ( Exception e )
			{
				if ( detailMessage == null )
					throw new Notice( e );
				else
					throw new Notice( detailMessage, e );
			}
		}

		public Notice()
		{
			super();
		}

		public Notice( String message )
		{
			super( message );
		}

		public Notice( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Notice( Throwable cause )
		{
			super( cause );
		}
	}

	public static class Runtime extends ApplicationException.Runtime
	{
		private static final long serialVersionUID = 5522301956671473324L;

		public Runtime()
		{
			super();
		}

		public Runtime( String message )
		{
			super( message );
		}

		public Runtime( String message, Throwable cause )
		{
			super( message, cause );
		}

		public Runtime( Throwable cause )
		{
			super( cause );
		}
	}
}
