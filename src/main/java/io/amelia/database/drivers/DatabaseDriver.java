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

import io.amelia.lang.DatabaseException;
import io.amelia.database.DatabaseColumnType;
import io.amelia.database.elegant.support.ElegantQuery;
import io.amelia.database.support.QueryCache;

/**
 * Provides the base outline for database drivers
 */
public abstract class DatabaseDriver
{
	private boolean debugEnabled = false;

	public abstract QueryCache execute( ElegantQuery query, boolean isDebugQuery ) throws DatabaseException;

	public abstract boolean isConnected();

	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}

	public void setDebugEnabled( boolean debugEnabled )
	{
		this.debugEnabled = debugEnabled;
	}

	public abstract void tableColumnCreate( String table, String columnName, DatabaseColumnType columnType, int columnLength, Object columnDef ) throws DatabaseException;

	public abstract void tableColumnDrop( String table, String colName ) throws DatabaseException;

	public abstract List<String> tableColumnsList( String table ) throws DatabaseException;

	public abstract List<String> tableColumnsRequired( String table ) throws DatabaseException;

	public abstract void tableDrop( String table ) throws DatabaseException;

	public abstract boolean tableExists( String table ) throws DatabaseException;

	public abstract String tableIndexKey( String table ) throws DatabaseException;

	public abstract String tablePrimaryKey( String table ) throws DatabaseException;

	interface ConnectionBuilder
	{
		DatabaseDriver connect() throws SQLException;
	}
}
