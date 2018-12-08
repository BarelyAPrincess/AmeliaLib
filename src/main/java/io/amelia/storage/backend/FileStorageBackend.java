/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.backend;

import java.nio.file.Path;

import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;

public class FileStorageBackend extends StorageBackend
{
	public FileStorageBackend( Path path, NodePath mountPath, HoneyStorage.BackendType type )
	{
		super( path, mountPath, type );
	}

	public FileStorageBackend( Path path, NodePath mountPath )
	{
		this( path, mountPath, HoneyStorage.BackendType.DEFAULT );
	}

	public FileStorageBackend( Path path )
	{
		this( path, NodePath.empty() );
	}
}
