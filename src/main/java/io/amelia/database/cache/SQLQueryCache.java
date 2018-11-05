/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.cache;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.amelia.database.drivers.SQLBaseDriver;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.database.elegant.ElegantQuery;
import io.amelia.database.elegant.StringBasedQuery;
import io.amelia.database.support.UtilDatabase;
import io.amelia.lang.DatabaseException;

public class SQLQueryCache implements StringBasedQuery, QueryCache
{
	private SQLBaseDriver driver;
	private ElegantQuery elegantQuery;
	private boolean firstCall = true;
	private String query;
	private ResultSet resultSetCache = null;
	private PreparedStatement stmt;

	public SQLQueryCache( SQLBaseDriver driver, ElegantQuery elegantQuery, String query, PreparedStatement stmt )
	{
		this.driver = driver;
		this.elegantQuery = elegantQuery;
		this.query = query;
		this.stmt = stmt;
	}

	public void close() throws SQLException
	{
		if ( stmt != null && !stmt.isClosed() )
			stmt.close();
	}

	@Override
	public int count()
	{
		try
		{
			if ( elegantQuery instanceof ElegantQuerySelect )
			{
				ResultSet rs = driver.execute( ( ElegantQuerySelect ) elegantQuery, true ).getResultSet();
				rs.next();
				return rs.getInt( 1 );
			}
			else
				return stmt.getUpdateCount();
		}
		catch ( NullPointerException | SQLException | DatabaseException e )
		{
			e.printStackTrace();
			return -1;
		}
	}

	public PreparedStatement getPreparedStatement() throws SQLException
	{
		// if ( stmt == null || stmt.isClosed() )
		// stmt = driver.query( elegantQuery, !( elegantQuery instanceof ElegantQuerySelect ), isDebugEnabled(), elegantQuery.values() );
		return stmt;
	}

	public ResultSet getResultSet() throws SQLException
	{
		if ( resultSetCache == null )
		{
			resultSetCache = stmt.getResultSet();

			if ( resultSetCache == null )
				return null;

			if ( firstCall )
				// Not being before first, on your first call means no results
				// Next returning null also means no results
				if ( !resultSetCache.isBeforeFirst() || !resultSetCache.next() )
					return null;

			firstCall = false;
		}

		return resultSetCache;
	}

	@Override
	public String toStringQuery()
	{
		if ( query == null )
			return UtilDatabase.toString( stmt );
		else
			return query;
	}
}
