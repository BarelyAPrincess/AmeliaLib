/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.support.NotImplemented;

/**
 * Provides result methods to keep base better organized.
 */
@NotImplemented
public abstract class ElegantQueryResult<T extends ElegantQuery> extends ElegantQuery<T>
{
	ElegantQueryResult( DatabaseDriver driver, String table, boolean autoUpdate )
	{
		super( driver, table, autoUpdate );
	}

	public void close()
	{

	}

	public final int count()
	{
		return 0;
	}

	public final Map<String, Object> first()
	{
		exceptionCheck();

		Map<String, Map<String, Object>> map = map();
		if ( map.size() == 0 )
			return null;
		return map.get( map.keySet().toArray( new String[0] )[0] );
	}

	public int getInt( String key )
	{
		return 0;
	}

	public Object getObject( String key )
	{
		return null;
	}

	public String getString( String key )
	{
		return null;
	}

	public final Map<String, Object> last()
	{
		exceptionCheck();

		Map<String, Map<String, Object>> map = map();
		if ( map.size() == 0 )
			return null;
		return map.get( map.keySet().toArray( new String[0] )[map.keySet().size() - 1] );
	}

	public List<Map<String, Object>> list()
	{
		return new ArrayList<>();
	}

	public final Map<String, Map<String, Object>> map()
	{
		return new HashMap<>();
	}

	public boolean next()
	{
		return false;
	}

	public Map<String, Object> row()
	{
		return row( Object.class );
	}

	public <V> Map<String, V> row( Class<V> valueType )
	{
		// TODO Return current row
		return null;
	}

	/*
	public final Map<String, Map<String, Object>> map() throws SQLException
	{
		return UtilDatabase.resultToMap( resultSet() );
	}

	public Map<String, Object> rowAbsolute( int row ) throws SQLException
	{
		ResultSet result = resultSet();
		if ( result != null && result.absolute( row ) )
			return UtilDatabase.rowToMap( result );
		return null;
	}

	public Map<String, Object> rowFirst() throws SQLException
	{
		ResultSet result = resultSet();
		if ( result != null && result.first() )
			return UtilDatabase.rowToMap( result );
		return null;
	}

	public Map<String, Object> rowLast() throws SQLException
	{
		ResultSet result = resultSet();
		if ( result != null && result.last() )
			return UtilDatabase.rowToMap( result );
		return null;
	}

	public final Map<String, Object> row() throws SQLException
	{
		return UtilDatabase.rowToMap( resultSet() );
	}

	public final Set<Map<String, Object>> set() throws SQLException
	{
		return UtilDatabase.resultToSet( resultSet() );
	}

	public final Map<String, Map<String, String>> stringMap() throws SQLException
	{
		return UtilDatabase.resultToStringMap( resultSet() );
	}

	public Map<String, String> stringRow() throws SQLException
	{
		return UtilDatabase.rowToStringMap( resultSet() );
	}

	public Set<Map<String, String>> stringSet() throws SQLException
	{
		return UtilDatabase.resultToStringSet( resultSet() );
	}*/
}
