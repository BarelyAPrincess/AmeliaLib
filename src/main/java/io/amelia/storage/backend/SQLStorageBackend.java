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

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;
import io.amelia.support.Objs;

public class SQLStorageBackend extends StorageBackend
{
	SQLStorageBackend( AbstractBuilder builder, HoneyStorage.BackendType type ) throws StorageException.Error
	{
		super( builder, type );
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

	abstract class AbstractBuilder<Extended extends AbstractBuilder> extends io.amelia.storage.backend.AbstractBuilder
	{
		Connection connection;
		String pass = null;
		String user = null;

		void abstractConnect() throws StorageException.Error
		{
			try
			{
				connection = DriverManager.getConnection( getConnectionString(), user, pass );
				connection.setAutoCommit( true );
			}
			catch ( SQLException e )
			{
				throw new StorageException.Error( e );
			}
		}

		abstract String getConnectionString();

		public Extended setPassword( String pass )
		{
			this.pass = pass;
			return ( Extended ) this;
		}

		@Override
		public void validate() throws StorageException.Error
		{
			super.validate();
			Objs.notEmpty( getConnectionString(), "Connection string was not specified." );
		}

		public Extended setUsername( String user )
		{
			this.user = user;
			return ( Extended ) this;
		}


	}

	public final class Builder extends AbstractBuilder
	{
		String connectionString;

		@Override
		public SQLStorageBackend init() throws StorageException.Error
		{
			validate();
			abstractConnect();
			return new SQLStorageBackend( this, HoneyStorage.BackendType.DEFAULT );
		}

		@Override
		String getConnectionString()
		{
			return connectionString;
		}

		public void setConnectionString( String connectionString )
		{
			this.connectionString = connectionString;
		}
	}
}
