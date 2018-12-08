/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.backend;

import io.amelia.lang.StorageException;
import io.amelia.support.NodePath;

public abstract class AbstractBuilder<Extended extends AbstractBuilder<Extended, Backend>, Backend extends StorageBackend>
{
	NodePath mountPath;

	public Extended setMountPath( NodePath mountPath ) throws StorageException.Error
	{
		this.mountPath = mountPath;
		return ( Extended ) this;
	}

	abstract Backend init() throws StorageException.Error;

	public void validate() throws StorageException.Error
	{
		if ( !mountPath.matches( "[a-z0-9]+" ) )
			throw new StorageException.Error( "Backend mount point must only contain lowercase letters and numbers." );
	}
}
