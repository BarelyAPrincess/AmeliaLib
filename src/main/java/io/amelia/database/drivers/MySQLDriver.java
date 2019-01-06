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

import io.amelia.lang.StartupException;

/**
 * MySQL Implementation
 */
public class MySQLDriver extends SQLBaseDriver
{
	static
	{
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
		}
		catch ( ClassNotFoundException e )
		{
			throw new StartupException( "We could not locate the 'com.mysql.jdbc.Driver' library, be sure to have this library in your build path." );
		}
	}

	private ConnectionBuilder lastBuilder = null;

	public MySQLDriver.ConnectionBuilder builder()
	{
		return new ConnectionBuilder();
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
		private String db;
		private String host;
		private String pass;
		private int port;
		private String user;

		@Override
		public MySQLDriver connect() throws SQLException
		{
			super.connect( "jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useUnicode=yes", user, pass );
			MySQLDriver.this.lastBuilder = this;
			return MySQLDriver.this;
		}

		public MySQLDriver.ConnectionBuilder db( String db )
		{
			this.db = db;
			return this;
		}

		public MySQLDriver.ConnectionBuilder hostname( String host )
		{
			this.host = host;
			return this;
		}

		public MySQLDriver.ConnectionBuilder port( int port )
		{
			this.port = port;
			return this;
		}

		public MySQLDriver.ConnectionBuilder user( String user, String password )
		{
			this.user = user;
			this.pass = password;
			return this;
		}
	}
}
