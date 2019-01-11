/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.backend;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;
import io.amelia.support.Strs;

public class MySQLStorageBackend extends SQLStorageBackend
{
	static
	{
		try
		{
			Class.forName( "com.mysql.jdbc.Driver" );
		}
		catch ( ClassNotFoundException e )
		{
			throw new ApplicationException.Runtime( "We could not locate the 'com.mysql.jdbc.Driver' library, be sure to have this library in your class path." );
		}
	}

	public MySQLStorageBackend( @Nonnull SQLMeta meta, @Nonnull NodePath mountPath, @Nonnull HoneyStorage.BackendType type ) throws StorageException.Error
	{
		super( meta, mountPath, type );
	}

	public class MySQLMeta extends SQLMeta
	{
		private String db;
		private String host;
		private String port = "3306";

		@Override
		public String getConnectionString()
		{
			return "jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useUnicode=yes";
		}

		public String getDb()
		{
			return db;
		}

		public String getHost()
		{
			return host;
		}

		public String getPort()
		{
			return port;
		}

		@Override
		public SQLMeta setConnectionString( String connectionString )
		{
			throw new IllegalArgumentException( "Not Applicable" );
		}

		public MySQLMeta setDb( String db )
		{
			this.db = db;
			return this;
		}

		public MySQLMeta setHost( String host )
		{
			this.host = host;
			return this;
		}

		public MySQLMeta setPort( String port )
		{
			this.port = Strs.notEmptyOrDef( port, "3306" );
			return this;
		}
	}
}
