package io.amelia.storage;

import java.io.IOException;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;

public class DefaultStorageMapper implements StorageMapper
{
	@Nonnull
	@Override
	public StorageEntry mapStorageContext( StorageContext storageContext ) throws StorageException.Error
	{
		try
		{
			return storageContext.isContainer() ? new StorageContainerEntry( storageContext ) : new StorageObjectEntry( storageContext );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}
	}
}
