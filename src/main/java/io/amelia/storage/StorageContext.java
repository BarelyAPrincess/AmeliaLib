package io.amelia.storage;

import java.io.IOException;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;

public abstract class StorageContext<Backend extends StorageBackend>
{
	protected final Backend backend;

	public StorageContext( @Nonnull Backend backend )
	{
		this.backend = backend;
	}

	public abstract ByteBuf getContent() throws IOException;

	public abstract long getCreationTime();

	public abstract long getLastAccessTime();

	public abstract long getLastModifiedTime();

	public abstract String getLocalName();

	public abstract String getPath();

	public Backend getStorageBackend()
	{
		return backend;
	}

	public abstract boolean isContainer();
}
