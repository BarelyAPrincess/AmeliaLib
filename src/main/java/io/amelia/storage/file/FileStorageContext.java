package io.amelia.storage.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageContext;
import io.amelia.support.IO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FileStorageContext extends StorageContext<FileStorageBackend>
{
	private final BasicFileAttributes fileAttr;
	private final Path path;

	public FileStorageContext( @Nonnull FileStorageBackend backend, Path path ) throws StorageException.Error
	{
		super( backend );

		this.path = path;

		try
		{
			this.fileAttr = Files.readAttributes( path, BasicFileAttributes.class );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}
	}

	@Override
	public ByteBuf getContent() throws IOException
	{
		return Unpooled.wrappedBuffer( IO.readStreamToNIOBuffer( new FileInputStream( path.toFile() ) ) );
	}

	@Override
	public long getCreationTime()
	{
		return fileAttr.creationTime().toMillis();
	}

	@Override
	public long getLastAccessTime()
	{
		return fileAttr.lastAccessTime().toMillis();
	}

	@Override
	public long getLastModifiedTime()
	{
		return fileAttr.lastModifiedTime().toMillis();
	}

	@Override
	public String getLocalName()
	{
		return path.getFileName().toString();
	}

	@Override
	public String getPath()
	{
		return IO.relPath( path.getParent(), backend.getDirectory() ).replaceAll( "\\\\", "/" );
	}

	@Override
	public boolean isContainer()
	{
		return Files.isDirectory( path );
	}
}
