package io.amelia.storage.backend;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageRegistry;
import io.amelia.support.LocalBoolean;

public class StorageBackend implements Comparable<StorageBackend>
{
	private final LocalBoolean DEBUG_ENABLED = new LocalBoolean( false );
	private final SQLStorageBackend.Builder builder;
	private StorageRegistry.BackendType type;

	public StorageBackend( SQLStorageBackend.Builder builder, StorageRegistry.BackendType type ) throws StorageException.Error
	{
		this.builder = builder;
		this.type = type;
	}

	@Override
	public boolean equals( Object obj )
	{
		return obj instanceof StorageBackend ? compareTo( ( StorageBackend ) obj ) == 0 : super.equals( obj );
	}

	@Override
	public int compareTo( StorageBackend other )
	{
		return getPrefix().compareTo( other.getPrefix() );
	}

	public String getPrefix()
	{
		return builder.mountPoint;
	}

	SQLStorageBackend.Builder getBuilder()
	{
		return builder;
	}

	public StorageRegistry.BackendType getType()
	{
		return type;
	}

	public boolean isDebugEnabled()
	{
		return DEBUG_ENABLED.get( this );
	}

	public void setDebugEnabled( boolean debugEnabled )
	{
		DEBUG_ENABLED.set( this, debugEnabled );
	}

	abstract class Builder<Extended extends Builder>
	{
		String mountPoint;

		public Extended setMountPoint( String mountPoint ) throws StorageException.Error
		{
			this.mountPoint = mountPoint;
			return ( Extended ) this;
		}

		abstract <T extends StorageBackend> T connect() throws StorageException.Error;

		public void validate() throws StorageException.Error
		{
			if ( !mountPoint.matches( "[a-z0-9]+" ) )
				throw new StorageException.Error( "Backend prefix must only contain lowercase letters and numbers." );
		}
	}
}
