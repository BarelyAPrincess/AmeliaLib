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

import java.nio.file.Path;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyPath;
import io.amelia.storage.HoneyStorage;
import io.amelia.storage.MountPoint;
import io.amelia.support.NodePath;

public abstract class StorageBackend
{
	private final NodePath mountPath;
	private boolean debugEnabled = false;
	private HoneyStorage.BackendType type;

	public StorageBackend( @Nonnull NodePath mountPath, @Nonnull HoneyStorage.BackendType type ) throws StorageException.Error
	{
		this.mountPath = mountPath;
		this.type = type;
	}

	public abstract void createDirectory( NodePath path );

	public abstract void delete( NodePath path );

	public NodePath getMountPath()
	{
		return mountPath;
	}

	public MountPoint getMountPoint( @Nonnull NodePath subPath )
	{
		return new MountPoint( this, getMountPath(), subPath );
	}

	public Path getRootPath()
	{
		// TODO
		return null;
	}

	public HoneyStorage.BackendType getType()
	{
		return type;
	}

	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}

	public abstract boolean isHidden( NodePath path );

	public MountPoint matches( @Nonnull Path origFullPath )
	{
		if ( !origFullPath.startsWith( getMountPath() ) )
			return null;
		return new MountPoint( this, getMountPath(), NodePath.of( origFullPath.subpath( getMountPath().getNodeCount(), origFullPath.getNameCount() ) ) );
	}

	public void setDebugEnabled( boolean debugEnabled )
	{
		this.debugEnabled = debugEnabled;
	}
}
