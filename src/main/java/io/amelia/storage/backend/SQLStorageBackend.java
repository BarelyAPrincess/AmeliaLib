package io.amelia.storage.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.amelia.lang.StorageException;
import io.amelia.support.Objs;

public class SQLStorageBackend extends StorageBackend
{
	SQLStorageBackend( Builder builder ) throws StorageException.Error
	{
		super( builder );
	}

	abstract class AbstractBuilder<Extended extends AbstractBuilder> extends StorageBackend.Builder<Extended>
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

	public final class Builder extends AbstractBuilder<Builder>
	{
		String connectionString;

		@Override
		public SQLStorageBackend connect() throws StorageException.Error
		{
			validate();
			abstractConnect();
			return new SQLStorageBackend( mountPoint, this );
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
