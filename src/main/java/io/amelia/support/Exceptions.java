package io.amelia.support;

import java.util.function.Function;

import io.amelia.lang.ApplicationException;

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
}
