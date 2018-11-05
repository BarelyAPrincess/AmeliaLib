package io.amelia.storage;

import java.nio.file.Path;

import io.amelia.storage.backend.StorageBackend;

public class MountPoint
{
	private final StorageBackend backend;
	private final Path mount;
	private final Path subPath;

	MountPoint( StorageBackend backend, Path mount, Path subPath )
	{
		this.backend = backend;
		this.mount = mount;
		this.subPath = subPath;
	}

	public StorageBackend getBackend()
	{
		return backend;
	}

	public Path getMount()
	{
		return mount;
	}

	public Path getSubPath()
	{
		return subPath;
	}
}
