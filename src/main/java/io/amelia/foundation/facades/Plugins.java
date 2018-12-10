/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.facades;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.bindings.BindingsException;
import io.amelia.bindings.Bindings;
import io.amelia.foundation.plugins.PluginImpl;
import io.amelia.foundation.plugins.PluginsImpl;
import io.amelia.lang.APINotice;
import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginNotFoundException;
import io.amelia.plugins.loader.PluginLoader;

@APINotice
public class Plugins
{
	public static void clearPlugins()
	{
		getInstance().clearPlugins();
	}

	public static void disablePlugin( final PluginImpl plugin )
	{
		getInstance().disablePlugin( plugin );
	}

	public static void disablePlugins()
	{
		getInstance().disablePlugins();
	}

	public static void enablePlugin( final PluginImpl plugin )
	{
		getInstance().enablePlugin( plugin );
	}

	public static PluginsImpl getInstance()
	{
		return Bindings.resolveClassOrFail( PluginsImpl.class, () -> new BindingsException.Ignorable( "The Plugins Subsystem is not loaded. This is either an application or initialization bug." ) );
	}

	public static PluginImpl getPluginByClass( Class<?> clz ) throws PluginNotFoundException
	{
		return getInstance().getPluginByClass( clz );
	}

	public static PluginImpl getPluginByClassWithoutException( Class<?> clz )
	{
		return getInstance().getPluginByClassWithoutException( clz );
	}

	public static PluginImpl getPluginByClassname( String className ) throws PluginNotFoundException
	{
		return getInstance().getPluginByClassname( className );
	}

	public static PluginImpl getPluginByClassnameWithoutException( String className )
	{
		return getInstance().getPluginByNameWithoutException( className );
	}

	public static PluginImpl getPluginByName( String pluginName ) throws PluginNotFoundException
	{
		return getInstance().getPluginByName( pluginName );
	}

	public static PluginImpl getPluginByNameWithoutException( String pluginName )
	{
		return getInstance().getPluginByNameWithoutException( pluginName );
	}

	public static synchronized PluginImpl[] getPlugins()
	{
		return getInstance().getPlugins();
	}

	public static synchronized PluginImpl loadPlugin( @Nonnull Path pluginPath ) throws PluginInvalidException, PluginDependencyUnknownException
	{
		return getInstance().loadPlugin( pluginPath );
	}

	public static void loadPlugins() throws PluginException.Error, IOException
	{
		getInstance().loadPlugins();
	}

	public static PluginImpl[] loadPlugins( @Nonnull Path pluginPath ) throws IOException
	{
		return getInstance().loadPlugins( pluginPath );
	}

	public static void registerInterface( Class<? extends PluginLoader> loader ) throws IllegalArgumentException
	{
		getInstance().registerInterface( loader );
	}

	public static void shutdown()
	{
		getInstance().shutdown();
	}

	public Plugins()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		public static final TypeBase BaseNode = new TypeBase( "plugins.conf" );
	}
}
