package io.amelia.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;

public interface StorageMapper
{
	@Nullable
	StorageEntry mapStorageContext( @Nonnull StorageContext storageContext ) throws StorageException.Error;
}
