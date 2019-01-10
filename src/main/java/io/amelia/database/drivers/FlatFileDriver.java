/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.drivers;

import java.util.List;

import io.amelia.lang.DatabaseException;
import io.amelia.database.DatabaseColumnType;
import io.amelia.database.elegant.support.ElegantQuery;
import io.amelia.database.support.QueryCache;

public class FlatFileDriver extends DatabaseDriver
{
	@Override
	public QueryCache execute( ElegantQuery query, boolean isDebugQuery ) throws DatabaseException
	{
		return null;
	}

	@Override
	public boolean isConnected()
	{
		return false;
	}

	@Override
	public void tableColumnCreate( String table, String columnName, DatabaseColumnType columnType, int columnLength, Object columnDef ) throws DatabaseException
	{

	}

	@Override
	public void tableColumnDrop( String table, String colName ) throws DatabaseException
	{

	}

	@Override
	public List<String> tableColumnsList( String table ) throws DatabaseException
	{
		return null;
	}

	@Override
	public List<String> tableColumnsRequired( String table ) throws DatabaseException
	{
		return null;
	}

	@Override
	public void tableDrop( String table ) throws DatabaseException
	{

	}

	@Override
	public boolean tableExists( String table ) throws DatabaseException
	{
		return false;
	}

	@Override
	public String tableIndexKey( String table ) throws DatabaseException
	{
		return null;
	}

	@Override
	public String tablePrimaryKey( String table ) throws DatabaseException
	{
		return null;
	}
}
