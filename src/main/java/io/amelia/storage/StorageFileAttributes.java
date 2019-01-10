/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import io.amelia.data.parcel.Parcel;

/**
 * Provides a reusable generic implementation of {@link BasicFileAttributes} using {@link Parcel} as the value container.
 */
public class StorageFileAttributes implements BasicFileAttributes
{
	public static final String CREATION_TIME = "creationTime";
	public static final String LAST_ACCESS_TIME = "lastAccessTime";
	public static final String LAST_MODIFIED_TIME = "lastModifiedTime";
	public static final String SIZE = "size";
	public static final String IS_DIRECTORY = "isDirectory";
	public static final String IS_OTHER = "isOther";
	public static final String IS_REGULAR_FILE = "isRegularFile";
	public static final String IS_SYMLINK = "isSymlink";
	public static final String FILE_KEY = "fileKey";

	public static StorageFileAttributes get( Path path, boolean b )
	{
		return null;
	}

	private final Parcel attributes;

	StorageFileAttributes( Parcel attributes )
	{
		this.attributes = attributes;
	}

	@Override
	public FileTime creationTime()
	{
		return FileTime.from( attributes.getLong( CREATION_TIME ).orElse( -1L ), TimeUnit.SECONDS );
	}

	@Override
	public String fileKey()
	{
		return attributes.getString( FILE_KEY ).orElse( null );
	}

	@Override
	public boolean isDirectory()
	{
		return attributes.getBoolean( IS_DIRECTORY ).orElse( false );
	}

	@Override
	public boolean isOther()
	{
		return attributes.getBoolean( IS_OTHER ).orElse( false );
	}

	@Override
	public boolean isRegularFile()
	{
		return attributes.getBoolean( IS_REGULAR_FILE ).orElse( false );
	}

	@Override
	public boolean isSymbolicLink()
	{
		return attributes.getBoolean( IS_SYMLINK ).orElse( false );
	}

	@Override
	public FileTime lastAccessTime()
	{
		return FileTime.from( attributes.getLong( LAST_ACCESS_TIME ).orElse( -1L ), TimeUnit.SECONDS );
	}

	@Override
	public FileTime lastModifiedTime()
	{
		return FileTime.from( attributes.getLong( LAST_MODIFIED_TIME ).orElse( -1L ), TimeUnit.SECONDS );
	}

	@Override
	public long size()
	{
		return attributes.getLong( SIZE ).orElse( -1L );
	}
}
