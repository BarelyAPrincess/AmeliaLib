package io.amelia.storage.backend;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.StorageException;
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

	public MySQLStorageBackend( Builder builder ) throws StorageException.Error
	{
		super( builder );
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
			return new MySQLStorageBackend( this );
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
