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

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import io.amelia.support.NodePath;

public class HoneyStorageProvider extends FileSystemProvider
{
	public static final String SCHEME = "honey";

	public static StorageFileSystem newFileSystem()
	{
		return new StorageFileSystem();
	}

	@Override
	public void checkAccess( Path path, AccessMode... modes ) throws IOException
	{
		// TODO Implement for DB backends.
	}

	@Override
	public void copy( Path source, Path target, CopyOption... options ) throws IOException
	{
		MountPoint mpSource = HoneyStorage.getMountPoint( source ).orElse( null );
		if ( mpSource == null )
			throw new IOException( "There is no mount point for path " + source.toAbsolutePath() + "." );
		MountPoint mpTarget = HoneyStorage.getMountPoint( target ).orElse( null );
		if ( mpTarget == null )
			throw new IOException( "There is no mount point for path " + target.toAbsolutePath() + "." );

		// TODO COPY
	}

	@Override
	public void createDirectory( Path path, FileAttribute<?>... attrs ) throws IOException
	{
		MountPoint mp = HoneyStorage.getMountPoint( path ).orElse( null );
		if ( mp == null )
			throw new IOException( "There is no mount point for path " + path.toAbsolutePath() + "." );
		mp.getBackend().createDirectory( mp.getPath() );

		// TODO Attributes
	}

	@Override
	public void delete( Path path ) throws IOException
	{
		MountPoint mp = HoneyStorage.getMountPoint( path ).orElse( null );
		if ( mp == null )
			throw new IOException( "There is no mount point for path " + path.toAbsolutePath() + "." );
		mp.getBackend().delete( mp.getPath() );
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView( Path path, Class<V> type, LinkOption... options )
	{
		return null;
	}

	@Override
	public FileStore getFileStore( Path path ) throws IOException
	{
		return null;
	}

	@Override
	public StorageFileSystem getFileSystem( URI uri )
	{
		Path path = getPath( uri );
	}

	@Override
	public Path getPath( URI uri )
	{
		MountPoint mp = HoneyStorage.getMountPoint( NodePath.of( uri.getPath() ) ).orElse( null );
		if ( mp == null )
			throw new FileSystemNotFoundException( "There is no mount point for path " + uri.getPath() + "." );
		return mp.getBackend();
	}

	@Override
	public String getScheme()
	{
		return SCHEME;
	}

	@Override
	public boolean isHidden( Path path ) throws IOException
	{
		MountPoint mp = HoneyStorage.getMountPoint( path ).orElse( null );
		if ( mp == null )
			throw new IOException( "There is no mount point for path " + path.toAbsolutePath() + "." );
		return mp.getBackend().isHidden( mp.getPath() );
	}

	@Override
	public boolean isSameFile( Path path, Path path2 ) throws IOException
	{
		return false;
	}

	@Override
	public void move( Path source, Path target, CopyOption... options ) throws IOException
	{

	}

	@Override
	public SeekableByteChannel newByteChannel( Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs ) throws IOException
	{
		return null;
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream( Path dir, DirectoryStream.Filter<? super Path> filter ) throws IOException
	{
		return null;
	}

	@Override
	public FileSystem newFileSystem( URI uri, Map<String, ?> env ) throws IOException
	{
		return null;
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes( Path path, Class<A> type, LinkOption... options ) throws IOException
	{
		return null;
	}

	@Override
	public Map<String, Object> readAttributes( Path path, String attributes, LinkOption... options ) throws IOException
	{
		return null;
	}

	@Override
	public void setAttribute( Path path, String attribute, Object value, LinkOption... options ) throws IOException
	{

	}
}
