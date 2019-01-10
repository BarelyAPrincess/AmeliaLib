/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.backend.memory;

import java.util.UUID;

import io.amelia.permissions.PermissibleGroup;

public class MemoryGroup extends PermissibleGroup
{
	public MemoryGroup( MemoryBackend backend, UUID uuid, String name )
	{
		super( uuid, name );
	}

	@Override
	public void reloadGroups()
	{

	}

	@Override
	public void reloadPermissions()
	{

	}

	@Override
	public void remove()
	{

	}

	@Override
	public void save()
	{

	}
}
