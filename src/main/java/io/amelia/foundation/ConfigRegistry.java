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
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.lang.ConfigException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Streams;

public class ConfigRegistry
{
	public static final ConfigData config = ConfigData.empty();
	private static boolean loaded = false;

	// TODO This LOADER is not thread-safe but if the application initialization works as intended, this shouldn't be an issue.
	public static final ConfigLoader LOADER = new ConfigLoader()
	{
		private ConfigData tempConfig;

		@Override
		public void commitConfig( @Nonnull ConfigLoader.CommitType type ) throws ConfigException.Error
		{
			Streams.forEachWithException( tempConfig.getChildren(), child -> config.setChild( child, true ) );
			tempConfig = null;
			loaded = true;
		}

		@Override
		public ConfigData beginConfig() throws ConfigException.Error
		{
			if ( tempConfig != null )
				throw new ConfigException.Error( tempConfig, "Configuration must be first be committed!" );
			tempConfig = ConfigData.empty();
			return tempConfig;
		}
	};

	/*
	 * We set default config values here for end-user reference, they're then saved to the config file upon load (if unset).
	 */
	static
	{
		try
		{
			config.setValueIfAbsent( ConfigKeys.WARN_ON_OVERLOAD );
			config.setValueIfAbsent( ConfigKeys.DEVELOPMENT_MODE );
			config.setValueIfAbsent( ConfigKeys.DEFAULT_BINARY_CHARSET );
			config.setValueIfAbsent( ConfigKeys.DEFAULT_TEXT_CHARSET );
		}
		catch ( ConfigException.Error e )
		{
			// Ignore
		}
	}

	public static void clearCache( @Nonnull Path path, @Nonnegative long keepHistory )
	{
		Objs.notNull( path );
		Objs.notNull( keepHistory );
		Objs.notNegative( keepHistory );

		try
		{
			if ( Files.isDirectory( path ) )
				Streams.forEachWithException( Files.list( path ), file -> {
					if ( Files.isDirectory( file ) )
						clearCache( file, keepHistory );
					else if ( Files.isRegularFile( file ) && IO.getLastModified( file ) < System.currentTimeMillis() - keepHistory * 24 * 60 * 60 )
						Files.delete( file );
				} );
		}
		catch ( IOException e )
		{
			Kernel.L.warning( "Exception thrown while clearing cache for directory " + path.toString(), e );
		}
	}

	public static void clearCache( @Nonnegative long keepHistory )
	{
		clearCache( Kernel.getPath( Kernel.PATH_CACHE ), keepHistory );
	}

	public static ConfigData getChild( String key )
	{
		return config.getChild( key );
	}

	public static ConfigData getChildOrCreate( String key )
	{
		return config.getChildOrCreate( key );
	}

	public static boolean isLoaded()
	{
		return loaded;
	}

	public static void save()
	{
		// TODO Save
	}

	public static void setObject( String key, Object value ) throws ConfigException.Error
	{
		if ( value instanceof ConfigData )
			config.getChildOrCreate( key ).setChild( ( ConfigData ) value, false );
		else
			config.getChildOrCreate( key ).setValue( value );
	}

	private static void vendorConfig() throws IOException
	{
		// WIP Copies config from resources and plugins to config directories.

		Path configPath = Kernel.getPath( Kernel.PATH_CONFIG, true );

		IO.extractResourceDirectory( "config", configPath, ConfigRegistry.class );
	}

	private ConfigRegistry()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		public static final TypeBase APPLICATION_BASE = new TypeBase( "foundation" );
		public static final TypeBase.TypeBoolean WARN_ON_OVERLOAD = new TypeBase.TypeBoolean( APPLICATION_BASE, "warnOnOverload", false );
		public static final TypeBase.TypeBoolean DEVELOPMENT_MODE = new TypeBase.TypeBoolean( APPLICATION_BASE, "developmentMode", false );
		public static final TypeBase CONFIGURATION_BASE = new TypeBase( "conf" );
		public static final TypeBase CONTENT_TYPES = new TypeBase( CONFIGURATION_BASE, "contentTypes" );
		public static final TypeBase EXT_TYPES = new TypeBase( CONFIGURATION_BASE, "extTypes" );
		public static final TypeBase.TypeString DEFAULT_BINARY_CHARSET = new TypeBase.TypeString( CONFIGURATION_BASE, "defaultBinaryCharset", "ISO-8859-1" );
		public static final TypeBase.TypeString DEFAULT_TEXT_CHARSET = new TypeBase.TypeString( CONFIGURATION_BASE, "defaultBinaryCharset", "UTF-8" );

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
