package io.amelia.storage.backend;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.storage.MountPoint;
import io.amelia.storage.HoneyPath;
import io.amelia.support.NodePath;

public abstract class StorageBackend
{
	private final SQLStorageBackend.Builder builder;
	private boolean debugEnabled = false;
	private HoneyStorage.BackendType type;

	public StorageBackend( SQLStorageBackend.Builder builder, HoneyStorage.BackendType type ) throws StorageException.Error
	{
		this.builder = builder;
		this.type = type;
	}

	SQLStorageBackend.Builder getBuilder()
	{
		return builder;
	}

	public NodePath getMountPath()
	{
		return builder.mountPath;
	}

	public MountPoint getMountPoint( @Nonnull NodePath subPath )
	{
		return new MountPoint( this, getMountPath(), subPath );
	}

	public HoneyPath getRootPath()
	{

	}

	public HoneyStorage.BackendType getType()
	{
		return type;
	}

	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}

	public MountPoint matches( @Nonnull NodePath origFullPath )
	{
		if ( !origFullPath.startsWith( getMountPath() ) )
			return null;
		return new MountPoint( this, getMountPath(), origFullPath.subNodes( getMountPath().getNodeCount() ) );
	}

	public void setDebugEnabled( boolean debugEnabled )
	{
		this.debugEnabled = debugEnabled;
	}
}
