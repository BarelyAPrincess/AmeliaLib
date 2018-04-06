/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public abstract class StorageEntry
{
	public static String CREATION_TIME = "last-modified";
	public static String LAST_ACCESSED_TIME = "last-modified";
	public static String LAST_MODIFIED_TIME = "last-modified";
	public static String SIZE = "size";

	private final StorageContext storageContext;
	public volatile Map<String, String> fields = new HashMap<>();

	public StorageEntry( @Nonnull StorageContext storageContext )
	{
		this.storageContext = storageContext;
	}

	public String getFullPath()
	{
		return storageContext.getPath() + "/" + storageContext.getLocalName();
	}

	public String getLocalName()
	{
		return storageContext.getLocalName();
	}

	public String getPath()
	{
		return storageContext.getPath();
	}

	public StorageDriver getStorageDriver()
	{
		return storageContext.getStorageDriver();
	}

	public abstract boolean isContainer();
}
