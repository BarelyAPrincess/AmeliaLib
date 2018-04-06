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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Kernel;
import io.amelia.lang.StorageException;

public class StorageManager
{
	public static final Kernel.Logger L = Kernel.getLogger( StorageManager.class );

	public static StorageEntry getEntry( @Nonnull StorageContext storageContext, @Nullable StorageMapper storageMapper ) throws StorageException.Error
	{
		if ( storageMapper == null )
			storageMapper = storageContext.getStorageDriver().getMapper();

		return storageMapper.mapStorageContext( storageContext );
	}
}
