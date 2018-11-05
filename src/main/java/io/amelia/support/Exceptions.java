/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.function.Function;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.netty.util.CharsetUtil;

public class Exceptions
{
	private Exceptions()
	{
		// Static Access
	}

	public static void tryCatch( Callback<Exception> fn ) throws ApplicationException.Error
	{
		tryCatch( fn, ApplicationException.Error::new );
	}

	public static <Cause extends Exception> void tryCatch( Callback<Exception> fn, Function<Exception, Cause> mapper ) throws Cause
	{
		try
		{
			fn.call();
		}
		catch ( Exception e )
		{
			throw mapper.apply( e );
		}
	}

	public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn ) throws ApplicationException.Error
	{
		return tryCatch( fn, ApplicationException.Error::new );
	}

	public static <Rtn, Cause extends Exception> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn, Function<Exception, Cause> mapper ) throws Cause
	{
		try
		{
			return fn.get();
		}
		catch ( Exception e )
		{
			throw mapper.apply( e );
		}
	}

	public static String getStackTrace( @Nonnull Throwable throwable )
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		throwable.printStackTrace( new PrintStream( out ) );
		return Strs.encodeDefault( out.toByteArray() );
	}
}
