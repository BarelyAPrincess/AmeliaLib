/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.drivers;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.ReqlExpr;
import com.rethinkdb.net.Connection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import io.amelia.database.Database;
import io.amelia.database.cache.QueryCache;
import io.amelia.database.cache.RethinkQueryCache;
import io.amelia.database.elegant.ElegantQueryDelete;
import io.amelia.database.elegant.ElegantQueryInsert;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.database.elegant.ElegantQueryTable;
import io.amelia.database.elegant.ElegantQueryUpdate;
import io.amelia.database.elegant.ElegantQuery;
import io.amelia.database.elegant.types.WhereItem;
import io.amelia.database.support.ColumnType;
import io.amelia.lang.DatabaseException;

/**
 * Provides driver methods for RethinkDB
 */
public class RethinkDBDriver implements DatabaseDriver
{
	public static final RethinkDB r = RethinkDB.r;

	private Connection conn = null;

	public ConnectionBuilder builder()
	{
		return new ConnectionBuilder();
	}

	@Override
	public QueryCache execute( ElegantQuery query, boolean isDebugQuery ) throws DatabaseException
	{
		if ( query instanceof ElegantQueryDelete )
			return execute( ( ElegantQueryDelete ) query, isDebugQuery );
		else if ( query instanceof ElegantQueryInsert )
			return execute( ( ElegantQueryInsert ) query, isDebugQuery );
		else if ( query instanceof ElegantQuerySelect )
			return execute( ( ElegantQuerySelect ) query, isDebugQuery );
		else if ( query instanceof ElegantQueryTable )
			return execute( ( ElegantQueryTable ) query, isDebugQuery );
		else if ( query instanceof ElegantQueryUpdate )
			return execute( ( ElegantQueryUpdate ) query, isDebugQuery );
		else
			throw new DatabaseException( "Unsupported Query. You should report this to the developer. " + query.getClass().getSimpleName() );
	}

	public RethinkQueryCache execute( ElegantQueryDelete elegantQuery, boolean isDebugQuery ) throws DatabaseException
	{
		synchronized ( elegantQuery )
		{
			ReqlExpr q = r.table( elegantQuery.getTable() );

			if ( elegantQuery.getElements().size() > 0 )
			{
				q = q.filter( ( d ) -> {
					return d;
				} );
			}
			for ( WhereItem e : elegantQuery.getElements() )
			{
				if ( e instanceof WhereItem.Group )
				{

				}
				else if ( e instanceof WhereItem.KeyValue )
				{
					q.filter( r.hashMap( ( ( WhereItem.KeyValue ) e ).key(), ( ( WhereItem.KeyValue ) e ).values() ) );
				}
						/*
						if ( e.seperator() != WhereItem.Divider.NONE && e != elegantQuery.getElements().get( 0 ) )
							segments.add( e.seperator().toString() );
						segments.add( e.toSqlQuery() );
						sqlValues.addAll( e.values().collect( Collectors.toList() ) );*/
			}

			if ( elegantQuery.getOffset() > 0 )
				q = q.skip( elegantQuery.getOffset() );

			if ( elegantQuery.getLimit() > 0 )
				q = q.limit( elegantQuery.getLimit() );

			try
			{
				return new RethinkQueryCache( this, elegantQuery, q.run( conn ) );
			}
			catch ( Exception e )
			{
				throw new DatabaseException( e );
			}
		}
	}

	@Override
	public boolean isConnected()
	{
		return conn != null && conn.isOpen();
	}

	@Override
	public void tableColumnCreate( String table, String columnName, ColumnType columnType, int columnLength, Object columnDef ) throws DatabaseException
	{
		// TODO
	}

	@Override
	public void tableColumnDrop( String table, String columnName ) throws DatabaseException
	{
		r.table( table ).replace( r.row().without( columnName ) ).run( conn );
	}

	@Override
	public List<String> tableColumnsList( String table ) throws DatabaseException
	{
		// TODO
		return new ArrayList<>();
	}

	@Override
	public List<String> tableColumnsRequired( String table ) throws DatabaseException
	{
		/* Due to the nature of RethinkDB, there are no required columns */
		return new ArrayList<>();
	}

	@Override
	public void tableDrop( String table ) throws DatabaseException
	{
		r.tableDrop( table ).run( conn );
	}

	@Override
	public boolean tableExists( String table ) throws DatabaseException
	{
		Object result = r.table( table ).run( conn );

		Database.L.debug( "Got result: " + result.getClass() + " -- " + result );

		return result != null;
	}

	@Override
	public String tableIndexKey( String table ) throws DatabaseException
	{
		return r.table( table ).indexList().run( conn );
	}

	@Override
	public String tablePrimaryKey( String table ) throws DatabaseException
	{
		return "id"; // ID is the default primary key
	}

	public class ConnectionBuilder implements DatabaseDriver.ConnectionBuilder
	{
		private Connection.Builder builder = r.connection().port( 28015 );

		public ConnectionBuilder authKey( String authKey )
		{
			builder.authKey( authKey );
			return this;
		}

		public ConnectionBuilder certFile( InputStream inputStream )
		{
			builder.certFile( inputStream );
			return this;
		}

		@Override
		public RethinkDBDriver connect()
		{
			RethinkDBDriver.this.conn = builder.connect();
			return RethinkDBDriver.this;
		}

		public ConnectionBuilder db( String val )
		{
			builder.db( val );
			return this;
		}

		public ConnectionBuilder hostname( String hostname )
		{
			builder.hostname( hostname );
			return this;
		}

		public ConnectionBuilder port( int port )
		{
			builder.port( port );
			return this;
		}

		public ConnectionBuilder sslContext( SSLContext val )
		{
			builder.sslContext( val );
			return this;
		}

		public ConnectionBuilder timeout( long val )
		{
			builder.timeout( val );
			return this;
		}

		public ConnectionBuilder user( String user, String password )
		{
			builder.user( user, password );
			return this;
		}
	}
}
