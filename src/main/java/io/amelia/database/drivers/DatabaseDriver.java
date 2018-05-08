/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.drivers;

import java.sql.SQLException;
import java.util.List;

import io.amelia.database.cache.QueryCache;
import io.amelia.database.elegant.ElegantQuery;
import io.amelia.database.support.ColumnType;
import io.amelia.lang.DatabaseException;
import io.amelia.support.LocalBoolean;

/**
 * Provides the base outline for database drivers
 */
public interface DatabaseDriver
{
	LocalBoolean DEBUG_ENABLED = new LocalBoolean( false );

	QueryCache execute( ElegantQuery query, boolean isDebugQuery ) throws DatabaseException;

	boolean isConnected();

	default boolean isDebugEnabled()
	{
		return DEBUG_ENABLED.get( this );
	}

	default void setDebugEnabled( boolean debugEnabled )
	{
		DEBUG_ENABLED.set( this, debugEnabled );
	}

	void tableColumnCreate( String table, String columnName, ColumnType columnType, int columnLength, Object columnDef ) throws DatabaseException;

	void tableColumnDrop( String table, String colName ) throws DatabaseException;

	List<String> tableColumnsList( String table ) throws DatabaseException;

	List<String> tableColumnsRequired( String table ) throws DatabaseException;

	void tableDrop( String table ) throws DatabaseException;

	boolean tableExists( String table ) throws DatabaseException;

	String tableIndexKey( String table ) throws DatabaseException;

	String tablePrimaryKey( String table ) throws DatabaseException;

	interface ConnectionBuilder
	{
		DatabaseDriver connect() throws SQLException;
	}
}
