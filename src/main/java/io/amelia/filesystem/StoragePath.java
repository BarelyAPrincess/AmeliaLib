/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.filesystem;

import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;

import io.amelia.support.Namespace;
import io.amelia.support.Objs;

public abstract class StoragePath implements Path
{
	protected final StorageFileSystem fileSystem;

	public StoragePath( StorageFileSystem fileSystem )
	{
		this.fileSystem = fileSystem;
	}

	@Override
	public int compareTo( Path other )
	{
		Objs.notNull( other );
		if ( !( other instanceof StoragePath ) || getFileSystem() != other.getFileSystem() )
			throw new ProviderMismatchException();

		return Namespace.parseString( getPath(), getFileSystem().getSeparator() ).compareTo( ( ( StoragePath ) other ).getPath() );
	}

	@Override
	public StorageFileSystem getFileSystem()
	{
		return fileSystem;
	}

	protected abstract String getPath();
}
