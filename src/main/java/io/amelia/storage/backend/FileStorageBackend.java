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
	public FileStorageBackend( Path path, NodePath mountPath, HoneyStorage.BackendType type ) throws StorageException.Error
	{
		// super( path, mountPath, type );
		super( null, type );
	}

	public FileStorageBackend( Path path, NodePath mountPath ) throws StorageException.Error
	{
		this( path, mountPath, HoneyStorage.BackendType.DEFAULT );
	}

	public FileStorageBackend( Path path ) throws StorageException.Error
	{
		this( path, NodePath.empty() );
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
}
