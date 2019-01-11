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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;

public class SQLStorageBackend extends StorageBackend
{
	Connection connection;
	SQLMeta meta;

	SQLStorageBackend( @Nonnull SQLMeta meta, @Nonnull NodePath mountPath, @Nonnull HoneyStorage.BackendType type ) throws StorageException.Error
	{
		super( mountPath, type );

		this.meta = meta;
		connect();
	}

	protected void connect() throws StorageException.Error
	{
		try
		{
			connection = DriverManager.getConnection( meta.getConnectionString(), meta.getUser(), meta.getPass() );
			connection.setAutoCommit( true );
		}
		catch ( SQLException e )
		{
			throw new StorageException.Error( e );
		}
	}

	@Override
	public void createDirectory( NodePath path )
	{

	}

	@Override
	public void delete( NodePath path )
	{

	}

	@Override
	public boolean isHidden( NodePath path )
	{
		return false;
	}

	public class SQLMeta
	{
		String connectionString;
		String pass = null;
		String user = null;

		public String getConnectionString()
		{
			return connectionString;
		}

		public String getPass()
		{
			return pass;
		}

		public String getUser()
		{
			return user;
		}

		public SQLMeta setConnectionString( String connectionString )
		{
			this.connectionString = connectionString;
			return this;
		}

		public SQLMeta setPassword( String pass )
		{
			this.pass = pass;
			return this;
		}

		public SQLMeta setUsername( String user )
		{
			this.user = user;
			return this;
		}
	}
}
