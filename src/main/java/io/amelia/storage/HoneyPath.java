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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.FileSystem;
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
import io.amelia.storage.types.StorageType;
import io.amelia.support.IO;
import io.amelia.support.Namespace;
import io.amelia.support.NodePath;
import io.amelia.support.Objs;
import io.amelia.support.StorageUtils;

public class HoneyPath implements Path
{
	private final FileSystem fileSystem;
	private final NodePath rootPath;

	public HoneyPath( FileSystem fileSystem, Path rootPath )
	{
		this.fileSystem = fileSystem;
		this.rootPath = NodePath.of( rootPath );
	}

	private HoneyPath( FileSystem fileSystem, NodePath rootPath )
	{
		this.fileSystem = fileSystem;
		this.rootPath = rootPath;
	}

	public HoneyPath( FileSystem fileSystem, String rootPath )
	{
		this( fileSystem, NodePath.of( rootPath, NodePath.Separator.FORWARDSLASH ) );
	}

	void checkAccess( AccessMode... accessModes ) throws IOException
	{
		EnumSet<AccessMode> accessModes0 = EnumSet.copyOf( Arrays.asList( accessModes ) );

		/* StorageFileAttributes attr = fileSystem.getFileAttributes( getResolvedPath() );
		if ( attr == null && !rootPath.startsWith( getSeparator() ) )
			throw new NoSuchFileException( getStringPath() );
		else if ( accessModes0.contains( AccessMode.WRITE ) && fileSystem.isReadOnly() )
			throw new AccessDeniedException( getStringPath() );
		else if ( accessModes0.contains( AccessMode.EXECUTE ) )
			throw new AccessDeniedException( getStringPath() ); */
	}

	@Override
	public int compareTo( Path other )
	{
		Objs.notNull( other );
		if ( !( other instanceof HoneyPath ) || getFileSystem() != other.getFileSystem() )
			throw new ProviderMismatchException();

		return Namespace.of( getStringPath(), getFileSystem().getSeparator() ).compareTo( ( ( HoneyPath ) other ).getStringPath() );
	}

	private HoneyPath create( NodePath path )
	{
		return new HoneyPath( fileSystem, path );
	}

	@Override
	public boolean endsWith( Path other )
	{
		return endsWith( other.toString() );
	}

	@Override
	public boolean endsWith( String other )
	{
		return rootPath.endsWith( other );
	}

	@Override
	public Path getFileName()
	{
		return create( rootPath.getLast().setAbsolute( false ) );
	}

	@Override
	public FileSystem getFileSystem()
	{
		return fileSystem;
	}

	@Override
	public Path getName( int index )
	{
		return create( rootPath.getNode( index ).setAbsolute( false ) );
	}

	@Override
	public int getNameCount()
	{
		return rootPath.getNodeCount();
	}

	@Override
	public Path getParent()
	{
		return create( rootPath.getParent() );
	}

	private String getResolvedPath()
	{
		return rootPath.getString();
	}

	@Override
	public Path getRoot()
	{
		return create( rootPath.getFirst() );
	}

	public String getSeparator()
	{
		return fileSystem.getSeparator();
	}

	public <Type extends StorageType> Type getStorageType( Class<Type> typeClass )
	{
		// TODO
		return null;
	}

	public String getStringPath()
	{
		return rootPath.toString();
	}

	@Override
	public boolean isAbsolute()
	{
		return rootPath.isAbsolute();
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
				return inx < rootPath.getNodeCount();
			}

			@Override
			public Path next()
			{
				return create( rootPath.getNode( inx++ ).setAbsolute( false ) );
			}
		};
	}

	@Override
	public Path normalize()
	{
		return create( rootPath.normalizeAscii() );
	}

	@Override
	public WatchKey register( WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers ) throws IOException
	{
		if ( watcher == null )
			throw new NullPointerException();
		else if ( !( watcher instanceof StorageWatchService ) )
			throw new ProviderMismatchException();
		else
		{
			// checkRead();
			return ( ( StorageWatchService ) watcher ).register( this, events, modifiers );
		}
	}

	@Override
	public WatchKey register( WatchService watcher, WatchEvent.Kind<?>... events ) throws IOException
	{
		return this.register( watcher, events, new WatchEvent.Modifier[0] );
	}

	@Override
	public HoneyPath relativize( Path other )
	{
		return create( NodePath.of( IO.relPath( this, other ) ) );
	}

	@Nonnull
	@Override
	public HoneyPath resolve( String other )
	{
		return this.resolve( getFileSystem().getPath( other ) );
	}

	@Override
	public HoneyPath resolve( Path other )
	{
		return create( rootPath.append( IO.toString( other, getSeparator() ) ) );
	}

	public HoneyPath resolve( NodePath other )
	{
		return create( rootPath.append( other ) );
	}

	@Override
	public HoneyPath resolveSibling( Path other )
	{
		return null;
	}

	@Override
	public HoneyPath resolveSibling( String other )
	{
		return null;
	}

	@Override
	public boolean startsWith( Path other )
	{
		return rootPath.startsWith( other.toString() );
	}

	@Override
	public boolean startsWith( String other )
	{
		return rootPath.startsWith( other );
	}

	@Override
	public Path subpath( int beginIndex, int endIndex )
	{
		return create( rootPath.getSubNodes( beginIndex, endIndex ).setAbsolute( false ) );
	}

	@Override
	public HoneyPath toAbsolutePath()
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
		HoneyPath var2 = new HoneyPath( fileSystem, getResolvedPath() ).toAbsolutePath();
		var2.checkAccess();
		return var2;
	}

	@Override
	public String toString()
	{
		return rootPath.getString();
	}

	@Override
	public URI toUri()
	{
		return StorageUtils.toUri( this );
	}
}
