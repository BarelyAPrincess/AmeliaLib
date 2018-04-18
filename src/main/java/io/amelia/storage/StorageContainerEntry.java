/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;

public class StorageContainerEntry extends StorageEntry implements StorageContainerTrait
{
	public StorageContainerEntry( @Nonnull StorageContext storageContext )
	{
		super( storageContext );
	}

	@Nonnull
	@Override
	public StorageContainerEntry createContainerEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		return getStorageDriver().createContainerEntry( getFullPath() + "/" + localName, storageMapper );
	}

	@Nonnull
	@Override
	public StorageObjectEntry createObjectEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		return getStorageDriver().createObjectEntry( getFullPath() + "/" + localName, storageMapper );
	}

	@Override
	public boolean deleteEntry( @Nonnull String localName )
	{
		return getStorageDriver().deleteEntry( localName );
	}

	@Override
	public StorageEntry getEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		return getStorageDriver().getEntry( getFullPath() + "/" + localName, storageMapper );
	}

	@Override
	public boolean isContainer()
	{
		return true;
	}

	@Override
	public <Entry extends StorageEntry> Stream<Entry> streamEntries( @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper )
	{
		return getStorageDriver().streamEntries( storageEntryClass, storageMapper );
	}

	@Override
	public Stream<StorageEntry> streamEntries( @Nullable StorageMapper storageMapper )
	{
		return getStorageDriver().streamEntries( storageMapper );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern, @Nullable StorageMapper storageMapper )
	{
		return getStorageDriver().streamEntriesRecursive( regexPattern, storageMapper );
	}

	@Override
	public <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper )
	{
		return getStorageDriver().streamEntriesRecursive( storageEntryClass, storageMapper );
	}

	@Override
	public <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nullable String regexPattern, @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper )
	{
		return getStorageDriver().streamEntriesRecursive( regexPattern, storageEntryClass, storageMapper );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable StorageMapper storageMapper )
	{
		return getStorageDriver().streamEntriesRecursive( storageMapper );
	}
}
