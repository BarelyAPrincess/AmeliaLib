package io.amelia.storage.backend;

import java.nio.file.Path;

import io.amelia.storage.StorageRegistry;

public class FileBackend extends StorageBackend
{
	public FileBackend( String backendPrefix, Path path, StorageRegistry.BackendType config )
	{
		super( backendPrefix, path, config );
	}

	public FileBackend( String backendPrefix, Path path )
	{
		this( backendPrefix, path, StorageRegistry.BackendType.DEFAULT );
	}
}
