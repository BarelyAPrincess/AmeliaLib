/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageBackend;
import io.amelia.storage.StorageContainerEntry;
import io.amelia.storage.StorageEntry;
import io.amelia.storage.StorageManager;
import io.amelia.storage.StorageMapper;
import io.amelia.storage.StorageObjectEntry;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;

public class FileStorageBackend extends StorageBackend
{
	private final Path directory;

	public FileStorageBackend( Path directory )
	{
		IO.isDirectory( directory );

		this.directory = directory;
	}

	@Nonnull
	@Override
	public StorageContainerEntry createContainerEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		try
		{
			Files.createFile( Paths.get( localName ).resolve( directory ) );
			return ( StorageContainerEntry ) getEntry( localName, storageMapper );
		}
		catch ( ClassCastException e )
		{
			throw StorageException.error( "There was a problem; the localName " + localName + " produced an object that didn't extend the StorageContainerEntry class.", e );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}
	}

	@Nonnull
	@Override
	public StorageObjectEntry createObjectEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		try
		{
			Files.createFile( Paths.get( localName ).resolve( directory ) );
			return ( StorageObjectEntry ) getEntry( localName, storageMapper );
		}
		catch ( ClassCastException e )
		{
			throw StorageException.error( "There was a problem; the localName " + localName + " produced an object that didn't extend the StorageObjectEntry class.", e );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}
	}

	@Override
	public boolean deleteEntry( @Nonnull String localName )
	{
		try
		{
			IO.deleteIfExists( Paths.get( localName ).resolve( directory ) );
			return true;
		}
		catch ( IOException e )
		{
			return false;
		}
	}

	public Path getDirectory()
	{
		return directory;
	}

	@Override
	public StorageEntry getEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		return StorageManager.getEntry( new FileStorageContext( this, Paths.get( localName ).resolve( directory ) ), storageMapper );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <Entry extends StorageEntry> Stream<Entry> streamEntries( @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper )
	{
		return streamEntries( storageMapper ).filter( entry -> storageEntryClass.isAssignableFrom( entry.getClass() ) ).map( entry -> ( Entry ) entry );
	}

	@Override
	public Stream<StorageEntry> streamEntries( @Nullable StorageMapper storageMapper )
	{
		try
		{
			return Files.list( directory ).map( file -> {
				try
				{
					return StorageManager.getEntry( new FileStorageContext( this, file ), storageMapper );
				}
				catch ( Exception e )
				{
					return null;
				}
			} ).filter( Objs::isNotNull );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return Stream.empty();
		}
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable StorageMapper storageMapper )
	{
		return streamEntriesRecursive( ( String ) null );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern, @Nullable StorageMapper storageMapper )
	{
		try
		{
			return ( regexPattern == null ? Files.walk( directory ) : Files.walk( directory, Strs.countMatches( regexPattern, '/' ) ).filter( path -> path.toString().replaceAll( "\\\\", "/" ).matches( regexPattern ) ) ).map( file -> {
				try
				{
					return StorageManager.getEntry( new FileStorageContext( this, file ), storageMapper );
				}
				catch ( Exception e )
				{
					return null;
				}
			} ).filter( Objs::isNotNull );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return Stream.empty();
		}
	}

	@Override
	public <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper )
	{
		return streamEntriesRecursive( null, storageEntryClass, storageMapper );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nullable String regexPattern, @Nonnull Class<Entry> storageEntryClass, @Nullable StorageMapper storageMapper )
	{
		return streamEntriesRecursive( regexPattern, storageMapper ).filter( entry -> storageEntryClass.isAssignableFrom( entry.getClass() ) ).map( entry -> ( Entry ) entry );
	}
}
