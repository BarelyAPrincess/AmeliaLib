/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant;

import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.amelia.database.Database;
import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.elegant.types.Values;
import io.amelia.lang.DatabaseException;

public final class ElegantQueryInsert extends ElegantQueryResult<ElegantQueryInsert> implements Values<ElegantQueryInsert>, Cloneable
{
	private Map<String, Object> values = new HashMap<>();

	public ElegantQueryInsert( DatabaseDriver sql, String table )
	{
		this( sql, table, false );
	}

	public ElegantQueryInsert( DatabaseDriver sql, String table, boolean autoExecute )
	{
		super( sql, table, autoExecute );
	}

	@Override
	public ElegantQueryInsert clone0()
	{
		ElegantQueryInsert clone = new ElegantQueryInsert( getDriver(), getTable() );

		clone.values.putAll( this.values );

		return clone;
	}

	@Override
	public Map<String, Object> getValues()
	{
		return Collections.unmodifiableMap( values );
	}

	@Override
	protected void preExecute() throws DatabaseException
	{
		List<String> requiredColumns = getDriver().tableColumnsRequired( getTable() );
		if ( !values.keySet().containsAll( requiredColumns ) )
			throw new DatabaseException( "The required columns were not satisfied. Provided columns were '" + Joiner.on( "," ).join( values.keySet() ) + "', required columns are '" + Joiner.on( "," ).join( requiredColumns ) + "'" );
	}

	@Override
	public ElegantQueryInsert value( String key, Object val )
	{
		values.put( key, val );
		return this;
	}

	@Override
	public ElegantQueryInsert values( String[] keys, Object[] valuesArray )
	{
		for ( int i = 0; i < Math.min( keys.length, valuesArray.length ); i++ )
			values.put( keys[i], valuesArray[i] );

		if ( keys.length != valuesArray.length )
			Database.L.warning( "keys/values were omitted due to length mismatches. (Keys: " + keys.length + ", Values: " + valuesArray.length + ")" );

		return this;
	}

	@Override
	public ElegantQueryInsert values( Map<String, Object> map )
	{
		values.putAll( map );
		return this;
	}


}
