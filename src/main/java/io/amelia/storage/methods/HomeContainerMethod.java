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

import io.amelia.storage.StorageEntryContainer;
import io.amelia.storage.StorageMethod;

public class HomeContainerMethod implements StorageMethod
{
	private final StorageEntryContainer directoryStorageEntry;

	public HomeContainerMethod( @Nonnull StorageEntryContainer directoryStorageEntry )
	{
		this.directoryStorageEntry = directoryStorageEntry;
	}

	public Stream<StorageEntryContainer> getEntries( @Nullable String regexPattern )
	{
		return directoryStorageEntry.streamEntriesRecursive( regexPattern, StorageEntryContainer.class );
	}
}
