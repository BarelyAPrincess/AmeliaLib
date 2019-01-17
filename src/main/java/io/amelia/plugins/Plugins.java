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
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.bindings.Singular;
import io.amelia.foundation.Kernel;
import io.amelia.lang.PluginDependencyUnknownException;
import io.amelia.lang.PluginException;
import io.amelia.lang.PluginInvalidException;
import io.amelia.lang.PluginNotFoundException;
import io.amelia.plugins.loader.PluginLoader;
import io.amelia.support.VoluntaryWithCause;

@Singular
public interface Plugins<Subclass extends BasePlugin>
{
	Kernel.Logger L = Kernel.getLogger( Plugins.class );

	void clearPlugins();

	void disablePlugin( final Subclass plugin );

	void disablePlugins();

	void enablePlugin( final Subclass plugin );

	VoluntaryWithCause<Subclass, PluginNotFoundException> getPluginByClass( @Nonnull Class<?> pluginClass );

	VoluntaryWithCause<Subclass, PluginNotFoundException> getPluginByClassname( String className );

	VoluntaryWithCause<Subclass, PluginNotFoundException> getPluginByName( String pluginName );

	Subclass getPluginByNameWithException( String pluginName ) throws PluginNotFoundException;

	Stream<Subclass> getPlugins();

	Subclass loadPlugin( @Nonnull Path pluginPath ) throws PluginInvalidException, PluginDependencyUnknownException;

	void loadPlugins() throws PluginException.Error, IOException;

	Stream<Subclass> loadPlugins( @Nonnull Path pluginPath ) throws IOException;

	void registerInterface( Class<? extends PluginLoader> loader ) throws IllegalArgumentException;

	void shutdown();
}
