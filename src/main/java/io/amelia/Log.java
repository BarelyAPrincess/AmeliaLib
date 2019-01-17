/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia;

import java.util.logging.Level;

import io.amelia.foundation.Kernel;

public class Log
{
	private static Kernel.Logger L = Kernel.getLogger( Log.class );

	public static void debug( String message, Object... args )
	{
		Log.L.debug( message, args );
	}

	public static void fine( String message, Object... args )
	{
		Log.L.fine( message, args );
	}

	public static void finest( String message, Object... args )
	{
		Log.L.finest( message, args );
	}

	public static void info( String message, Object... args )
	{
		Log.L.info( message, args );
	}

	public static void log( Level level, String message, Object... args )
	{
		Log.L.log( level, message, args );
	}

	public static void log( Level level, Throwable cause )
	{
		Log.L.log( level, cause );
	}

	public static void severe( Throwable cause )
	{
		Log.L.severe( cause );
	}

	public static void severe( String message, Object... args )
	{
		Log.L.severe( message, args );
	}

	public static void severe( String message, Throwable cause, Object... args )
	{
		Log.L.severe( message, cause, args );
	}

	public static void warning( String message, Object... args )
	{
		Log.L.warning( message, args );
	}

	public static void warning( Throwable cause )
	{
		Log.L.warning( cause );
	}

	public static void warning( String message, Throwable cause, Object... args )
	{
		Log.L.warning( message, cause, args );
	}

	private Log()
	{
		// Static Access
	}
}
