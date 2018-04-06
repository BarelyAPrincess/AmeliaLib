package io.amelia.storage.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageContext;
import io.amelia.support.IO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FileStorageContext extends StorageContext<FileStorageDriver>
{
	private final File file;
	private final BasicFileAttributes fileAttr;

	public FileStorageContext( @Nonnull FileStorageDriver storageDriver, File file ) throws StorageException.Error
	{
		super( storageDriver );

		this.file = file;

		try
		{
			this.fileAttr = Files.readAttributes( file.toPath(), BasicFileAttributes.class );
		}
		catch ( IOException e )
		{
			throw StorageException.error( e );
		}
	}

	@Override
	public ByteBuf getContent() throws IOException
	{
		return Unpooled.wrappedBuffer( IO.readStreamToNIOBuffer( new FileInputStream( file ) ) );
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
		return file.getName();
	}

	@Override
	public String getPath()
	{
		return IO.relPath( file.getParentFile(), storageDriver.getDirectory() ).replaceAll( "\\\\", "/" );
	}

	@Override
	public boolean isContainer()
	{
		return file.isDirectory();
	}
}
