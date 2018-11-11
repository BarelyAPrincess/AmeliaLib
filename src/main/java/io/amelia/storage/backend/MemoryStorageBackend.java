package io.amelia.storage.backend;

import io.amelia.lang.StorageException;
import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;

public class MemoryStorageBackend extends StorageBackend
{
	public MemoryStorageBackend() throws StorageException.Error
	{
		super( NodePath.empty(), HoneyStorage.BackendType.DEFAULT );
	}
}
