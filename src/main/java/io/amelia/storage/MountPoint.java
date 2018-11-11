package io.amelia.storage;

import io.amelia.storage.backend.StorageBackend;
import io.amelia.support.NodePath;

public class MountPoint
{
	private final StorageBackend backend;
	private final NodePath mountPath;
	private final NodePath path;

	public MountPoint( StorageBackend backend, NodePath mountPath, NodePath path )
	{
		this.backend = backend;
		this.mountPath = mountPath;
		this.path = path;
	}

	public StorageBackend getBackend()
	{
		return backend;
	}

	public NodePath getMountPath()
	{
		return mountPath;
	}

	public NodePath getPath()
	{
		return path;
	}
}
