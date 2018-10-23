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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.ContainerWithValue;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.ConfigException;
import io.amelia.support.Voluntary;

public final class ConfigMap extends ContainerWithValue<ConfigMap, Object, ConfigException.Error> implements KeyValueTypesTrait<ConfigException.Error>
{
	private String loadedValueHash = null;

	private ConfigMap() throws ConfigException.Error
	{
		super( ConfigMap::new, "" );
	}

	public ConfigMap( @Nonnull String key ) throws ConfigException.Error
	{
		super( ConfigMap::new, key );
	}

	public ConfigMap( @Nullable ConfigMap parent, @Nonnull String key ) throws ConfigException.Error
	{
		super( ConfigMap::new, parent, key );
	}

	public ConfigMap( @Nullable ConfigMap parent, @Nonnull String key, @Nullable Object value ) throws ConfigException.Error
	{
		super( ConfigMap::new, parent, key, value );
	}

	@Nonnull
	public static ConfigMap empty()
	{
		try
		{
			return new ConfigMap( null, "" );
		}
		catch ( ConfigException.Error error )
		{
			// This should never happen!
			throw new RuntimeException( error );
		}
	}

	@Override
	protected ConfigException.Error getException( String message )
	{
		return new ConfigException.Error( this, message );
	}

	void loadNewValue( Object obj )
	{
		disposalCheck();
		// A loaded value is only set if the current value is null, was never set, or the new value hash doesn't match the loaded one.
		if ( loadedValueHash == null || value == null || !ParcelLoader.hashObject( obj ).equals( loadedValueHash ) )
		{
			loadedValueHash = ParcelLoader.hashObject( obj );
			updateValue( obj );
		}
	}

	void loadNewValue( String key, Object obj )
	{
		getChildOrCreate( key ).loadNewValue( obj );
	}

	@Override
	public Voluntary<Object, ConfigException.Error> getValue( String key )
	{
		return super.getValue( key );
	}

	public void setEnvironmentVariables( Map<String, Object> map )
	{
		// TODO
	}

	@Override
	protected Object updateValue( Object value )
	{
		if ( getNamespace().getNodeCount() < 2 )
			throw new ConfigException.Ignorable( this, "You can't set configuration values on the top-level config node. Minimum depth is two!" );
		return super.updateValue( value );
	}

}
