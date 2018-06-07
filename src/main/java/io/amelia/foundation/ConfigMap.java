/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.util.Map;
import java.util.Optional;

import io.amelia.data.StackerWithValue;
import io.amelia.data.ValueTypesTrait;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.ConfigException;

public final class ConfigMap extends StackerWithValue<ConfigMap, Object> implements ValueTypesTrait
{
	private String loadedValueHash = null;

	public ConfigMap()
	{
		super( ConfigMap::new, "" );
	}

	public ConfigMap( String key )
	{
		super( ConfigMap::new, key );
	}

	public ConfigMap( ConfigMap parent, String key )
	{
		super( ConfigMap::new, parent, key );
	}

	public ConfigMap( ConfigMap parent, String key, Object value )
	{
		super( ConfigMap::new, parent, key, value );
	}

	void loadNewValue( Object obj )
	{
		disposeCheck();
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
	public Optional<Object> getValue( String key )
	{
		return super.getValue( key );
	}

	public void setEnviromentVariables( Map<String, Object> map )
	{

	}

	@Override
	public void throwExceptionError( String message ) throws ConfigException.Error
	{
		throw new ConfigException.Error( this, message );
	}

	@Override
	public void throwExceptionIgnorable( String message ) throws ConfigException.Ignorable
	{
		throw new ConfigException.Ignorable( this, message );
	}

	@Override
	protected Object updateValue( Object value )
	{
		if ( getNamespace().getNodeCount() < 2 )
			throw new ConfigException.Ignorable( this, "You can't set configuration values on the top-level config node. Minimum depth is two!" );
		return super.updateValue( value );
	}

}
