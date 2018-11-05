/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;
import io.amelia.support.Namespace;
import io.amelia.support.NodePath;
import io.amelia.support.Objs;
import io.amelia.support.StorageUtils;
import sun.nio.fs.AbstractWatchService;

public class StoragePath implements Path
{
	private final StorageFileSystem fileSystem;
	private final NodePath path;
	private final boolean absolute;

	private StoragePath( StorageFileSystem fileSystem, NodePath path, boolean absolute )
	{
		this.fileSystem = fileSystem;
		this.path = path;
		this.absolute = absolute;
	}

	public StoragePath( StorageFileSystem fileSystem, String path )
	{
		this( fileSystem, NodePath.parseString( path, NodePath.Separator.FORWARDSLASH ), path.startsWith( "/" ) );
	}

	void checkAccess( AccessMode... accessModes ) throws IOException
	{
		EnumSet<AccessMode> accessModes0 = EnumSet.copyOf( Arrays.asList( accessModes ) );

		StorageFileAttributes attr = fileSystem.getFileAttributes( getResolvedPath() );
		if ( attr == null && !path.startsWith( getSeparator() ) )
			throw new NoSuchFileException( getStringPath() );
		else if ( accessModes0.contains( AccessMode.WRITE ) && fileSystem.isReadOnly() )
			throw new AccessDeniedException( getStringPath() );
		else if ( accessModes0.contains( AccessMode.EXECUTE ) )
			throw new AccessDeniedException( getStringPath() );
	}

	@Override
	public int compareTo( Path other )
	{
		Objs.notNull( other );
		if ( !( other instanceof StoragePath ) || getFileSystem() != other.getFileSystem() )
			throw new ProviderMismatchException();

		return Namespace.parseString( getStringPath(), getFileSystem().getSeparator() ).compareTo( ( ( StoragePath ) other ).getStringPath() );
	}

	private String getResolvedPath()
	{
		return path.getString();
	}

	public String getSeparator()
	{
		return fileSystem.getSeparator();
	}

	@Override
	public StorageFileSystem getFileSystem()
	{
		return fileSystem;
	}

	public String getStringPath()
	{
		return path.toString();
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
		return create( path.getLast() );
	}

	@Override
	public Path getName( int index )
	{
		return create( path.getNode( index ) );
	}

	@Override
	public int getNameCount()
	{
		return path.getNodeCount();
	}

	private StoragePath create( NodePath path )
	{
		return new StoragePath( fileSystem, path, true );
	}

	@Override
	public Path getParent()
	{
		return create( path.getParent() );
	}

	@Override
	public Path getRoot()
	{
		return create( path.getFirst() );
	}

	@Override
	public boolean isAbsolute()
	{
		return absolute;
	}

	@Override
	public Iterator<Path> iterator()
	{
		return new Iterator<Path>()
		{
			int inx = 0;

			@Override
			public boolean hasNext()
			{
				return inx < path.getNodeCount();
			}

			@Override
			public Path next()
			{
				return create( path.getNode( inx++ ) );
			}
		};
	}

	@Override
	public Path normalize()
	{
		return create( path );
	}

	@Override
	public WatchKey register( WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers ) throws IOException
	{
		if ( watcher == null )
			throw new NullPointerException();
		else if ( !( watcher instanceof AbstractWatchService ) )
			throw new ProviderMismatchException();
		else
		{
			this.checkRead();
			return ( ( AbstractWatchService ) watcher ).register( this, events, modifiers );
		}
	}

	@Override
	public WatchKey register( WatchService watcher, WatchEvent.Kind<?>... events ) throws IOException
	{
		return null;
	}

	public StoragePath create( String path )
	{
		return create( NodePath.parseString( path ) );
	}

	@Override
	public Path relativize( Path other )
	{
		return create( IO.relPath( this, other ) );
	}

	@Nonnull
	@Override
	public Path resolve( String other )
	{
		return this.resolve( getFileSystem().getPath( other ) );
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
		return path.startsWith( other.toString() );
	}

	@Override
	public boolean startsWith( String other )
	{
		return path.startsWith( other );
	}

	@Override
	public Path subpath( int beginIndex, int endIndex )
	{
		return create( path.subNodes( beginIndex, endIndex ) );
	}

	@Override
	public StoragePath toAbsolutePath()
	{
		return this;
	}

	@Override
	public File toFile()
	{
		throw new ApplicationException.Runtime( "Not Possible" );
	}

	@Override
	public Path toRealPath( LinkOption... options ) throws IOException
	{
		StoragePath var2 = new StoragePath( fileSystem, getResolvedPath() ).toAbsolutePath();
		var2.checkAccess();
		return var2;
	}

	@Override
	public URI toUri()
	{
		return StorageUtils.toUri( this );
	}

	@Override
	public String toString()
	{
		return path.getString();
	}
}
