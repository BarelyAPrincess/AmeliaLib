/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.stream.Collectors;

import io.amelia.storage.backend.StorageBackend;
import io.amelia.support.Arrs;
import io.amelia.support.Strs;

public class StorageFileSystem extends FileSystem
{
	public static final UserPrincipalLookupService USER_LOOKUP_SERVICE = new UserPrincipalLookupService()
	{
		// TODO This is only temporary but ultimately we'd implement an internal file permission checker

		@Override
		public GroupPrincipal lookupPrincipalByGroupName( String group ) throws IOException
		{
			return () -> group;
		}

		@Override
		public UserPrincipal lookupPrincipalByName( String name ) throws IOException
		{
			return () -> name;
		}
	};

	@Override
	public void close() throws IOException
	{
		throw new UnsupportedOperationException();
	}

	public StorageFileAttributes getFileAttributes( String path )
	{
		return null;
	}

	@Override
	public Iterable<FileStore> getFileStores()
	{
		return null;
	}

	@Override
	public Path getPath( String first, String... more )
	{
		return new HoneyPath( this, Strs.join( Arrs.prepend( more, first ), getSeparator() ) );
	}

	@Override
	public PathMatcher getPathMatcher( String syntaxAndPattern )
	{
		return null;
	}

	@Override
	public Iterable<Path> getRootDirectories()
	{
		return HoneyStorage.getBackends().map( StorageBackend::getRootPath ).collect( Collectors.toList() );
	}

	@Override
	public String getSeparator()
	{
		return "/";
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService()
	{
		return USER_LOOKUP_SERVICE;
	}

	@Override
	public boolean isOpen()
	{
		return false;
	}

	@Override
	public boolean isReadOnly()
	{
		return false;
	}

	@Override
	public WatchService newWatchService() throws IOException
	{
		return null;
	}

	@Override
	public FileSystemProvider provider()
	{
		return null;
	}

	@Override
	public Set<String> supportedFileAttributeViews()
	{
		return null;
	}
}
