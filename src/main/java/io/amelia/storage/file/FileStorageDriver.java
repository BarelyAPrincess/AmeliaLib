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

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageDriver;
import io.amelia.storage.StorageEntry;
import io.amelia.storage.StorageManager;
import io.amelia.storage.StorageMapper;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;

public class FileStorageDriver extends StorageDriver
{
	private final File directory;
	// private final File metaFile;

	public FileStorageDriver( File directory )
	{
		this( directory, new File( directory, ".meta" ) );
	}

	public FileStorageDriver( File directory, File metaFile )
	{
		IO.isDirectory( directory );
		IO.fileExists( metaFile );

		this.directory = directory;
		// this.metaFile = metaFile;
	}

	public File getDirectory()
	{
		return directory;
	}

	@Override
	public StorageEntry getEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		return StorageManager.getEntry( new FileStorageContext( this, new File( directory, localName ) ), storageMapper );
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
		return Arrays.stream( Objects.requireNonNull( directory.listFiles() ) ).map( file -> {
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

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable StorageMapper storageMapper )
	{
		return streamEntriesRecursive( ( String ) null );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern, @Nullable StorageMapper storageMapper )
	{
		return IO.recursiveFiles( directory, regexPattern == null ? -1 : Strs.countMatches( regexPattern, '/' ), regexPattern ).stream().map( file -> {
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
