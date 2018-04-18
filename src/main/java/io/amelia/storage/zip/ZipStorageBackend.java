/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.zip;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import io.amelia.support.Maps;
import io.amelia.support.Objs;

public class ZipStorageBackend extends StorageBackend
{
	private final File zipFile;
	private ZipFile zip;

	public ZipStorageBackend( File zipFile ) throws IOException
	{
		IO.fileExists( zipFile );

		this.zip = new ZipFile( zipFile );
		this.zipFile = zipFile;
	}

	@Nonnull
	@Override
	public StorageContainerEntry createContainerEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		try
		{
			createEntry( localName );
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

	private void createEntry( @Nonnull String localName ) throws IOException
	{
		URI uri = URI.create( "jar:file:" + zipFile.getAbsolutePath() );
		FileSystem zipfs = FileSystems.newFileSystem( uri, Maps.builder( "create", "false" ).hashMap() );
		Files.createFile( zipfs.getPath( localName ) );

		this.zip = new ZipFile( zipFile );
	}

	@Nonnull
	@Override
	public StorageObjectEntry createObjectEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		try
		{
			createEntry( localName );
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
			URI uri = URI.create( "jar:file:" + zipFile.getAbsolutePath() );
			FileSystem zipfs = FileSystems.newFileSystem( uri, Maps.builder( "create", "false" ).hashMap() );
			Files.delete( zipfs.getPath( localName ) );

			this.zip = new ZipFile( zipFile );
			return true;
		}
		catch ( IOException e )
		{
			return false;
		}
	}

	@Override
	public StorageEntry getEntry( @Nonnull String localName, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		try
		{
			ZipEntry entry = zip.getEntry( localName );
			return StorageManager.getEntry( new ZipStorageContext( this, entry, zip.getInputStream( entry ) ), storageMapper );
		}
		catch ( Exception e )
		{
			throw StorageException.error( e );
		}
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
		return streamEntriesRecursive( storageMapper ).filter( entry -> Objs.isEmpty( entry.getPath() ) );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable StorageMapper storageMapper )
	{
		return zip.stream().map( zipEntry -> {
			try
			{
				return StorageManager.getEntry( new ZipStorageContext( this, zipEntry, zip.getInputStream( zipEntry ) ), storageMapper );
			}
			catch ( Exception e )
			{
				return null;
			}
		} ).filter( Objs::isNotNull );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern, @Nullable StorageMapper storageMapper )
	{
		return streamEntriesRecursive( storageMapper ).filter( entry -> regexPattern == null || entry.getFullPath().matches( regexPattern ) );
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
