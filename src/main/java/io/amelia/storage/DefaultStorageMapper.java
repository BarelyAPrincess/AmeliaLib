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
			return storageContext.isContainer() ? new StorageEntryContainer( storageContext ) : new StorageEntry( storageContext );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}
	}
}
