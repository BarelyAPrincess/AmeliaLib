/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.amelia.database.drivers.SQLBaseDriver;
import io.amelia.support.Objs;

public class SQLTableMeta implements Iterable<String>
{
	private final List<SQLColumn> columns = new ArrayList<>();
	private SQLBaseDriver driver;
	private String table;

	public SQLTableMeta( SQLBaseDriver driver, String table ) throws SQLException
	{
		this.driver = driver;
		this.table = table;
		refresh();
	}

	public List<String> columnNames()
	{
		List<String> rtn = new ArrayList<>();
		for ( SQLColumn m : columns )
			rtn.add( m.name );
		return rtn;
	}

	public List<String> columnNamesRequired()
	{
		List<String> rtn = new ArrayList<>();
		for ( SQLColumn m : columns )
			if ( Objs.isNull( m.def ) && !m.isNullable )
				rtn.add( m.name );
		return rtn;
	}

	public List<SQLColumn> columns()
	{
		return Collections.unmodifiableList( columns );
	}

	public List<SQLColumn> columnsRequired()
	{
		List<SQLColumn> rtn = new ArrayList<>();
		for ( SQLColumn m : columns )
			if ( Objs.isNull( m.def ) && !m.isNullable )
				rtn.add( m );
		return rtn;
	}

	public boolean contains( String colName )
	{
		for ( SQLColumn m : columns )
			if ( m.name().equals( colName ) )
				return true;
		return false;
	}

	public int count()
	{
		return columns.size();
	}

	public boolean exists() throws SQLException
	{
		ResultSet rs = driver.getMetaData().getTables( null, null, null, null );

		while ( rs.next() )
			if ( rs.getString( 3 ).equals( table ) )
				return true;
		return false;
	}

	public SQLColumn get( String name )
	{
		for ( SQLColumn c : columns )
			if ( c.name.equals( name ) )
				return c;
		return null;
	}

	@Override
	public Iterator<String> iterator()
	{
		List<String> rtn = new ArrayList<>();
		for ( SQLColumn m : columns )
			rtn.add( m.name );
		return rtn.iterator();
	}

	public SQLTableMeta refresh() throws SQLException
	{
		ResultSet sqlColumns = driver.getMetaData().getColumns( null, null, table, null );
		columns.clear();

		while ( sqlColumns.next() )
		{
			String name = sqlColumns.getString( "COLUMN_NAME" );
			int type = sqlColumns.getInt( "DATA_TYPE" );
			int size = sqlColumns.getInt( "COLUMN_SIZE" );
			String def = sqlColumns.getString( "COLUMN_DEF" );
			boolean isNullable = "YES".equals( sqlColumns.getString( "IS_NULLABLE" ) );

			columns.add( new SQLColumn( name, size, type, def, isNullable ) );
		}

		return this;
	}

	public class SQLColumn
	{
		private final String def;
		private final boolean isNullable;
		private final String name;
		private final int size;
		private final int type;

		SQLColumn( String name, int size, int type, String def, boolean isNullable )
		{
			this.name = name;
			this.size = size;
			this.type = type;
			this.def = def;
			this.isNullable = isNullable;
		}

		public String def()
		{
			return def;
		}

		public boolean isNullable()
		{
			return isNullable;
		}

		public String name()
		{
			return name;
		}

		public int size()
		{
			return size;
		}

		public int type()
		{
			return type;
		}
	}
}
