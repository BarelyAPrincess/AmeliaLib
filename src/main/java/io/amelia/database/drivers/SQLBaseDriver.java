/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.drivers;

import com.google.common.base.Joiner;
import com.mysql.jdbc.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;

import java.io.NotSerializableException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.database.Database;
import io.amelia.database.cache.SQLQueryCache;
import io.amelia.database.elegant.ElegantQuery;
import io.amelia.database.elegant.ElegantQueryDelete;
import io.amelia.database.elegant.ElegantQueryInsert;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.database.elegant.ElegantQueryTable;
import io.amelia.database.elegant.ElegantQueryUpdate;
import io.amelia.database.elegant.types.WhereItem;
import io.amelia.database.support.ColumnType;
import io.amelia.database.support.SQLTableMeta;
import io.amelia.foundation.Kernel;
import io.amelia.lang.DatabaseException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Triplet;

/**
 * Implements base methods for traditional SQL databases
 */
public abstract class SQLBaseDriver implements DatabaseDriver
{
	private final static Map<ColumnType, Triplet<Integer, Class, String>> COLUMN_COMPATIBILITY = Collections.unmodifiableMap( new HashMap<ColumnType, Triplet<Integer, Class, String>>()
	{{
		put( ColumnType.STRING, new Triplet<>( 256, String.class, "VARCHAR" ) );
		put( ColumnType.TEXT, new Triplet<>( 0, String.class, "TEXT" ) );
		put( ColumnType.BOOLEAN, new Triplet<>( 0, String.class, "TINYINT(1)" ) );
		put( ColumnType.INTEGER, new Triplet<>( 11, String.class, "INT" ) );
	}} );

	static
	{
		ExceptionReport.registerException( ( cause, report, context ) -> {
			report.addException( ReportingLevel.E_ERROR, cause );
			return ReportingLevel.E_ERROR;
		}, SQLException.class );
	}

	private boolean autoReconnect = true;
	private Connection connection = null;

	@Override
	public SQLQueryCache execute( ElegantQuery query, boolean isDebugQuery ) throws DatabaseException
	{
		if ( query instanceof ElegantQueryDelete )
			return execute( ( ElegantQueryDelete ) query, isDebugQuery );
		else if ( query instanceof ElegantQueryInsert )
			return execute( ( ElegantQueryInsert ) query, isDebugQuery );
		else if ( query instanceof ElegantQuerySelect )
			return execute( ( ElegantQuerySelect ) query, isDebugQuery, false );
		else if ( query instanceof ElegantQueryTable )
			return execute( ( ElegantQueryTable ) query, isDebugQuery, false );
		else if ( query instanceof ElegantQueryUpdate )
			return execute( ( ElegantQueryUpdate ) query, isDebugQuery );
		else
			throw new DatabaseException( "Unsupported Query. You should report this to the developer. " + query.getClass().getSimpleName() );
	}

	public SQLQueryCache execute( ElegantQueryDelete elegantQuery, boolean isDebugQuery ) throws DatabaseException
	{
		synchronized ( elegantQuery )
		{
			List<String> segments = new LinkedList<>();
			List<Object> sqlValues = new LinkedList<>();

			segments.add( "DELETE FROM" );

			segments.add( Strs.wrap( elegantQuery.getTable(), '`' ) );

			sqlValues.clear();

			if ( elegantQuery.getElements().size() > 0 )
			{
				segments.add( "WHERE" );

				for ( WhereItem e : elegantQuery.getElements() )
				{
					if ( e.seperator() != WhereItem.Divider.NONE && e != elegantQuery.getElements().get( 0 ) )
						segments.add( e.seperator().toString() );
					segments.add( e.toSqlQuery() );
					sqlValues.addAll( e.values().collect( Collectors.toList() ) );
				}
			}

			if ( elegantQuery.getLimit() > 0 )
				segments.add( "LIMIT " + elegantQuery.getLimit() );

			if ( elegantQuery.getOffset() > 0 )
				segments.add( "OFFSET " + elegantQuery.getOffset() );

			try
			{
				String query = Joiner.on( " " ).join( segments ) + ";";
				return new SQLQueryCache( this, elegantQuery, query, query( query, true, isDebugQuery, sqlValues ) );
			}
			catch ( SQLException e )
			{
				throw new DatabaseException( e );
			}
		}
	}

	public SQLQueryCache execute( ElegantQueryInsert elegantQuery, boolean isDebugQuery ) throws DatabaseException
	{
		synchronized ( elegantQuery )
		{
			List<String> segments = new LinkedList<>();

			segments.add( "INSERT INTO" );

			segments.add( Strs.wrap( elegantQuery.getTable(), '`' ) );

			Map<String, Object> sqlValues = elegantQuery.getValues();
			segments.add( String.format( "(%s) VALUES (%s)", Joiner.on( ", " ).join( Strs.wrap( sqlValues.keySet(), '`' ) ), Joiner.on( ", " ).join( Strs.repeatToList( "?", sqlValues.values().size() ) ) ) );

			try
			{
				String query = Joiner.on( " " ).join( segments ) + ";";
				return new SQLQueryCache( this, elegantQuery, query, query( query, true, isDebugQuery, null ) );
			}
			catch ( SQLException e )
			{
				throw new DatabaseException( e );
			}
		}
	}

	public SQLQueryCache execute( ElegantQuerySelect elegantQuery, boolean isDebugQuery, boolean resultCount ) throws DatabaseException
	{
		synchronized ( elegantQuery )
		{
			List<String> segments = new LinkedList<>();
			List<Object> sqlValues = new LinkedList<>();

			segments.add( "SELECT" );

			if ( resultCount )
				segments.add( "COUNT(*)" );
			else if ( elegantQuery.getFields().size() == 0 )
				segments.add( "*" );
			else
				segments.add( Strs.join( Strs.wrap( elegantQuery.getFields(), '`' ), ", " ) );

			segments.add( "FROM" );

			segments.add( Strs.wrap( elegantQuery.getTable(), '`' ) );

			if ( elegantQuery.getElements().size() > 0 )
			{
				segments.add( "WHERE" );

				for ( WhereItem e : elegantQuery.getElements() )
				{
					if ( e.seperator() != WhereItem.Divider.NONE && e != elegantQuery.getElements().get( 0 ) )
						segments.add( e.seperator().toString() );
					segments.add( e.toSqlQuery() );
					sqlValues.addAll( e.values().collect( Collectors.toList() ) );
				}
			}

			if ( elegantQuery.getGroupBy().size() > 0 )
				segments.add( "GROUP BY " + Joiner.on( ", " ).join( Strs.wrap( elegantQuery.getGroupBy(), '`' ) ) );

			if ( elegantQuery.isOrderRand() )
				segments.add( "ORDER BY RAND()" );
			else if ( elegantQuery.getOrderBy().size() > 0 )
				segments.add( "ORDER BY " + Joiner.on( ", " ).join( Strs.wrap( elegantQuery.getOrderBy(), '`' ) ) + ( elegantQuery.isOrderAscending() ? " ASC" : " DESC" ) );

			if ( elegantQuery.getLimit() > 0 )
				segments.add( "LIMIT " + elegantQuery.getLimit() );

			if ( elegantQuery.getOffset() > 0 )
				segments.add( "OFFSET " + elegantQuery.getOffset() );

			try
			{
				String query = Strs.join( segments, " " ) + ";";
				return new SQLQueryCache( this, elegantQuery, query, query( query, false, isDebugQuery, sqlValues ) );
			}
			catch ( SQLException e )
			{
				throw new DatabaseException( e );
			}
		}
	}

	public SQLQueryCache execute( ElegantQueryTable elegantQuery, boolean isDebugQuery, boolean resultCount ) throws DatabaseException
	{
		synchronized ( elegantQuery )
		{
			List<String> segments = new LinkedList<>();

			segments.add( "SELECT" );

			if ( resultCount )
				segments.add( "COUNT(*)" );
			else
				segments.add( "*" );

			segments.add( "FROM" );

			segments.add( Strs.wrap( elegantQuery.getTable(), '`' ) );

			try
			{
				String query = Strs.join( segments, " " ) + ";";
				return new SQLQueryCache( this, elegantQuery, query, query( query, false, isDebugQuery, null ) );
			}
			catch ( SQLException e )
			{
				throw new DatabaseException( e );
			}
		}
	}

	public SQLQueryCache execute( ElegantQueryUpdate elegantQuery, boolean isDebugQuery ) throws DatabaseException
	{
		synchronized ( elegantQuery )
		{
			List<String> segments = new LinkedList<>();
			List<Object> sqlValues = new LinkedList<>();

			if ( elegantQuery.getValues().size() == 0 )
				throw new IllegalStateException( "Invalid Query State: There are no values to be updated" );

			segments.add( "UPDATE" );

			segments.add( Strs.wrap( elegantQuery.getTable(), '`' ) );

			segments.add( "SET" );

			List<String> sets = new LinkedList<>();

			for ( String key : elegantQuery.getValues().keySet() )
				sets.add( String.format( "`%s` = ?", key ) );

			sqlValues.addAll( elegantQuery.getValues().values() );

			segments.add( Joiner.on( ", " ).join( sets ) );

			if ( elegantQuery.getElements().size() > 0 )
			{
				segments.add( "WHERE" );

				for ( WhereItem e : elegantQuery.getElements() )
				{
					if ( e.seperator() != WhereItem.Divider.NONE && e != elegantQuery.getElements().get( 0 ) )
						segments.add( e.seperator().toString() );
					segments.add( e.toSqlQuery() );
					sqlValues.addAll( e.values().collect( Collectors.toList() ) );
				}
			}

			if ( elegantQuery.getLimit() > 0 )
				segments.add( "LIMIT " + elegantQuery.getLimit() );

			try
			{
				String query = Joiner.on( " " ).join( segments ) + ";";
				return new SQLQueryCache( this, elegantQuery, query, query( query, true, isDebugQuery, sqlValues ) );
			}
			catch ( SQLException e )
			{
				throw new DatabaseException( e );
			}
		}
	}

	public DatabaseMetaData getMetaData() throws SQLException
	{
		return connection.getMetaData();
	}

	@Override
	public boolean isConnected()
	{
		try
		{
			return connection != null && !connection.isClosed();
		}
		catch ( SQLException e )
		{
			return false;
		}
	}

	/**
	 * Executes the provided string query with the SQL Database Server
	 *
	 * @param sqlQuery The string query to be executed
	 * @param isUpdate Is this an update to the database, e.g., Delete, Insert, or Update.
	 * @param values   The update/insert and where values to be used.
	 *
	 * @return The Java SQL Statement
	 *
	 * @throws SQLException Something went wrong, duh!
	 */
	public PreparedStatement query( String sqlQuery, boolean isUpdate, boolean isDebugQuery, List<Object> values ) throws SQLException
	{
		try
		{
			if ( connection == null )
				throw new SQLException( "The SQL connection was never opened." );

			// stmt = con.prepareStatement( query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
			PreparedStatement stmt = connection.prepareStatement( sqlQuery );

			int x = 0;
			String valuesString = values == null ? "" : " with values [" + Joiner.on( ", " ).join( values ) + "]";
			if ( values != null )
				for ( Object s : values )
					if ( s != null )
						try
						{
							x++;
							stmt.setObject( x, s );
						}
						catch ( SQLException e )
						{
							if ( e.getCause() instanceof NotSerializableException )
								Database.L.severe( "The object " + s.getClass() + " (" + s.toString() + ") is not serializable!" );

							if ( !e.getMessage().startsWith( "Parameter index out of range" ) )
								throw e;

							if ( Kernel.isDevelopment() )
								e.printStackTrace();
						}
						catch ( ArrayIndexOutOfBoundsException e )
						{
							Database.L.warning( String.format( "SQL Query '%s' is lacking replace points (?) to satisfy the argument '%s', index '%s'", sqlQuery, s, x ) );
						}

			try
			{
				if ( isUpdate )
					stmt.executeUpdate();
				else
					stmt.execute();
			}
			catch ( SQLException e )
			{
				if ( isDebugEnabled() || isDebugQuery )
					Database.L.severe( "SQL query \"" + sqlQuery + "\" failed" + valuesString + ", message: " + e.getMessage() );
				throw e;
			}

			if ( isDebugEnabled() || isDebugQuery )
				if ( isUpdate )
					Database.L.fine( "SQL query \"" + sqlQuery + "\"" + valuesString + " " + ( isUpdate ? "affected" : "returned" ) + " " + stmt.getUpdateCount() + " rows" );
				else
					Database.L.fine( "SQL query \"" + sqlQuery + "\"" + valuesString );

			return stmt;
		}
		catch ( CommunicationsException | MySQLNonTransientConnectionException e )
		{
			if ( autoReconnect )
			{
				reconnect();
				return query( sqlQuery, isUpdate, isDebugQuery, values );
			}
			else
				throw e;
		}
	}

	protected abstract void reconnect() throws SQLException;

	@Override
	public void tableColumnCreate( @Nonnull String table, @Nonnull String columnName, @Nonnull ColumnType columnType, @Nonnegative int columnLength, @Nullable Object columnDef ) throws DatabaseException
	{
		Triplet<Integer, Class, String> triplet = COLUMN_COMPATIBILITY.get( columnType );
		if ( triplet == null )
			throw new IllegalArgumentException( columnType.name() + " is not supported by this driver!" );
		if ( triplet.getLeft() > 0 && columnLength > triplet.getLeft() )
			throw new DatabaseException( "Var can't be longer than 256 characters in length, use TEXT instead." );
		if ( columnDef != null && Objs.length( columnDef ) > columnLength )
			throw new DatabaseException( "Default exceeds max length" );

		String defString = columnDef == null ? "NULL" : "NOT NULL DEFAULT '" + Objs.castToString( columnDef ) + "'";

		String typeString = triplet.getRight();

		if ( columnLength > 0 )
			typeString += "(" + columnLength + ")";

		try
		{
			if ( tableExists( table ) )
			{
				// Column already exists!
				if ( tableColumnsList( table ).contains( columnName ) )
					return;

				query( String.format( "ALTER TABLE `%s` ADD `%s` %s %s;", table, columnName, typeString, defString ), true, false, null );
			}
			else
				query( String.format( "CREATE TABLE `%s` ( `%s` %s %s );", table, columnName, typeString, defString ), true, false, null );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	@Override
	public void tableColumnDrop( String table, String colName ) throws DatabaseException
	{
		try
		{
			query( String.format( "ALTER TABLE `%s` DROP `%s`;", table, colName ), true, false, null );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	@Override
	public List<String> tableColumnsList( String table ) throws DatabaseException
	{
		try
		{
			return new SQLTableMeta( this, table ).columnNames();
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	public List<String> tableColumnsRequired( String table ) throws DatabaseException
	{
		try
		{
			return new SQLTableMeta( this, table ).columnNamesRequired();
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	@Override
	public void tableDrop( String table ) throws DatabaseException
	{
		try
		{
			query( String.format( "DROP TABLE `%s` IF EXISTS;", table ), true, false, null );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	@Override
	public boolean tableExists( String table ) throws DatabaseException
	{
		try
		{
			return new SQLTableMeta( this, table ).exists();
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	@Override
	public String tableIndexKey( String table ) throws DatabaseException
	{
		try
		{
			ResultSet rs = query( String.format( "SHOW INDEX FROM `%s`", table ), false, false, null ).getResultSet();
			if ( !rs.first() )
				return null;
			return rs.getString( "Column_name" );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	@Override
	public String tablePrimaryKey( String table ) throws DatabaseException
	{
		try
		{
			ResultSet rs = query( String.format( "SHOW KEYS FROM `%s` WHERE `Key_name` = 'PRIMARY';", table ), false, false, null ).getResultSet();
			if ( !rs.first() )
				return null;
			return rs.getString( "Column_name" );
		}
		catch ( SQLException e )
		{
			throw new DatabaseException( e );
		}
	}

	abstract class ConnectionBuilder implements DatabaseDriver.ConnectionBuilder
	{
		public Connection connect( String connectionString, String user, String pass ) throws SQLException
		{
			Connection connection = DriverManager.getConnection( connectionString, user, pass );
			connection.setAutoCommit( true );
			return connection;
		}

		public Connection connect( String connectionString ) throws SQLException
		{
			Connection connection = DriverManager.getConnection( connectionString );
			connection.setAutoCommit( true );
			return connection;
		}
	}
}
