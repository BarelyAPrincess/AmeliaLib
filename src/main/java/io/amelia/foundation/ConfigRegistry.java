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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.lang.ConfigException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
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
		config.setValueIfAbsent( ConfigKeys.WARN_ON_OVERLOAD, false );
		config.setValueIfAbsent( ConfigKeys.DEVELOPMENT_MODE, false );
	}

	public static void clearCache( @Nonnull File path, @Nonnegative long keepHistory )
	{
		Objs.notNull( path );
		Objs.notNull( keepHistory );

		if ( path.isDirectory() )
		{
			for ( File f : path.listFiles() )
				if ( f.isFile() && f.lastModified() < System.currentTimeMillis() - keepHistory * 24 * 60 * 60 )
					f.delete();
				else if ( f.isDirectory() )
					clearCache( f, keepHistory );
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
		Kernel.setAppPath( IO.buildFile( false, env.getString( "app-dir" ).orElse( null ) ) );

		env.getStringsMap().filter( e -> e.getKey().startsWith( "dir-" ) ).forEach( e -> Kernel.setPath( e.getKey().substring( 4 ), Strs.split( e.getValue(), "/" ).toArray( String[]::new ) ) );

		loader.loadConfig( config );
		isConfigLoaded = true;

		ConfigMap envNode = config.getChildOrCreate( "env" );
		for ( Map.Entry<String, Object> entry : env.map().entrySet() )
			envNode.setValue( entry.getKey().replace( '-', '_' ), entry.getValue() );
		envNode.addFlag( ConfigMap.Flag.READ_ONLY, ConfigMap.Flag.NO_SAVE );
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

		File configPath = Kernel.getPath( Kernel.PATH_CONFIG, true );

		IO.extractResourceDirectory( "config", configPath, ConfigRegistry.class );
	}

	private ConfigRegistry()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		public static final String APPLICATION_BASE = "app";
		public static final String WARN_ON_OVERLOAD = APPLICATION_BASE + ".warnOnOverload";
		public static final String DEVELOPMENT_MODE = APPLICATION_BASE + ".developmentMode";
		public static final String CONFIGURATION_BASE = "conf";

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
