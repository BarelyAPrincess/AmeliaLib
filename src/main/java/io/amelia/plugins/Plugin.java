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
import java.io.InputStream;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import io.amelia.data.parcel.Parcel;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.PluginException;
import io.amelia.plugins.loader.PluginLoader;

public abstract class Plugin implements BasePlugin
{
	@Override
	public final boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( !( obj instanceof Plugin ) )
			return false;
		return getName().equals( ( ( Plugin ) obj ).getName() );
	}

	@Override
	public abstract Parcel getConfig();

	@Override
	public abstract Path getConfigPath();

	@Override
	public abstract Path getDataPath();

	@Override
	public abstract PluginMeta getMeta();

	/**
	 * Returns the name of the plugin.
	 * <p>
	 * This should return the bare name of the plugin and should be used for comparison.
	 *
	 * @return name of the plugin
	 */
	@Nonnull
	@Override
	public final String getName()
	{
		return getMeta().getName();
	}

	@Override
	public PluginLoader getPluginLoader()
	{
		return null;
	}

	@Override
	public InputStream getResource( String filename )
	{
		return null;
	}

	@Override
	public final int hashCode()
	{
		try
		{
			return getName().hashCode();
		}
		catch ( NullPointerException e )
		{
			return super.hashCode();
		}
	}

	@Override
	public abstract void init( PluginLoader loader, PluginMeta meta, Path dataPath, Path pluginPath, ClassLoader classLoader );

	@Override
	public abstract boolean isEnabled();

	@Override
	public abstract boolean isNaggable();

	@Override
	public abstract void onDisable() throws PluginException.Error;

	@Override
	public abstract void onEnable() throws PluginException.Error;

	@Override
	public abstract void onLoad() throws PluginException.Error;

	@Override
	public abstract void reloadConfig() throws IOException, ParcelableException.Error;

	@Override
	public abstract void saveConfig();

	@Override
	public abstract void saveDefaultConfig();

	@Override
	public abstract void saveResource( String resourcePath, boolean replace );

	@Override
	public abstract void setNaggable( boolean canNag );
}
