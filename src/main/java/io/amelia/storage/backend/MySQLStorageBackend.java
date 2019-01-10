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

import io.amelia.lang.ApplicationException;
import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.Objs;
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

	private Builder lastBuilder = null;

	public MySQLStorageBackend( Builder builder, HoneyStorage.BackendType type ) throws StorageException.Error
	{
		super( builder, type );
	}

	protected MySQLStorageBackend reconnect() throws StorageException.Error
	{
		return lastBuilder.init();
	}

	public class Builder extends SQLStorageBackend.AbstractBuilder<Builder>
	{
		private String db;
		private String host;
		private String port = "3306";

		@Override
		public MySQLStorageBackend init() throws StorageException.Error
		{
			validate();
			abstractConnect();
			return new MySQLStorageBackend( this, HoneyStorage.BackendType.DEFAULT );
		}

		public Builder db( String db )
		{
			this.db = db;
			return this;
		}

		@Override
		String getConnectionString()
		{
			return "jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useUnicode=yes";
		}

		@Override
		public void validate() throws StorageException.Error
		{
			super.validate();
			Objs.notEmpty( host );
			Objs.notEmpty( db );
		}

		public Builder hostname( String host )
		{
			this.host = host;
			return this;
		}

		public Builder port( String port )
		{
			this.port = Strs.notEmptyOrDef( port, "3306" );
			return this;
		}
	}
}
