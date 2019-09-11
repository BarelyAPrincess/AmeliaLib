/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logcompat;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import io.amelia.foundation.Kernel;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Streams;

/**
 * Builder for Loggers
 */
public class LogBuilder
{
	private static final Set<LibLogger> LOGGERS = new HashSet<>();
	private static final ConsoleHandler consoleHandler = new ConsoleHandler();
	private static final Logger rootLogger = Logger.getLogger( "" );

	static
	{
		for ( Handler h : rootLogger.getHandlers() )
			rootLogger.removeHandler( h );

		consoleHandler.setFormatter( new SimpleLogFormatter() );
		addHandler( consoleHandler );

		System.setOut( new PrintStream( new LoggerOutputStream( get( "SysOut" ), Level.INFO ), true ) );
		System.setErr( new PrintStream( new LoggerOutputStream( get( "SysErr" ), Level.SEVERE ), true ) );

		try
		{
			IO.forceCreateDirectory( Kernel.getPath( Kernel.PATH_LOGS ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	public static void addFileHandler( String filename, boolean useColor, int archiveLimit, Level level )
	{
		Path logPath = Kernel.getPath( Kernel.PATH_LOGS ).resolve( filename + ".log" );
		try
		{
			if ( Files.exists( logPath ) )
			{
				if ( archiveLimit > 0 )
					IO.gzFile( logPath, Kernel.getPath( Kernel.PATH_LOGS ).resolve( new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( new Date() ) + "-" + filename + ".log.gz" ) );
				Files.delete( logPath );
			}

			cleanupLogs( "-" + filename + ".log.gz", archiveLimit );

			FileHandler fileHandler = new FileHandler( logPath.toString() );
			fileHandler.setLevel( level );
			fileHandler.setFormatter( new DefaultLogFormatter( useColor ) );

			addHandler( fileHandler );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			get().severe( "Failed to log to \"" + logPath.toString() + "\" for reason \"" + e.getMessage() + "\".", e );
		}
	}

	public static void addHandler( Handler h )
	{
		rootLogger.addHandler( h );
	}

	private static void cleanupLogs( final String suffix, int limit ) throws IOException
	{
		Stream<Path> result = Files.list( Kernel.getPath( Kernel.PATH_LOGS ) ).filter( path -> path.toString().toLowerCase().endsWith( suffix.toLowerCase() ) );

		// Delete all logs, no archiving!
		if ( limit < 1 )
			Streams.forEachWithException( result, IO::deleteIfExists );
		else
			Streams.forEachWithException( result.sorted( new IO.PathComparatorByCreated() ).limit( limit ), IO::deleteIfExists );
	}

	public static LibLogger get()
	{
		return get( "" );
	}

	/**
	 * Gets an instance of Log for provided loggerId. If the logger does not exist one will be created.
	 *
	 * @param name The logger name
	 *
	 * @return ConsoleLogger An empty loggerId will return the System Logger.
	 */
	public static LibLogger get( @Nullable String name )
	{
		if ( Objs.isEmpty( name ) )
			name = "Core";

		for ( LibLogger log : LOGGERS )
			if ( log.getName().equals( name ) )
				return log;

		LibLogger log = new LibLogger( name );
		LOGGERS.add( log );
		return log;
	}

	public static LibLogger get( Class<?> logClass )
	{
		return get( logClass.getSimpleName() );
	}

	public static Logger getRootLogger()
	{
		return rootLogger;
	}

	public static void removeHandler( Handler h )
	{
		rootLogger.removeHandler( h );
	}

	public static void setConsoleFormatter( Formatter formatter )
	{
		consoleHandler.setFormatter( formatter );
	}

	/**
	 * Checks if the currently set Log Formatter, supports colored logs.
	 *
	 * @return true if it does
	 */
	public static boolean useColor()
	{
		return consoleHandler.getFormatter() instanceof DefaultLogFormatter && ( ( DefaultLogFormatter ) consoleHandler.getFormatter() ).useColor();
	}
}
