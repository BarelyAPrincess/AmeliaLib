/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.lang.ConfigException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Streams;
import io.amelia.support.Strs;

public class ConfigRegistry
{
	public static final ConfigMap config = new ConfigMap();
	private static boolean isConfigLoaded;

	/*
	 * We set default config values here for end-user reference, they're then saved to the config file upon load (if unset).
	 */
	static
	{
		config.setValueIfAbsent( Config.WARN_ON_OVERLOAD );
		config.setValueIfAbsent( Config.DEVELOPMENT_MODE );
		config.setValueIfAbsent( Config.DEFAULT_BINARY_CHARSET );
		config.setValueIfAbsent( Config.DEFAULT_TEXT_CHARSET );
	}

	public static void clearCache( @Nonnull Path path, @Nonnegative long keepHistory )
	{
		Objs.notNull( path );
		Objs.notNull( keepHistory );

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

	public static ConfigMap getChild( String key )
	{
		return config.getChild( key );
	}

	public static ConfigMap getChildOrCreate( String key )
	{
		return config.getChildOrCreate( key );
	}

	public static void init( Env env, ConfigRegistryLoader loader ) throws ConfigException.Error
	{
		Kernel.setAppPath( IO.buildPath( false, env.getString( "app-dir" ).orElse( null ) ) );

		env.getStringsMap().filter( e -> e.getKey().startsWith( "dir-" ) ).forEach( e -> Kernel.setPath( e.getKey().substring( 4 ), Strs.split( e.getValue(), "/" ).toArray( String[]::new ) ) );

		loader.loadConfig( config );
		isConfigLoaded = true;

		ConfigMap envNode = config.getChildOrCreate( "env" );
		for ( Map.Entry<String, Object> entry : env.map().entrySet() )
			envNode.setValue( entry.getKey().replace( '-', '_' ), entry.getValue() );
		envNode.addFlag( ConfigMap.Flag.READ_ONLY, ConfigMap.Flag.NO_SAVE );

		// TODO Call loader.loadConfigAdditional( config ) once some additional internal systems are initialized.
	}

	public static boolean isConfigLoaded()
	{
		return isConfigLoaded;
	}

	public static void save()
	{
		// TODO Save
	}

	public static void setObject( String key, Object value )
	{
		if ( value instanceof ConfigMap )
			config.setChild( key, ( ConfigMap ) value, false );
		else
			config.setValue( key, value );
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

	public static class Config
	{
		public static final TypeBase APPLICATION_BASE = new TypeBase( "app" );
		public static final TypeBase.TypeBoolean WARN_ON_OVERLOAD = new TypeBase.TypeBoolean( APPLICATION_BASE, "warnOnOverload", false );
		public static final TypeBase.TypeBoolean DEVELOPMENT_MODE = new TypeBase.TypeBoolean( APPLICATION_BASE, "developmentMode", false );
		public static final TypeBase CONFIGURATION_BASE = new TypeBase( "conf" );
		public static final TypeBase CONTENT_TYPES = new TypeBase( CONFIGURATION_BASE, "contentTypes" );
		public static final TypeBase EXT_TYPES = new TypeBase( CONFIGURATION_BASE, "extTypes" );
		public static final TypeBase.TypeString DEFAULT_BINARY_CHARSET = new TypeBase.TypeString( CONFIGURATION_BASE, "defaultBinaryCharset", "ISO-8859-1" );
		public static final TypeBase.TypeString DEFAULT_TEXT_CHARSET = new TypeBase.TypeString( CONFIGURATION_BASE, "defaultBinaryCharset", "UTF-8" );

		private Config()
		{
			// Static Access
		}
	}
}
