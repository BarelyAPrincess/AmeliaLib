/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.plugins;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.events.EventHandlers;
import io.amelia.events.Events;
import io.amelia.events.RunlevelEvent;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.Runlevel;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginMetaException;
import io.amelia.lang.PluginNotFoundException;
import io.amelia.plugins.loader.PluginClassLoader;
import io.amelia.plugins.loader.PluginLoader;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Priority;
import io.amelia.support.VoluntaryWithCause;
import io.amelia.tasks.Tasks;

public class HoneyPlugins implements Plugins<Plugin>
{

	private final Map<Pattern, PluginLoader> fileAssociations = new HashMap<>();
	private final ReentrantLock lock = new ReentrantLock();
	private final Map<String, Plugin> lookupNames = new HashMap<>();
	private final List<Plugin> plugins = new ArrayList<>();
	private Set<String> loadedPlugins = new HashSet<>();

	public HoneyPlugins()
	{
		// Loads plugins in order as PluginManager receives the notices from the Events
		Events.getInstance().listen( Foundation.getApplication(), Priority.NORMAL, RunlevelEvent.class, event -> {
			Runlevel level = event.getRunLevel();
			getPlugins().filter( plugin -> !plugin.isEnabled() && plugin.getMeta().getLoad() == level ).forEach( this::enablePlugin );
		} );
	}

	private void checkUpdate( @Nonnull Path path )
	{
		try
		{
			Path updatePath = Kernel.getPath( Kernel.PATH_UPDATES );
			IO.forceCreateDirectory( updatePath );
			Path updateFile = path.getFileName().resolve( updatePath );
			if ( Files.isRegularFile( updateFile ) && IO.copy( updateFile, path ) )
				Files.delete( updateFile );
		}
		catch ( IOException e )
		{
			// Do Nothing
		}
	}

	public void clearPlugins()
	{
		lock.lock();
		try
		{
			disablePlugins();
			plugins.clear();
			lookupNames.clear();
			EventHandlers.unregisterAll();
			fileAssociations.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void disablePlugin( final Plugin plugin )
	{
		if ( plugin.isEnabled() )
		{
			try
			{
				plugin.getPluginLoader().disablePlugin( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				Tasks.cancelTasks( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while cancelling tasks for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				// Loader.getServicesManager().unregisterAll( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while unregistering services for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				EventHandlers.unregisterAll( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while unregistering events for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}

			try
			{
				// TODO Implement here the unregistering of plugin loopers

				// Loader.getMessenger().unregisterIncomingPluginChannel( plugin );
				// Loader.getMessenger().unregisterOutgoingPluginChannel( plugin );
			}
			catch ( NoClassDefFoundError ex )
			{
				// Ignore
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while unregistering plugin channels for " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}
		}
	}

	public void disablePlugins()
	{
		getPlugins().forEach( this::disablePlugin );
	}

	@Override
	public void enablePlugin( final Plugin plugin )
	{
		if ( !plugin.isEnabled() )
			try
			{
				plugin.getPluginLoader().enablePlugin( plugin );
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, "Error occurred (in the plugin loader) while enabling " + plugin.getMeta().getDisplayName() + " (Check for Version Mismatch)", ex );
			}
	}

	@Override
	public VoluntaryWithCause<Plugin, PluginNotFoundException> getPluginByClass( @Nonnull Class<?> pluginClass )
	{
		if ( pluginClass.getClassLoader() instanceof PluginClassLoader )
			return VoluntaryWithCause.ofWithCause( getPlugins().filter( plugin -> plugin == ( ( PluginClassLoader ) pluginClass.getClassLoader() ).getPlugin() ).findAny() );
		return VoluntaryWithCause.emptyWithCause();
	}

	@Override
	public VoluntaryWithCause<Plugin, PluginNotFoundException> getPluginByClassname( String className )
	{
		return VoluntaryWithCause.ofWithCause( getPlugins().filter( plugin -> plugin.getClass().getName().startsWith( className ) ).findAny() );
	}

	@Override
	public VoluntaryWithCause<Plugin, PluginNotFoundException> getPluginByName( String pluginName )
	{
		return VoluntaryWithCause.ofWithCause( getPlugins().filter( plugin -> plugin.getClass().getCanonicalName().equals( pluginName ) || plugin.getName().equalsIgnoreCase( pluginName ) ).findAny() );
	}

	@Override
	public Plugin getPluginByNameWithException( String pluginName ) throws PluginNotFoundException
	{
		return getPluginByName( pluginName ).orElseThrow( () -> new PluginNotFoundException( "We could not find a plugin by the name '" + pluginName + "', maybe it's not loaded." ) );
	}

	@Override
	public synchronized Stream<Plugin> getPlugins()
	{
		return plugins.stream();
	}

	/**
	 * Loads the plugin in the specified file
	 * <p>
	 * File must be valid according to the current enabled Plugin interfaces
	 * <p>
	 *
	 * @param pluginPath File containing the plugin to load
	 *
	 * @return The Plugin instance
	 *
	 * @throws PluginInvalidException           Thrown when the specified file is not a valid plugin
	 * @throws PluginDependencyUnknownException If a required dependency could not be found
	 */
	@Override
	public synchronized Plugin loadPlugin( @Nonnull Path pluginPath ) throws PluginInvalidException, PluginDependencyUnknownException
	{
		checkUpdate( pluginPath );

		Set<Pattern> filters = fileAssociations.keySet();
		Plugin result = null;

		for ( Pattern filter : filters )
		{
			String name = pluginPath.getFileName().toString();
			Matcher match = filter.matcher( name );

			if ( match.find() )
			{
				PluginLoader<Plugin> loader = fileAssociations.get( filter );
				result = loader.loadPlugin( pluginPath );
			}
		}

		if ( result != null )
		{
			plugins.add( result );
			lookupNames.put( result.getMeta().getName(), result );
		}

		return result;
	}

	protected void loadPlugin( Plugin plugin )
	{
		try
		{
			enablePlugin( plugin );
		}
		catch ( Throwable ex )
		{
			L.log( Level.SEVERE, ex.getMessage() + " loading " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
		}
	}

	@Override
	public void loadPlugins() throws IOException
	{
		registerInterface( PluginLoader.class );
		// registerInterface( GroovyPluginLoader.class );

		Path pluginPath = Kernel.getPath( Kernel.PATH_PLUGINS );
		IO.forceCreateDirectory( pluginPath );

		loadPlugins( pluginPath ).forEach( plugin -> {
			try
			{
				String message = String.format( "Loading %s", plugin.getMeta().getDisplayName() );
				L.info( message );
				plugin.onLoad();
			}
			catch ( Throwable ex )
			{
				L.log( Level.SEVERE, ex.getMessage() + " initializing " + plugin.getMeta().getDisplayName() + " (Is it up to date?)", ex );
			}
		} );
	}

	/**
	 * Loads the plugins contained within the specified directory
	 * <p>
	 *
	 * @param pluginPath Directory to check for plugins
	 *
	 * @return A list of all plugins loaded
	 */
	@Override
	public Stream<Plugin> loadPlugins( @Nonnull Path pluginPath ) throws IOException
	{
		Objs.notFalse( Files.isDirectory( pluginPath ), "Directory must be a directory" );

		List<Plugin> result = new ArrayList<>();
		Set<Pattern> filters = fileAssociations.keySet();

		Map<String, Path> plugins = new HashMap<>();
		Map<String, List<MavenReference>> libraries = new HashMap<>();
		Map<String, List<String>> dependencies = new HashMap<>();
		Map<String, List<String>> softDependencies = new HashMap<>();

		// This is where it figures out all possible plugins
		Files.list( pluginPath ).forEach( pluginFile -> {
			PluginLoader loader = null;
			for ( Pattern filter : filters )
			{
				Matcher match = filter.matcher( pluginFile.getFileName().toString() );
				if ( match.find() )
					loader = fileAssociations.get( filter );
			}

			if ( loader == null )
				return;

			PluginMeta description = null;
			try
			{
				description = loader.getPluginMeta( pluginFile );
			}
			catch ( PluginMetaException ex )
			{
				L.log( Level.SEVERE, "Could not load '" + pluginFile.toString() + "' in folder '" + pluginPath.toString() + "'", ex );
				return;
			}

			plugins.put( description.getName(), pluginFile );

			Collection<String> softDependencySet = description.getSoftDepend();
			if ( softDependencySet != null )
				if ( softDependencies.containsKey( description.getName() ) )
					// Duplicates do not matter, they will be removed together if applicable
					softDependencies.get( description.getName() ).addAll( softDependencySet );
				else
					softDependencies.put( description.getName(), new LinkedList<>( softDependencySet ) );

			Collection<MavenReference> librariesSet = description.getLibraries();
			if ( librariesSet != null )
				libraries.put( description.getName(), new LinkedList<>( librariesSet ) );

			Collection<String> dependencySet = description.getDepend();
			if ( dependencySet != null )
				dependencies.put( description.getName(), new LinkedList<>( dependencySet ) );

			Collection<String> loadBeforeSet = description.getLoadBefore();
			if ( loadBeforeSet != null )
				for ( String loadBeforeTarget : loadBeforeSet )
					if ( softDependencies.containsKey( loadBeforeTarget ) )
						softDependencies.get( loadBeforeTarget ).add( description.getName() );
					else
					{
						// softDependencies is never iterated, so 'ghost' plugins aren't an issue
						List<String> shortSoftDependency = new LinkedList<>();
						shortSoftDependency.add( description.getName() );
						softDependencies.put( loadBeforeTarget, shortSoftDependency );
					}
		} );

		while ( !plugins.isEmpty() )
		{
			boolean missingDependency = true;
			Iterator<String> pluginIterator = plugins.keySet().iterator();

			while ( pluginIterator.hasNext() )
			{
				String plugin = pluginIterator.next();

				if ( libraries.containsKey( plugin ) )
				{
					Iterator<MavenReference> librariesIterator = libraries.get( plugin ).iterator();

					while ( librariesIterator.hasNext() )
					{
						MavenReference library = librariesIterator.next();

						if ( Libraries.isLoaded( library ) )
							librariesIterator.remove();
						else if ( !Libraries.loadLibrary( library ) )
						{
							missingDependency = false;
							Path pluginFile = plugins.get( plugin );
							pluginIterator.remove();
							libraries.remove( plugin );
							softDependencies.remove( plugin );
							dependencies.remove( plugin );

							L.severe( "Could not load '" + pluginFile.toString() + "' in folder '" + pluginPath.toString() + "' due to issue with library '" + library + "'." );
							break;
						}
					}
				}

				if ( dependencies.containsKey( plugin ) )
				{
					Iterator<String> dependencyIterator = dependencies.get( plugin ).iterator();

					while ( dependencyIterator.hasNext() )
					{
						String dependency = dependencyIterator.next();

						// Dependency loaded
						if ( loadedPlugins.contains( dependency ) )
							dependencyIterator.remove();
						else if ( !plugins.containsKey( dependency ) )
						{
							missingDependency = false;
							Path pluginFile = plugins.get( plugin );
							pluginIterator.remove();
							libraries.remove( plugin );
							softDependencies.remove( plugin );
							dependencies.remove( plugin );

							L.severe( "Could not load '" + pluginFile.toString() + "' in folder '" + pluginPath.toString() + "'", new PluginDependencyUnknownException( dependency ) );
							break;
						}
					}

					if ( dependencies.containsKey( plugin ) && dependencies.get( plugin ).isEmpty() )
						dependencies.remove( plugin );
				}
				if ( softDependencies.containsKey( plugin ) )
				{
					// Soft depend is no longer around
					softDependencies.get( plugin ).removeIf( softDependency -> !plugins.containsKey( softDependency ) );

					if ( softDependencies.get( plugin ).isEmpty() )
						softDependencies.remove( plugin );
				}
				if ( !( dependencies.containsKey( plugin ) || softDependencies.containsKey( plugin ) ) && plugins.containsKey( plugin ) )
				{
					// We're clear to load, no more soft or hard dependencies left
					Path pluginFile = plugins.get( plugin );
					pluginIterator.remove();
					missingDependency = false;

					try
					{
						result.add( loadPlugin( pluginFile ) );
						loadedPlugins.add( plugin );
						continue;
					}
					catch ( PluginInvalidException ex )
					{
						L.severe( "Could not load '" + pluginFile.toString() + "' in folder '" + pluginPath.toString() + "'", ex );
					}
				}
			}

			if ( missingDependency )
			{
				// We now iterate over plugins until something loads
				// This loop will ignore soft dependencies
				pluginIterator = plugins.keySet().iterator();

				while ( pluginIterator.hasNext() )
				{
					String plugin = pluginIterator.next();

					if ( !dependencies.containsKey( plugin ) )
					{
						softDependencies.remove( plugin );
						missingDependency = false;
						Path pluginFile = plugins.get( plugin );
						pluginIterator.remove();

						try
						{
							result.add( loadPlugin( pluginFile ) );
							loadedPlugins.add( plugin );
							break;
						}
						catch ( PluginInvalidException ex )
						{
							L.severe( "Could not load '" + pluginFile.toString() + "' in folder '" + pluginPath.toString() + "'", ex );
						}
					}
				}
				// We have no plugins left without a depend
				if ( missingDependency )
				{
					softDependencies.clear();
					dependencies.clear();
					Iterator<Path> failedPluginIterator = plugins.values().iterator();

					while ( failedPluginIterator.hasNext() )
					{
						Path pluginFile = failedPluginIterator.next();
						failedPluginIterator.remove();
						L.severe( "Could not load '" + pluginFile.toString() + "' in folder '" + pluginPath.toString() + "': circular dependency detected" );
					}
				}
			}
		}

		return result.stream();
	}

	/**
	 * Registers the specified plugin loader
	 * <p>
	 *
	 * @param loader Class name of the PluginLoader to register
	 *
	 * @throws IllegalArgumentException Thrown when the given Class is not a valid PluginLoader
	 */
	public void registerInterface( Class<? extends PluginLoader> loader ) throws IllegalArgumentException
	{
		PluginLoader instance;

		if ( PluginLoader.class.isAssignableFrom( loader ) )
		{
			Constructor<? extends PluginLoader> constructor;

			try
			{
				constructor = loader.getConstructor();
				instance = constructor.newInstance();
			}
			catch ( NoSuchMethodException ex )
			{
				/*
				 * try
				 * {
				 * constructor = loader.getConstructor( AppController.class );
				 * instance = constructor.newInstance( AppController.instance );
				 * }
				 * catch ( NoSuchMethodException ex1 )
				 * {
				 */
				String className = loader.getName();
				throw new IllegalArgumentException( String.format( "Class %s does not have a public %s(Server) constructor", className, className ), ex );
				/*
				 * }
				 * catch ( Exception ex1 )
				 * {
				 * throw new IllegalArgumentException( String.format( "Unexpected exception %s while attempting to construct a new instance of %s", ex.getClass().getProductName(), loader.getProductName() ), ex1 );
				 * }
				 */
			}
			catch ( Exception ex )
			{
				throw new IllegalArgumentException( String.format( "Unexpected exception %s while attempting to construct a new instance of %s", ex.getClass().getName(), loader.getName() ), ex );
			}
		}
		else
			throw new IllegalArgumentException( String.format( "Class %s does not implement interface PluginLoader", loader.getName() ) );

		Pattern[] patterns = instance.getPluginFileFilters();

		lock.lock();
		try
		{
			for ( Pattern pattern : patterns )
				fileAssociations.put( pattern, instance );
		}
		finally
		{
			lock.unlock();
		}
	}

	public void shutdown()
	{
		clearPlugins();
	}

	public static class ConfigKeys
	{
		public static final TypeBase BaseNode = new TypeBase( "plugins.conf" );
	}
}
