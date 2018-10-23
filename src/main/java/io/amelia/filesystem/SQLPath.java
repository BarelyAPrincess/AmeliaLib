/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

import javax.annotation.Nonnull;

public class SQLPath extends StoragePath<SQLFileSystem>
{
	private final String path;

	SQLPath( SQLFileSystem fileSystem, String path )
	{
		super( fileSystem );
		this.path = path;
	}

	void checkAccess( AccessMode... accessModes ) throws IOException
	{
		EnumSet<AccessMode> accessModes0 = EnumSet.copyOf( Arrays.asList( accessModes ) );

		StorageFileAttributes attr = fileSystem.getFileAttributes( getResolvedPath() );
		if ( attr == null && !path.startsWith( getSeparator() ) )
			throw new NoSuchFileException( getPath() );
		else if ( accessModes0.contains( AccessMode.WRITE ) && fileSystem.isReadOnly() )
			throw new AccessDeniedException( getPath() );
		else if ( accessModes0.contains( AccessMode.EXECUTE ) )
			throw new AccessDeniedException( getPath() );
	}

	private String getResolvedPath()
	{
		return null;
	}

	@Override
	public boolean endsWith( Path other )
	{
		return endsWith( other.toString() );
	}

	@Override
	public boolean endsWith( String other )
	{
		return path.endsWith( other );
	}

	@Override
	public Path getFileName()
	{
		return null;
	}

	@Override
	public Path getName( int index )
	{
		return null;
	}

	@Override
	public int getNameCount()
	{
		return 0;
	}

	@Override
	public Path getParent()
	{
		return null;
	}

	public String getPath()
	{
		return path;
	}

	@Override
	public Path getRoot()
	{
		return null;
	}

	public String getSeparator()
	{
		return fileSystem.getSeparator();
	}

	@Override
	public boolean isAbsolute()
	{
		return false;
	}

	@Override
	public Iterator<Path> iterator()
	{
		return null;
	}

	@Override
	public Path normalize()
	{
		return null;
	}

	@Override
	public WatchKey register( WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers ) throws IOException
	{
		return null;
	}

	@Override
	public WatchKey register( WatchService watcher, WatchEvent.Kind<?>... events ) throws IOException
	{
		return null;
	}

	@Override
	public Path relativize( Path other )
	{
		return null;
	}

	@Nonnull
	@Override
	public Path resolve( String other )
	{
		return this.resolve( getFileSystem().getPath( other, new String[0] ) );
	}

	@Override
	public Path resolve( Path other )
	{
		return null;
	}

	@Override
	public Path resolveSibling( Path other )
	{
		return null;
	}

	@Override
	public Path resolveSibling( String other )
	{
		return null;
	}

	@Override
	public boolean startsWith( Path other )
	{
		return false;
	}

	@Override
	public boolean startsWith( String other )
	{
		return false;
	}

	@Override
	public Path subpath( int beginIndex, int endIndex )
	{
		return null;
	}

	@Override
	public SQLPath toAbsolutePath()
	{
		return null;
	}

	@Override
	public File toFile()
	{
		return null;
	}

	@Override
	public Path toRealPath( LinkOption... options ) throws IOException
	{
		SQLPath var2 = new SQLPath( fileSystem, getResolvedPath() ).toAbsolutePath();
		var2.checkAccess();
		return var2;
	}

	@Override
	public URI toUri()
	{
		return null;
	}
}
