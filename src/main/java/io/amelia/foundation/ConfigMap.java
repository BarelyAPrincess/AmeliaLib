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

import io.amelia.data.ContainerWithValue;
import io.amelia.data.ValueTypesTrait;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.ConfigException;
import io.amelia.support.OptionalExt;

public final class ConfigMap extends ContainerWithValue<ConfigMap, Object, ConfigException.Error> implements ValueTypesTrait<ConfigException.Error>
{
	private String loadedValueHash = null;

	public ConfigMap() throws ConfigException.Error
	{
		super( ConfigMap::new, "" );
	}

	public ConfigMap( String key ) throws ConfigException.Error
	{
		super( ConfigMap::new, key );
	}

	public ConfigMap( ConfigMap parent, String key ) throws ConfigException.Error
	{
		super( ConfigMap::new, parent, key );
	}

	public ConfigMap( ConfigMap parent, String key, Object value ) throws ConfigException.Error
	{
		super( ConfigMap::new, parent, key, value );
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
	public OptionalExt<Object, ConfigException.Error> getValue( String key )
	{
		return super.getValue( key );
	}

	public void setEnviromentVariables( Map<String, Object> map )
	{

	}

	@Override
	protected Object updateValue( Object value )
	{
		if ( getNamespace().getNodeCount() < 2 )
			throw new ConfigException.Ignorable( this, "You can't set configuration values on the top-level config node. Minimum depth is two!" );
		return super.updateValue( value );
	}

}
