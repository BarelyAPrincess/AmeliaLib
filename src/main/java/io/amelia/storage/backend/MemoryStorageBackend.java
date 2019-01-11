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

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;

public class MemoryStorageBackend extends StorageBackend
{
	public MemoryStorageBackend( NodePath mountPath ) throws StorageException.Error
	{
		super( mountPath, HoneyStorage.BackendType.DEFAULT );
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
