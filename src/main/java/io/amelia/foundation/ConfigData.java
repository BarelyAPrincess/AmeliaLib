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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.ContainerWithValue;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.ConfigException;
import io.amelia.support.Namespace;
import io.amelia.support.NodeStack;
import io.amelia.support.Objs;
import io.amelia.support.Voluntary;

public final class ConfigData extends ContainerWithValue<ConfigData, Object, ConfigException.Error> implements KeyValueTypesTrait<ConfigException.Error>
{
	public static ConfigData decodeJson( Path path ) throws IOException, ConfigException.Error
	{
		ConfigData configData = ConfigData.empty();
		ParcelLoader.decodeMap( ParcelLoader.decodeJsonToMap( path ), configData );
		return configData;
	}

	public static ConfigData decodeList( Path path ) throws IOException, ConfigException.Error
	{
		ConfigData configData = ConfigData.empty();
		ParcelLoader.decodeMap( ParcelLoader.decodeListToMap( path ), configData );
		return configData;
	}

	public static ConfigData decodeProp( Path path ) throws IOException, ConfigException.Error
	{
		ConfigData configData = ConfigData.empty();
		ParcelLoader.decodeMap( ParcelLoader.decodePropToMap( path ), configData );
		return configData;
	}

	public static ConfigData decodeYaml( Path path ) throws IOException, ConfigException.Error
	{
		ConfigData configData = ConfigData.empty();
		ParcelLoader.decodeMap( ParcelLoader.decodeYamlToMap( path ), configData );
		return configData;
	}

	@Nonnull
	public static ConfigData empty()
	{
		try
		{
			return new ConfigData( null, "" );
		}
		catch ( ConfigException.Error error )
		{
			// This should never happen!
			throw new RuntimeException( error );
		}
	}

	public static ConfigData of( String namespace )
	{
		return of( Namespace.of( Objs.notNullOrDef( namespace, "" ) ) );
	}

	public static ConfigData of( NodeStack namespace )
	{
		ConfigData current = empty();
		for ( String child : namespace.getNames() )
			current = current.getChildOrCreate( child );
		return current;
	}

	private String loadedValueHash = null;

	private ConfigData() throws ConfigException.Error
	{
		super( ConfigData::new, "" );
	}

	public ConfigData( @Nonnull String key ) throws ConfigException.Error
	{
		super( ConfigData::new, key );
	}

	public ConfigData( @Nullable ConfigData parent, @Nonnull String key ) throws ConfigException.Error
	{
		super( ConfigData::new, parent, key );
	}

	public ConfigData( @Nullable ConfigData parent, @Nonnull String key, @Nullable Object value ) throws ConfigException.Error
	{
		super( ConfigData::new, parent, key, value );
	}

	@Override
	protected ConfigException.Error getException( @Nonnull String message, Exception exception )
	{
		return new ConfigException.Error( this, message, exception );
	}

	@Override
	public Voluntary<Object> getValue( @Nonnull String key )
	{
		return super.getValue( key );
	}

	public void setEnvironmentVariables( Map<String, Object> map )
	{
		// TODO
	}

	<T> T setValueWithHash( Object obj )
	{
		notDisposed();
		// A loaded value is only set if the current value is null, was never set, or the new value hash doesn't match the loaded one.
		if ( loadedValueHash == null || value == null || !ParcelLoader.hashObject( obj ).equals( loadedValueHash ) )
		{
			loadedValueHash = ParcelLoader.hashObject( obj );
			return updateValue( obj );
		}
		return null;
	}

	<T> T setValueWithHash( String key, Object obj )
	{
		return getChildOrCreate( key ).setValueWithHash( obj );
	}

	@Override
	protected <T> T updateValue( Object value )
	{
		if ( getNamespace().getNodeCount() < 2 )
			throw new ConfigException.Ignorable( this, "You can't set configuration values on the top-level config node. Minimum depth is two!" );
		return super.updateValue( value );
	}
}
