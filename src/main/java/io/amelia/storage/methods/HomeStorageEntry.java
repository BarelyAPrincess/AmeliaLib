package io.amelia.storage.methods;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageContainerEntry;
import io.amelia.storage.StorageContainerPolicy;
import io.amelia.storage.StorageContext;

public class HomeStorageEntry extends StorageContainerEntry
{
	public HomeStorageEntry( @Nonnull StorageContext storageContext, @Nonnull StorageContainerPolicy storageTemplate ) throws StorageException.Error
	{
		super( storageContext );

		storageTemplate.enforcePolicy( this );
	}
}
