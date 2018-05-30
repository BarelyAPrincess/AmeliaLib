/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.filesystem;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

import io.amelia.support.Arrs;
import io.amelia.support.Strs;

public class SQLFileSystem extends StorageFileSystem
{
	@Override
	public void close() throws IOException
	{

	}

	@Override
	public Iterable<FileStore> getFileStores()
	{
		return null;
	}

	@Override
	public Path getPath( String first, String... more )
	{
		return new SQLPath( this, Strs.join( Arrs.prepend( more, first ), getSeparator() ) );
	}

	@Override
	public PathMatcher getPathMatcher( String syntaxAndPattern )
	{
		return null;
	}

	@Override
	public Iterable<Path> getRootDirectories()
	{
		return null;
	}

	@Override
	public String getSeparator()
	{
		return "/";
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService()
	{
		return null;
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
