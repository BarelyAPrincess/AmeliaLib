package io.amelia.storage.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

import io.amelia.lang.StorageException;
import io.amelia.storage.StorageContext;
import io.amelia.support.IO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ZipStorageContext extends StorageContext<ZipStorageBackend>
{
	private final InputStream inputStream;
	private final ZipEntry zipEntry;

	public ZipStorageContext( @Nonnull ZipStorageBackend backend, @Nonnull ZipEntry zipEntry, @Nonnull InputStream inputStream ) throws StorageException.Error
	{
		super( backend );

		this.zipEntry = zipEntry;
		this.inputStream = inputStream;
	}

	@Override
	public ByteBuf getContent() throws IOException
	{
		return Unpooled.wrappedBuffer( IO.readStreamToNIOBuffer( inputStream ) );
	}

	@Override
	public long getCreationTime()
	{
		return zipEntry.getCreationTime().toMillis();
	}

	@Override
	public long getLastAccessTime()
	{
		return zipEntry.getLastAccessTime().toMillis();
	}

	@Override
	public long getLastModifiedTime()
	{
		return zipEntry.getLastModifiedTime().toMillis();
	}

	@Override
	public String getLocalName()
	{
		return IO.getLocalName( zipEntry.getName() );
	}

	@Override
	public String getPath()
	{
		return IO.getParentPath( zipEntry.getName() ).replaceAll( "\\\\", "/" );
	}

	@Override
	public boolean isContainer()
	{
		return zipEntry.isDirectory();
	}
}
