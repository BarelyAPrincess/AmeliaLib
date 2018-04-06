package io.amelia.storage;

import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;

public interface StorageContainerTrait
{
	default StorageEntry getEntry( @Nonnull String localName ) throws StorageException.Error
	{
		return getEntry( localName, null );
	}

	StorageEntry getEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error;

	default <Entry extends StorageEntry> Stream<Entry> streamEntries( @Nonnull Class<Entry> storageEntryClass )
	{
		return streamEntries( storageEntryClass, null );
	}

	default Stream<StorageEntry> streamEntries()
	{
		return streamEntries( ( StorageMapper ) null );
	}

	<Entry extends StorageEntry> Stream<Entry> streamEntries( @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper );

	Stream<StorageEntry> streamEntries( @Nullable StorageMapper storageMapper );

	default Stream<StorageEntry> streamEntriesRecursive()
	{
		return streamEntriesRecursive( ( StorageMapper ) null );
	}

	default Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern )
	{
		return streamEntriesRecursive( regexPattern, ( StorageMapper ) null );
	}

	default <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nonnull Class<Entry> storageEntryClass )
	{
		return streamEntriesRecursive( storageEntryClass, null );
	}

	default <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nullable String regexPattern, @Nonnull Class<Entry> storageEntryClass )
	{
		return streamEntriesRecursive( regexPattern, storageEntryClass, null );
	}

	Stream<StorageEntry> streamEntriesRecursive( @Nullable StorageMapper storageMapper );

	Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern, @Nullable StorageMapper storageMapper );

	<Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper );

	<Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nullable String regexPattern, @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper );
}
