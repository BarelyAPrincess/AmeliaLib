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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.drivers.H2Driver;
import io.amelia.database.drivers.MySQLDriver;
import io.amelia.database.drivers.RethinkDBDriver;
import io.amelia.database.drivers.SQLiteDriver;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.StartupException;
import io.amelia.support.Lists;
import io.amelia.support.Objs;

/**
 * Manages the Storage and Database connections and methods
 */
public class DatabaseManager
{
	public static final Kernel.Logger L = Kernel.getLogger( DatabaseManager.class );

	private static List<DatabaseManager> instances = new ArrayList<>();

	public static DatabaseManager getDefault()
	{
		return Lists.findOrNew( instances, DatabaseManager::isDefault, () -> new DatabaseManager( "default" ) );
	}

	public static DatabaseManager getInstance( @Nonnull String id )
	{
		if ( "default".equalsIgnoreCase( id ) || id.length() == 0 )
			throw new ApplicationException.Runtime( "Instance ID must not equal \"default\" or be empty." );
		return Lists.findOrNew( instances, mgr -> id.equalsIgnoreCase( mgr.id ), () -> new DatabaseManager( id ) );
	}

	public static DatabaseDriver makeDatabaseDriver( ConfigData section )
	{
		DatabaseDriver databaseDriver = null;

		try
		{
			String type = section.getString( "type" ).orElse( "sqlite" ).toLowerCase();
			if ( Objs.isEmpty( type ) || type.equals( "none" ) )
				L.warning( "The Server Database is not configured, some features may not function as intended. See config option 'server.database.type' in server config 'server.yaml'." );
			else if ( type.equals( "sqlite" ) )
				databaseDriver = new SQLiteDriver().builder().file( section.getString( "dbfile" ).orElse( "server.db" ) ).connect();
			else if ( type.equals( "h2" ) )
				databaseDriver = new H2Driver().builder().file( section.getString( "dbfile" ).orElse( "server.db" ) ).connect();
			else if ( type.equals( "mysql" ) )
			{
				String host = section.getString( "host" ).orElse( "localhost" );
				int port = section.getInteger( "port" ).orElse( 3306 );
				String database = section.getString( "database" ).orElse( "server" );
				String user = section.getString( "username" ).orElse( "fwuser" );
				String password = section.getString( "password" ).orElse( "fwpass" );

				databaseDriver = new MySQLDriver().builder().hostname( host ).port( port ).db( database ).user( user, password ).connect();
			}
			else if ( type.equals( "rethink" ) )
			{
				String host = section.getString( "host" ).orElse( "localhost" );
				int port = section.getInteger( "port" ).orElse( 28015 );
				String database = section.getString( "database" ).orElse( "server" );
				String user = section.getString( "username" ).orElse( "fwuser" );
				String password = section.getString( "password" ).orElse( "fwpass" );

				databaseDriver = new RethinkDBDriver().builder().hostname( host ).port( port ).db( database ).user( user, password ).connect();
			}

			if ( databaseDriver == null )
				L.severe( "Unrecognized database type \"" + section.getString( "type" ) + "\", presently only mysql, sqlite, h2, and rethink are supported. Please update your configuration." );
			else
				L.info( "We successfully connected to the database with database type \"" + type + "\"." );
		}
		catch ( SQLException e )
		{
			throw new StartupException( e );
		}

		return databaseDriver;
	}

	private DatabaseDriver database;
	private String id;

	public DatabaseManager( String id )
	{
		this.id = id;
	}

	public Database getDatabase()
	{
		if ( database == null )
			init();
		return new Database( database );
	}

	public DatabaseManager init()
	{
		database = makeDatabaseDriver( ConfigRegistry.config.getChildOrCreate( "compat.database" ) );
		return this;
	}

	public DatabaseManager init( ConfigData section )
	{
		database = makeDatabaseDriver( section );
		return this;
	}

	public boolean isDefault()
	{
		return "default".equalsIgnoreCase( id );
	}

	public enum DatabaseDriverType
	{
		RethinkDB,
		File_Config,
		File_Table,
		MySql,
		SQLite,
		H2
	}
}
