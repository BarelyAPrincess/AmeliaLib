package io.amelia.storage.backend;

import java.nio.file.Path;

import io.amelia.storage.HoneyStorage;
import io.amelia.support.NodePath;

public class FileStorageBackend extends StorageBackend
{
	public FileStorageBackend( Path path, NodePath mountPath, HoneyStorage.BackendType type )
	{
		super( path, mountPath, type );
	}

	public FileStorageBackend( Path path, NodePath mountPath )
	{
		this( path, mountPath, HoneyStorage.BackendType.DEFAULT );
	}

	public FileStorageBackend( Path path )
	{
		this( path, NodePath.empty() );
	}
}
