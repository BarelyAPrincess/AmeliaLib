/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionRegistrar;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.UncaughtException;
import io.amelia.support.Arrs;
import io.amelia.support.DateAndTime;
import io.amelia.support.Exceptions;
import io.amelia.support.IO;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Strs;

public class Kernel
{
	public static final String PATH_APP = "__app";
	public static final String PATH_CACHE = "__cache";
	public static final String PATH_LOGS = "__logs";
	public static final String PATH_LIBS = "__libs";
	public static final String PATH_CONFIG = "__config";
	public static final String PATH_PLUGINS = "__plugins";
	public static final String PATH_UPDATES = "__updates";
	public static final String PATH_STORAGE = "__storage";

	public static final Logger L = getLogger( Kernel.class );
	/**
	 * An {@link Executor} that can be used to execute tasks in parallel.
	 */
	static final Executor EXECUTOR_PARALLEL;
	/**
	 * An {@link Executor} that executes tasks one at a time in serial order.
	 */
	static final Executor EXECUTOR_SERIAL;
	static final int KEEP_ALIVE_SECONDS = 30;
	private static final Map<String, List<String>> APP_PATHS = new ConcurrentHashMap<>();
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	static final int THREAD_ROOL_SIZE_MAXIMUM = CPU_COUNT * 2 + 1;
	// We want at least 2 threads and at most 4 threads in the core pool,
	// preferring to have 1 less than the CPU count to avoid saturating
	// the CPU with background work
	static final int THREAD_POOL_SIZE_CORE = Math.max( 4, Math.min( CPU_COUNT - 1, 1 ) );
	public static long startTime = System.currentTimeMillis();
	private static Path appPath;
	private static DevMetaProvider devMeta;
	private static ExceptionRegistrar exceptionRegistrar = null;
	static final ThreadFactory threadFactory = new ThreadFactory()
	{
		private final AtomicInteger mCount = new AtomicInteger( 1 );

		@Override
		public Thread newThread( @Nonnull Runnable runnable )
		{
			Thread newThread = new Thread( runnable, "HPS Thread #" + String.format( "%d04", mCount.getAndIncrement() ) );
			newThread.setUncaughtExceptionHandler( ( thread, exp ) -> ExceptionReport.handleSingleException( new UncaughtException( "Uncaught exception thrown on thread " + thread.getName(), exp ) ) );

			return newThread;
		}
	};

	private static LogHandler log = new BuiltinLogHandler();
	private static KernelHandler kernelHandler = new BuiltinKernelHandler();

	static
	{
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor( THREAD_POOL_SIZE_CORE, THREAD_ROOL_SIZE_MAXIMUM, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), threadFactory );
		threadPoolExecutor.allowCoreThreadTimeOut( true );
		EXECUTOR_PARALLEL = threadPoolExecutor;

		EXECUTOR_SERIAL = new Executor()
		{
			final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
			Runnable mActive;

			public synchronized void execute( final Runnable r )
			{
				mTasks.offer( () -> {
					try
					{
						r.run();
					}
					finally
					{
						scheduleNext();
					}
				} );
				if ( mActive == null )
				{
					scheduleNext();
				}
			}

			protected synchronized void scheduleNext()
			{
				if ( ( mActive = mTasks.poll() ) != null )
				{
					EXECUTOR_PARALLEL.execute( mActive );
				}
			}
		};

		setPath( PATH_CACHE, PATH_STORAGE, "cache" );
		setPath( PATH_LOGS, PATH_STORAGE, "logs" );
		setPath( PATH_LIBS, PATH_APP, "libs" );
		setPath( PATH_CONFIG, PATH_APP, "config" );
		setPath( PATH_PLUGINS, PATH_APP, "plugins" );
		setPath( PATH_UPDATES, PATH_APP, "updates" );
		setPath( PATH_STORAGE, PATH_APP, "storage" );
	}

	public static DevMetaProvider getDevMeta()
	{
		if ( devMeta == null )
			devMeta = new NoDevMeta();
		return devMeta;
	}

	public static void setDevMeta( DevMetaProvider devMeta )
	{
		if ( Kernel.devMeta != null && !( Kernel.devMeta instanceof NoDevMeta ) )
			throw new IllegalStateException( "Improper Implementation: DevMeta has already been set." );
		Kernel.devMeta = devMeta;
	}

	public static KernelHandler getKernelHandler()
	{
		return kernelHandler;
	}

	public static void setKernelHandler( KernelHandler kernelHandler )
	{
		if ( Kernel.kernelHandler != null && !( Kernel.kernelHandler instanceof BuiltinKernelHandler ) )
			throw new IllegalStateException( "Improper Implementation: KernelHandler has already been set." );
		Kernel.kernelHandler = kernelHandler;
	}

	public static ExceptionRegistrar getExceptionRegistrar()
	{
		return exceptionRegistrar;
	}

	public static void setExceptionRegistrar( ExceptionRegistrar exceptionRegistrar )
	{
		Kernel.exceptionRegistrar = exceptionRegistrar;
	}

	public static Executor getExecutorParallel()
	{
		return EXECUTOR_PARALLEL;
	}

	public static Executor getExecutorSerial()
	{
		return EXECUTOR_SERIAL;
	}

	public static Logger getLogger( Class<?> source )
	{
		return new Logger( source );
	}

	public static Path getPath( @Nonnull String slug )
	{
		return getPath( new String[] {slug} );
	}

	public static Path getPath( @Nonnull String slug, boolean createPath )
	{
		return getPath( new String[] {slug}, createPath );
	}

	public static Path getPath( @Nonnull String[] slugs )
	{
		return getPath( slugs, false );
	}

	/**
	 * Builds a directory based on the provided slugs.
	 * Key based paths MUST start with double underscores.
	 * <p>
	 * The options are as follows:
	 * __app
	 * __webroot
	 * __config
	 * __plugins
	 * __updates
	 * __database
	 * __storage
	 * __sessions
	 * __cache
	 * __logs
	 * <p>
	 * Slugs not starting with double underscores will be treated as either a relative
	 * or absolute path depending on if it starts with a single forward slash.
	 * <p>
	 * Examples:
	 * __app -> /usr/share/honeypot
	 * __sessions -> /usr/share/honeypot/storage/sessions
	 * relative -> /usr/share/honeypot/relative
	 * /absolute -> /absolute
	 * <p>
	 *
	 * @param slugs      The path slugs
	 * @param createPath Should we try creating the directory if it doesn't exist?
	 *
	 * @return The absolute File
	 *
	 * @throws ApplicationException.Ignorable
	 */
	public static Path getPath( @Nonnull String[] slugs, boolean createPath )
	{
		Objs.notNull( slugs );

		if ( slugs.length == 0 )
			return getPath();

		if ( slugs[0].startsWith( "__" ) )
		{
			String key = slugs[0].substring( 2 );
			if ( key.equals( "foundation" ) )
				slugs[0] = getPath().toString();
			else if ( Kernel.APP_PATHS.containsKey( key ) )
				slugs = Stream.concat( Kernel.APP_PATHS.get( key ).stream(), Arrays.stream( slugs ).skip( 1 ) ).toArray( String[]::new );
			else
				throw ApplicationException.ignorable( "Path " + key + " is not set!" );

			return getPath( slugs, createPath );
		}
		else if ( !slugs[0].startsWith( "/" ) )
			slugs = Arrs.prepend( slugs, getPath().toString() );

		Path path = IO.buildPath( true, slugs );

		if ( createPath )
		{
			try
			{
				IO.forceCreateDirectory( path );
			}
			catch ( IOException e )
			{
				throw ApplicationException.ignorable( "The foundation path \"" + path.toString() + "\" does not exist and we couldn't create it.", e );
			}
		}

		return path;
	}

	public static Path getPath()
	{
		Objs.notEmpty( appPath, "The foundation path has not been set." );
		return appPath;
	}

	public static List<String> getPathSlugs()
	{
		return new ArrayList<>( APP_PATHS.keySet() );
	}

	/**
	 * Indicates if we are running a development build of the server
	 *
	 * @return True is we are running in development mode
	 */
	public static boolean isDevelopment()
	{
		return devMeta != null && "0".equals( devMeta.getBuildNumber() ) || ConfigRegistry.config.getValue( ConfigRegistry.ConfigKeys.DEVELOPMENT_MODE );
	}

	protected static void setAppPath( @Nonnull Path appPath )
	{
		Objs.notNull( appPath );
		Kernel.appPath = appPath;
	}

	public static void setLogHandler( @Nonnull LogHandler log )
	{
		Kernel.log = log;
	}

	public static void setPath( @Nonnull String pathKey, @Nonnull String... paths )
	{
		Objs.notEmpty( pathKey );
		if ( pathKey.startsWith( "__" ) )
			pathKey = pathKey.substring( 2 );

		final String key = pathKey.toLowerCase();

		if ( "foundation".equals( key ) )
			throw new IllegalArgumentException( "App path is set using the setAppPath() method." );
		if ( !Paths.get( paths[0] ).isAbsolute() && !paths[0].startsWith( "__" ) )
			throw new IllegalArgumentException( "App paths must be absolute or reference another foundation path, i.e., __app. Paths: [" + Strs.join( paths ) + "]" );
		Kernel.APP_PATHS.put( key, Lists.newArrayList( paths ) );
	}

	public static long uptime()
	{
		return System.currentTimeMillis() - startTime;
	}

	public static String uptimeDescribe()
	{
		return DateAndTime.formatDuration( System.currentTimeMillis() - startTime );
	}

	private Kernel()
	{
		// Static Access
	}

	private static class BuiltinKernelHandler implements KernelHandler
	{
		@Override
		public boolean isPrimaryThread()
		{
			// TODO
			return false;
		}
	}

	private static class BuiltinLogHandler implements LogHandler
	{
		@Override
		public void log( Level level, Class<?> source, String message, Object... args )
		{
			message = "[" + level.getName() + "] " + ( args.length > 0 ? String.format( message, args ) : message );

			if ( level.intValue() <= Level.INFO.intValue() )
				System.out.println( message );
			else
				System.err.println( message );
		}

		@Override
		public void log( Level level, Class<?> source, Throwable cause )
		{
			log( level, source, Exceptions.getStackTrace( cause ) );
		}
	}

	public static class Logger
	{
		private Class<?> source;

		public Logger( Class<?> source )
		{
			this.source = source;
		}

		public void debug( String message, Object... args )
		{
			log.debug( source, message, args );
		}

		public void fine( String message, Object... args )
		{
			log.fine( source, message, args );
		}

		public void finest( String message, Object... args )
		{
			log.finest( source, message, args );
		}

		public void info( String message, Object... args )
		{
			log.info( source, message, args );
		}

		public void severe( Throwable cause )
		{
			log.severe( source, cause );
		}

		public void severe( String message, Object... args )
		{
			log.severe( source, message, args );
		}

		public void severe( String message, Throwable cause, Object... args )
		{
			log.severe( source, message, cause, args );
		}

		public void warning( Throwable cause )
		{
			log.warning( source, cause );
		}

		public void warning( String message, Throwable cause, Object... args )
		{
			log.warning( source, message, cause, args );
		}

		public void warning( String message, Object... args )
		{
			log.warning( source, message, args );
		}
	}

	private static class NoDevMeta implements DevMetaProvider
	{
		@Override
		public String getProperty( String key )
		{
			switch ( key )
			{
				case KEY_PRODUCT_NAME:
					return "(Unset Name)";
				case KEY_PRODUCT_COPYRIGHT:
					return "(Unset Copyright)";
				case KEY_VERSION_MAJOR:
					return "0";
				case KEY_VERSION_MINOR:
					return "0";
				case KEY_VERSION_REVISION:
					return "0";
				case KEY_BUILD_NUMBER:
					return "0";
				case KEY_CODENAME:
					return "(Unset Codename)";
				case KEY_GIT_REPO:
					return "(Unset Repo)";
				case KEY_GIT_REPO_URL:
					return "(Unset Repo URL)";
				case KEY_GIT_BRANCH:
					return "master";
				case KEY_DEV_NAME:
					return "(Unset Dev Name)";
				case KEY_DEV_EMAIL:
					return "(Unset Dev Email)";
				case KEY_DEV_LICENSE:
					return "(Unset License)";
			}
			return null;
		}
	}
}
