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

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;

public class FileStorageBackend extends StorageBackend
{
	private final Path path;

	/**
	 * @param path      The real system path on the file system
	 * @param mountPath The path to mount the new backend
	 * @param type      The backend type
	 *
	 * @throws StorageException.Error
	 */
	public FileStorageBackend( Path path, NodePath mountPath, HoneyStorage.BackendType type ) throws StorageException.Error
	{
		// super( path, mountPath, type );
		super( mountPath, type );

		this.path = path;
	}

	public FileStorageBackend( Path path, NodePath mountPath ) throws StorageException.Error
	{
		this( path, mountPath, HoneyStorage.BackendType.DEFAULT );
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
	public Path getRootPath()
	{
		return path;
		// return new HoneyPath( FileSystems.getDefault(), path );
	}

	@Override
	public boolean isHidden( NodePath path )
	{
		return false;
	}
}
