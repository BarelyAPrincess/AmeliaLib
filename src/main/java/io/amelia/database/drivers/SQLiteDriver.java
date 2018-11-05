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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import io.amelia.database.Database;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
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
			throw new ApplicationException.Runtime( "We could not locate the 'org.sqlite.JDBC' library, be sure to have this library in your build path." );
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
		private Path path;

		@Override
		public SQLiteDriver connect() throws SQLException
		{
			if ( !Files.isRegularFile( path ) )
			{
				Database.L.warning( "The SQLite file '" + IO.relPath( path ) + "' did not exist or is not a regular file, we will attempt to create an empty file instead." );
				try
				{
					IO.deleteIfExists( path );
					Files.createFile( path );
				}
				catch ( IOException e )
				{
					throw new ApplicationException.Runtime( "We had a problem creating the SQLite file, the exact exception message was: " + e.getMessage(), e );
				}
			}

			super.connect( "jdbc:sqlite:" + path.toAbsolutePath() );
			SQLiteDriver.this.lastBuilder = this;
			return SQLiteDriver.this;
		}

		public SQLiteDriver.ConnectionBuilder file( String file )
		{
			Path filePath = Paths.get( file );
			return file( filePath.isAbsolute() ? filePath : Kernel.getPath().resolve( filePath ) );
		}

		public SQLiteDriver.ConnectionBuilder file( Path path )
		{
			this.path = path;
			return this;
		}
	}
}
