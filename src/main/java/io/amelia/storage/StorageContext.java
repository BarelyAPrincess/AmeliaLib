package io.amelia.storage;

import java.io.IOException;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;

public abstract class StorageContext<Driver extends StorageDriver>
{
	protected final Driver storageDriver;

	public StorageContext( @Nonnull Driver storageDriver )
	{
		this.storageDriver = storageDriver;
	}

	public abstract ByteBuf getContent() throws IOException;

	public abstract long getCreationTime();

	public abstract long getLastAccessTime();

	public abstract long getLastModifiedTime();

	public abstract String getLocalName();

	public abstract String getPath();

	public Driver getStorageDriver()
	{
		return storageDriver;
	}

	public abstract boolean isContainer();
}
