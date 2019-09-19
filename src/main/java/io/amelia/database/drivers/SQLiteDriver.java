/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.drivers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import io.amelia.database.DatabaseManager;
import io.amelia.foundation.Kernel;
import io.amelia.lang.StartupException;
import io.amelia.support.IO;

/**
 * SQLite Implementation
 */
public class SQLiteDriver extends SQLBaseDriver
{
	static
	{
		try
		{
			Class.forName( "org.sqlite.JDBC" );
		}
		catch ( ClassNotFoundException e )
		{
			throw new StartupException( "We could not locate the 'org.sqlite.JDBC' library, be sure to have this library in your build path." );
		}
	}

	private ConnectionBuilder lastBuilder = null;

	public SQLiteDriver.ConnectionBuilder builder()
	{
		return new SQLiteDriver.ConnectionBuilder();
	}

	@Override
	protected void reconnect() throws SQLException
	{
		if ( lastBuilder == null )
			throw new SQLException( "The SQL connection was never opened." );
		lastBuilder.connect();
	}

	public class ConnectionBuilder extends SQLBaseDriver.ConnectionBuilder
	{
		private Path dbFile;

		@Override
		public SQLiteDriver connect() throws SQLException
		{
			if ( Files.notExists( dbFile ) )
			{
				DatabaseManager.L.warning( "The SQLite file \"" + dbFile.toString() + "\" did not exist, we will attempt to create an empty file." );
				try
				{
					Files.createFile( dbFile );
				}
				catch ( IOException e )
				{
					throw new StartupException( "We had a problem creating the SQLite file, the exact exception message was: " + e.getMessage(), e );
				}
			}

			super.connect( "jdbc:sqlite:" + dbFile.toString() );
			SQLiteDriver.this.lastBuilder = this;
			return SQLiteDriver.this;
		}

		public SQLiteDriver.ConnectionBuilder file( String dbFile )
		{
			this.dbFile = Kernel.getPath( Kernel.PATH_STORAGE ).resolve( dbFile );
			return this;
		}

		public SQLiteDriver.ConnectionBuilder file( Path dbFile )
		{
			this.dbFile = Kernel.getPath( Kernel.PATH_STORAGE ).resolve( dbFile );
			return this;
		}
	}
}
