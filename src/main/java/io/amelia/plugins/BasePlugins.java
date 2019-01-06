/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginNotFoundException;
import io.amelia.plugins.loader.PluginLoader;

public interface BasePlugins<Subclass extends BasePlugin>
{
	void clearPlugins();

	void disablePlugin( final Subclass plugin );

	void disablePlugins();

	void enablePlugin( final Subclass plugin );

	<T extends Subclass> Optional<T> getPluginByClass( @Nonnull Class<?> pluginClass );

	<T extends Subclass> T getPluginByClassWithException( @Nonnull Class<?> pluginClass ) throws PluginNotFoundException;

	Optional<Subclass> getPluginByClassname( String className ) throws PluginNotFoundException;

	Subclass getPluginByClassnameWithException( String className ) throws PluginNotFoundException;

	Subclass getPluginByNameWithException( String pluginName ) throws PluginNotFoundException;

	Optional<Subclass> getPluginByName( String pluginName ) throws PluginNotFoundException;

	Stream<Subclass> getPlugins();

	Subclass loadPlugin( @Nonnull Path pluginPath ) throws PluginInvalidException, PluginDependencyUnknownException;

	void loadPlugins() throws PluginException.Error, IOException;

	Stream<Subclass> loadPlugins( @Nonnull Path pluginPath ) throws IOException;

	void registerInterface( Class<? extends PluginLoader> loader ) throws IllegalArgumentException;

	void shutdown();
}
