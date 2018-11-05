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

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.support.ColumnType;
import io.amelia.lang.DatabaseException;

public class ElegantQueryTable extends ElegantQuery<ElegantQueryTable>
{
	public ElegantQueryTable( DatabaseDriver driver, String table )
	{
		super( driver, table, false );
	}

	@Override
	public ElegantQueryTable clone0()
	{
		return new ElegantQueryTable( getDriver(), getTable() );
	}

	public ElegantQueryTable columnCreate( ColumnType columnType, String columnName ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, columnType, 0, null );
		return this;
	}

	public ElegantQueryTable columnCreateInt( String columnName, int columnLength ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, ColumnType.INTEGER, columnLength, null );
		return this;
	}

	public ElegantQueryTable columnCreateInt( String columnName, int columnLength, int columnDef ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, ColumnType.INTEGER, columnLength, columnDef );
		return this;
	}

	public ElegantQueryTable columnCreateText( String columnName ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, ColumnType.TEXT, 0, null );
		return this;
	}

	public ElegantQueryTable columnCreateText( String columnName, String columnDef ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, ColumnType.TEXT, 0, columnDef );
		return this;
	}

	public ElegantQueryTable columnCreateVar( String columnName, int columnLength ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, ColumnType.STRING, columnLength, null );
		return this;
	}

	public ElegantQueryTable columnCreateVar( String columnName, int columnLength, String columnDef ) throws DatabaseException
	{
		getDriver().tableColumnCreate( getTable(), columnName, ColumnType.STRING, columnLength, columnDef );
		return this;
	}

	public ElegantQueryTable columnDrop( String colName ) throws DatabaseException
	{
		getDriver().tableColumnDrop( getTable(), colName );
		return this;
	}

	public List<String> columnsList() throws DatabaseException
	{
		return getDriver().tableColumnsList( getTable() );
	}

	public List<String> columnsRequired() throws DatabaseException
	{
		return getDriver().tableColumnsRequired( getTable() );
	}

	public ElegantQueryDelete delete()
	{
		return new ElegantQueryDelete( getDriver(), getTable() );
	}

	public ElegantQueryTable drop() throws DatabaseException
	{
		getDriver().tableDrop( getTable() );
		return this;
	}

	public boolean exists() throws DatabaseException
	{
		return getDriver().tableExists( getTable() );
	}

	public String indexKey() throws DatabaseException
	{
		return getDriver().tableIndexKey( getTable() );
	}

	public ElegantQueryInsert insert()
	{
		return new ElegantQueryInsert( getDriver(), getTable() );
	}

	public boolean isUpdateQuery()
	{
		return false;
	}

	@Override
	protected void preExecute() throws DatabaseException
	{
		// Ignore
	}

	public String primaryKey() throws DatabaseException
	{
		return getDriver().tablePrimaryKey( getTable() );
	}

	public ElegantQuerySelect select()
	{
		return new ElegantQuerySelect( getDriver(), getTable() );
	}

	public ElegantQuerySelect select( Collection<String> fields )
	{
		return select().fields( fields );
	}

	public Map<String, Object> selectOne( Map<String, Object> where ) throws SQLException
	{
		return new ElegantQuerySelect( getDriver(), getTable() ).whereMatches( where ).limit( 1 ).first();
	}

	public ElegantQueryUpdate update()
	{
		return new ElegantQueryUpdate( getDriver(), getTable() );
	}
}
