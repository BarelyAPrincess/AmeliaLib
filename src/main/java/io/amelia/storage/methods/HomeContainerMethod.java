/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.methods;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageContainerEntry;
import io.amelia.storage.StorageContainerPolicy;
import io.amelia.storage.StorageContainerTrait;
import io.amelia.storage.StorageContext;
import io.amelia.storage.StorageEntry;
import io.amelia.storage.StorageMapper;
import io.amelia.storage.StorageMethod;

public class HomeContainerMethod implements StorageMethod
{
	public final StorageContainerPolicy policy;

	public HomeContainerMethod( StorageContainerPolicy policy )
	{
		this.policy = policy;
	}

	public Stream<StorageContainerEntry> getEntries( @Nonnull StorageContainerTrait storageContainer, @Nullable String regexPattern )
	{
		return storageContainer.streamEntriesRecursive( regexPattern, StorageContainerEntry.class, new HomeContainerStorageMapper() );
	}

	private class HomeContainerStorageMapper implements StorageMapper
	{
		@Nullable
		@Override
		public StorageEntry mapStorageContext( @Nonnull StorageContext storageContext ) throws StorageException.Error
		{
			if ( !storageContext.isContainer() )
				return null;
			return new HomeStorageEntry( storageContext, policy );
		}
	}
}
