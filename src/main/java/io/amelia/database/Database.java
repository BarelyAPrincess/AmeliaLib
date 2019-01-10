/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.elegant.ElegantQueryDelete;
import io.amelia.database.elegant.ElegantQueryInsert;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.database.elegant.ElegantQueryTable;
import io.amelia.database.elegant.ElegantQueryUpdate;

public class Database
{
	DatabaseDriver driver;

	public Database( @Nonnull DatabaseDriver driver )
	{
		this.driver = driver;
	}

	public ElegantQueryDelete delete( String table )
	{
		return new ElegantQueryTable( driver, table ).delete();
	}

	public ElegantQueryDelete delete( String table, Map<String, Object> where )
	{
		return delete( table, where, -1 );
	}

	public ElegantQueryDelete delete( String table, Map<String, Object> where, int lmt )
	{
		return delete( table ).where( where ).limit( lmt );
	}

	public ElegantQueryInsert insert( String table )
	{
		return new ElegantQueryTable( driver, table ).insert();
	}

	public ElegantQueryInsert insert( String table, Map<String, Object> data )
	{
		return insert( table ).values( data );
	}

	public ElegantQuerySelect select( String table )
	{
		return new ElegantQueryTable( driver, table ).select();
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQuerySelect select( String table, Map<String, Object> map )
	{
		return select( table ).where( map );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQuerySelect select( String table, String key, Object value )
	{
		return select( table, new HashMap<String, Object>()
		{
			{
				put( key, value );
			}
		} );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public Map<String, Object> selectOne( String table, Map<String, Object> where )
	{
		return select( table, where ).limit( 1 ).first();
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public Map<String, Object> selectOne( String table, String key, Object value )
	{
		return select( table, new HashMap<String, Object>()
		{
			{
				put( key, value );
			}
		} ).limit( 1 ).first();
	}

	public ElegantQueryTable table( String table )
	{
		return new ElegantQueryTable( driver, table );
	}

	public ElegantQueryUpdate update( String table )
	{
		return new ElegantQueryTable( driver, table ).update();
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Collection<String> dataKeys, Collection<Object> dataValues )
	{
		return update( table, dataKeys, dataValues, null, null, -1 );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Collection<String> dataKeys, Collection<Object> dataValues, Collection<String> whereKeys, Collection<Object> whereValues )
	{
		return update( table, dataKeys, dataValues, whereKeys, whereValues, -1 );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Collection<String> dataKeys, Collection<Object> dataValues, Collection<String> whereKeys, Collection<Object> whereValues, int lmt )
	{
		ElegantQueryUpdate query = update( table ).limit( lmt );
		if ( dataKeys != null && dataValues != null && Math.min( dataKeys.size(), dataValues.size() ) > 0 )
			query.values( dataKeys.toArray( new String[0] ), dataValues.toArray( new Object[0] ) );
		if ( whereKeys != null && whereValues != null && Math.min( whereKeys.size(), whereValues.size() ) > 0 )
			query.whereMatches( whereKeys, whereValues );
		return query;
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Collection<String> dataKeys, Collection<Object> dataValues, int lmt )
	{
		return update( table, dataKeys, dataValues, null, null, lmt );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Map<String, Object> data )
	{
		return update( table, data, null, -1 );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Map<String, Object> data, int lmt )
	{
		return update( table, data, null, lmt );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Map<String, Object> data, Map<String, Object> where )
	{
		return update( table, data, where, -1 );
	}

	/**
	 * Legacy Method
	 */
	@Deprecated
	public ElegantQueryUpdate update( String table, Map<String, Object> data, Map<String, Object> where, int lmt )
	{
		ElegantQueryUpdate query = update( table ).limit( lmt );
		if ( data != null && data.size() > 0 )
			query.values( data );
		if ( where != null && where.size() > 0 )
			query.where( where );
		return query;
	}
}
