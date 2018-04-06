package io.amelia.storage;

import io.amelia.lang.StorageException;

public interface StorageMapper
{
	StorageEntry mapStorageContext( StorageContext storageContext ) throws StorageException.Error;
}
