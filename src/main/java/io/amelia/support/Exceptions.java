/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;

public class Exceptions
{
	public static String getStackTrace( @Nonnull Throwable throwable )
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		throwable.printStackTrace( new PrintStream( out ) );
		return Strs.encodeDefault( out.toByteArray() );
	}

	public static String getStackTrace()
	{
		return stackTraceToString( Thread.currentThread().getStackTrace() );
	}

	public static <Rtn> Rtn muteTryCatch( SupplierWithException<Rtn, Exception> supplier, Supplier<Rtn> altSupplier )
	{
		try
		{
			return supplier.get();
		}
		catch ( Exception e )
		{
			return altSupplier.get();
		}
	}

	public static String stackTraceToString( StackTraceElement[] stackTrace )
	{
		return Arrays.stream( stackTrace ).map( StackTraceElement::toString ).collect( Collectors.joining( "\n" ) );
	}

	public static <Rtn> Rtn tryCatch( SupplierWithException<Rtn, Exception> fn ) throws ApplicationException.Error
	{
		return tryCatch( fn, ApplicationException.Error::new );
	}

	public static void tryCatch( Callback<Exception> fn ) throws ApplicationException.Error
	{
		tryCatch( fn, ApplicationException.Error::new );
	}

	public static <Rtn, Exp extends Exception, Cause extends Exception> Rtn tryCatch( SupplierWithException<Rtn, Exp> fn, Function<Exp, Cause> mapper ) throws Cause
	{
		try
		{
			return fn.get();
		}
		catch ( Exception e )
		{
			try
			{
				throw mapper.apply( ( Exp ) e );
			}
			catch ( ClassCastException ee )
			{
				throw new RuntimeException( e );
			}
		}
	}

	public static <Exp extends Exception, Cause extends Exception> void tryCatch( Callback<Exp> fn, Function<Exp, Cause> mapper ) throws Cause
	{
		try
		{
			fn.call();
		}
		catch ( Exception e )
		{
			try
			{
				throw mapper.apply( ( Exp ) e );
			}
			catch ( ClassCastException ee )
			{
				throw new RuntimeException( e );
			}
		}
	}

	public static <Rtn, Cause extends Exception, Exp extends Exception> Rtn tryCatchOrNotPresent( SupplierWithException<Voluntary<Rtn>, Cause> fn, Function<Cause, Exp> mapper ) throws Exp
	{
		Voluntary<Rtn> result;

		try
		{
			result = fn.get();
		}
		catch ( Exception e )
		{
			if ( e instanceof RuntimeException )
				throw ( RuntimeException ) e;
			throw mapper.apply( ( Cause ) e );
		}

		if ( result == null || !result.isPresent() )
			throw mapper.apply( null );
		return result.get();
	}

	private Exceptions()
	{
		// Static Access
	}
}
