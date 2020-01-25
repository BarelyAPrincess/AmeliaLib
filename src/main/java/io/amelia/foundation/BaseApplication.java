/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerBase;
import io.amelia.data.parcel.ParcelInterface;
import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionRegistrar;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.StartupException;
import io.amelia.lang.StartupInterruptException;
import io.amelia.looper.LooperRouter;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Sys;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * When a {@link BaseApplication} is instigated, its main thread is dedicated to
 * running the Looper that takes care of managing the top-level application tasks and parcels.
 */
public abstract class BaseApplication implements VendorRegistrar, ExceptionRegistrar, ParcelInterface, ParcelReceiver
{
	public final Thread primaryThread = Thread.currentThread();
	private final OptionParser optionParser = new OptionParser();
	private Env env = null;
	private OptionSet optionSet = null;

	public BaseApplication()
	{
		if ( Kernel.isDevelopment() )
			Kernel.L.info( "%s%sApp is running in development mode.", EnumColor.DARK_RED, EnumColor.NEGATIVE );

		optionParser.acceptsAll( Arrays.asList( "?", "h", "help" ), "Show the help" );
		optionParser.acceptsAll( Arrays.asList( "v", "version" ), "Show the version" );

		optionParser.accepts( "env-file", "The env file" ).withRequiredArg().ofType( String.class ).defaultsTo( ".env" );
		optionParser.accepts( "env", "Override env values" ).withRequiredArg().ofType( String.class );

		for ( String pathKey : Kernel.getPathSlugs() )
			optionParser.accepts( "dir-" + pathKey, "Sets the " + pathKey + " directory path." ).withRequiredArg().ofType( String.class );
	}

	public void addBooleanArgument( String arg, String desc )
	{
		addBooleanArgument( arg, desc, true );
	}

	public void addBooleanArgument( String arg, String desc, boolean def )
	{
		optionParser.accepts( arg, desc ).withOptionalArg().ofType( Boolean.class ).defaultsTo( def );
	}

	public void addArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc );
	}

	public void addIntegerArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc ).withRequiredArg().ofType( Integer.class );
	}

	public void addStringArgument( String arg, String desc )
	{
		optionParser.accepts( arg, desc ).withRequiredArg().ofType( String.class );
	}

	public void checkOptionSet()
	{
		if ( optionSet == null )
			throw new ApplicationException.Runtime( ReportingLevel.E_ERROR, "Method parse( String[] ) was never called." );
	}

	void dispose()
	{
		LooperRouter.dispose();
	}

	@Override
	public void fatalError( ExceptionReport report, boolean crashOnError )
	{
		if ( !Objs.stackTraceAntiLoop( getClass(), "fatalError" ) )
			return;
		if ( report.hasErrored() && crashOnError )
			Foundation.setRunlevel( Runlevel.CRASHED );
	}

	public Env getEnv()
	{
		checkOptionSet();
		return env;
	}

	public Optional<Integer> getIntegerArgument( String arg )
	{
		return Optional.ofNullable( optionSet.valuesOf( arg ) ).filter( l -> l.size() > 0 ).map( l -> ( Integer ) l.get( 0 ) );
	}

	public OptionParser getOptionParser()
	{
		return optionParser;
	}

	public OptionSet getOptionSet()
	{
		checkOptionSet();
		return optionSet;
	}

	public Optional<String> getStringArgument( String arg )
	{
		return Optional.ofNullable( optionSet.valuesOf( arg ) ).filter( l -> l.size() > 0 ).map( l -> ( String ) l.get( 0 ) );
	}

	public Optional<List<String>> getStringListArgument( String arg )
	{
		return Optional.ofNullable( ( List<String> ) optionSet.valuesOf( arg ) );
	}

	public VendorMeta getVendorMeta()
	{
		return new VendorMeta( new HashMap<String, String>()
		{{
			put( VendorMeta.NAME, Kernel.getDevMeta().getProductName() );
			put( VendorMeta.DESCRIPTION, Kernel.getDevMeta().getProductDescription() );
			put( VendorMeta.AUTHORS, Kernel.getDevMeta().getDeveloperName() );
			put( VendorMeta.GITHUB_BASE_URL, Kernel.getDevMeta().getGitRepoUrl() );
			put( VendorMeta.VERSION, Kernel.getDevMeta().getVersionDescribe() );
		}} );
	}

	public boolean hasArgument( String arg )
	{
		return optionSet.hasArgument( arg );
	}

	public boolean isPrimaryThread()
	{
		return primaryThread == Thread.currentThread();
	}

	@Override
	public final boolean isRemote()
	{
		return false;
	}

	public abstract void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException.Error;

	/**
	 * Handles internal argument options and triggers, such as
	 *
	 * @throws StartupInterruptException
	 */
	public final void parse( String[] args ) throws StartupInterruptException
	{
		optionSet = optionParser.parse( args );

		if ( optionSet.has( "help" ) )
		{
			try
			{
				optionParser.printHelpOn( System.out );
			}
			catch ( IOException e )
			{
				throw new StartupException( e );
			}
			throw new StartupInterruptException();
		}

		if ( optionSet.has( "version" ) )
		{
			Kernel.L.info( Kernel.getDevMeta().getProductDescribed() );
			throw new StartupInterruptException();
		}

		try
		{
			/* Load env file -- Can be set with arg `--env-file=.env` */
			Path envFile = Paths.get( ( String ) optionSet.valueOf( "env-file" ) );
			env = new Env( envFile );

			/* Override defaults and env with command args */
			for ( OptionSpec<?> optionSpec : optionSet.specs() )
				for ( String optionKey : optionSpec.options() )
					if ( !Objs.isNull( optionSpec.value( optionSet ) ) )
					{
						if ( optionKey.startsWith( "dir-" ) )
						{
							Kernel.L.debug( "Setting path \"" + optionKey.substring( 4 ) + "\" to \"" + optionSpec.value( optionSet ) + "\"." );
							Kernel.setPath( optionKey.substring( 4 ), ( String ) optionSpec.value( optionSet ) );
						}
						else if ( !env.isValueSet( optionKey ) )
						{
							Kernel.L.debug( "Setting environment key \"" + optionKey + "\" to \"" + optionSpec.value( optionSet ) + "\"." );
							env.set( optionKey, optionSpec.value( optionSet ), false );
						}
					}

			// XXX Use Encrypt::hash as an alternative to Encrypt::uuid
			env.computeValue( "instance-id", Encrypt::uuid, true );

			Kernel.setAppPath( env.getString( "app-dir" ).map( Paths::get ).orElse( Sys.getAppPath().orElseThrow( () -> new StartupException( "The app path is not specified nor could we resolve it." ) ) ) );
			env.getStringsMap().filter( e -> e.getKey().endsWith( "-dir" ) ).forEach( e -> Kernel.setPath( e.getKey().substring( 0, e.getKey().length() - 4 ), Strs.split( e.getValue(), "/" ).toArray( String[]::new ) ) );

			ConfigRegistry.config.setEnvironmentVariables( env.map() );

			ConfigData envNode = ConfigRegistry.config.getChildOrCreate( "env" );
			for ( Map.Entry<String, Object> entry : env.map().entrySet() )
				envNode.setValue( entry.getKey().replace( '-', '_' ), entry.getValue() );
			envNode.addFlag( ContainerBase.Flags.READ_ONLY, ContainerBase.Flags.NO_SAVE );

			// ConfigRegistry should load here!
			Foundation.invokeHook( Foundation.class, Foundation.HOOK_ACTION_PARSE );
			if ( !ConfigRegistry.isLoaded() )
				throw new ApplicationException.Error( "ConfigRegistry did not initialize as expected. Is the `HOOK_ACTION_PARSE` implemented to load data into the ConfigRegistry?" );

			parse();
		}
		catch ( StartupException e )
		{
			throw e;
		}
		catch ( Exception e )
		{
			throw new StartupException( e );
		}
	}

	/**
	 * Called to perform some additional tasks during the parse phase of loading.
	 *
	 * @throws Exception
	 */
	protected abstract void parse() throws Exception;

	void quitSafely()
	{
		LooperRouter.quitSafely();
	}

	void quitUnsafe()
	{
		LooperRouter.quitUnsafely();
	}

	public void showBanner( Kernel.Logger logger )
	{
		logger.info( EnumColor.NEGATIVE + "" + EnumColor.GOLD + "Starting " + Kernel.getDevMeta().getProductName() + " version " + Kernel.getDevMeta().getVersionDescribe() );
		logger.info( EnumColor.NEGATIVE + "" + EnumColor.GOLD + Kernel.getDevMeta().getProductCopyright() );
	}

	public void throwStartupException( Exception e ) throws StartupException
	{
		throw new StartupException( "There was a problem starting the application", e );
	}

	@Nonnull
	public UUID uuid()
	{
		return env.getString( "uuid" ).map( UUID::fromString ).orElseGet( UUID::randomUUID );
	}
}
