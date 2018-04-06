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
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageDriver;
import io.amelia.storage.StorageEntry;
import io.amelia.storage.StorageManager;
import io.amelia.support.IO;
import io.amelia.support.Objs;

public class ZipStorageDriver extends StorageDriver
{
	private final ZipFile zip;

	public ZipStorageDriver( File zipFile ) throws IOException
	{
		IO.fileExists( zipFile );

		this.zip = new ZipFile( zipFile );
	}

	@Override
	public StorageEntry getEntry( @Nonnull String localName ) throws StorageException.Error
	{
		try
		{
			ZipEntry entry = zip.getEntry( localName );
			return StorageManager.getEntry( new ZipStorageContext( this, entry, zip.getInputStream( entry ) ) );
		}
		catch ( Exception e )
		{
			throw StorageException.error( e );
		}
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <Entry extends StorageEntry> Stream<Entry> streamEntries( @Nonnull Class<Entry> storageEntryClass )
	{
		return streamEntries().filter( entry -> storageEntryClass.isAssignableFrom( entry.getClass() ) ).map( entry -> ( Entry ) entry );
	}

	@Override
	public Stream<StorageEntry> streamEntries()
	{
		return streamEntriesRecursive().filter( entry -> !Objs.isEmpty( entry.getPath() ) );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive()
	{
		return Objs.enumerationAsStream( zip.entries() ).map( zipEntry -> {
			try
			{
				return StorageManager.getEntry( new ZipStorageContext( this, zipEntry, zip.getInputStream( zipEntry ) ) );
			}
			catch ( Exception e )
			{
				return null;
			}
		} ).filter( Objs::isNotNull );
	}

	@Override
	public Stream<StorageEntry> streamEntriesRecursive( @Nullable String regexPattern )
	{
		return streamEntriesRecursive().filter( entry -> regexPattern == null || entry.getFullPath().matches( regexPattern ) );
	}

	@Override
	public <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nonnull Class<Entry> storageEntryClass )
	{
		return streamEntriesRecursive( null, storageEntryClass );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <Entry extends StorageEntry> Stream<Entry> streamEntriesRecursive( @Nullable String regexPattern, @Nonnull Class<Entry> storageEntryClass )
	{
		return streamEntriesRecursive( regexPattern ).filter( entry -> storageEntryClass.isAssignableFrom( entry.getClass() ) ).map( entry -> ( Entry ) entry );
	}
}
