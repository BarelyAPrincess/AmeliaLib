/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.backend.memory;

import java.util.UUID;
import java.util.stream.Stream;

import io.amelia.foundation.Foundation;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.permissions.PermissibleGroup;
import io.amelia.permissions.Permission;
import io.amelia.permissions.PermissionBackend;
import io.amelia.permissions.Permissions;
import io.amelia.permissions.References;
import io.amelia.permissions.lang.PermissionBackendException;

/*
 * Memory Backend
 * Zero Persistence. Does not attempt to save any changes.
 */
public class MemoryBackend extends PermissionBackend
{
	// TODO Temporary
	public static final UUID DEFAULT_UUID = UUID.randomUUID();

	public MemoryBackend( Permissions permissions, boolean isDefault )
	{
		super( permissions, "memory", isDefault );
	}

	@Override
	public void commit()
	{
		// Nothing to do here!
	}

	@Override
	public PermissibleGroup getDefaultGroup( References refs )
	{
		return Foundation.getPermissions().getGroup( DEFAULT_UUID );
	}

	@Override
	public PermissibleEntity getEntity( UUID uuid )
	{
		return new MemoryEntity( this, uuid, "" );
	}

	@Override
	public Stream<String> getEntityNames()
	{
		return Stream.empty();
	}

	@Override
	public Stream<String> getEntityNames( int type )
	{
		return Stream.empty();
	}

	@Override
	public PermissibleGroup getGroup( UUID uuid )
	{
		return new MemoryGroup( this, uuid, "" );
	}

	@Override
	public Stream<String> getGroupNames()
	{
		return Stream.empty();
	}

	@Override
	public void initialize() throws PermissionBackendException
	{
		// Nothing to do here!
	}

	@Override
	public void loadEntities()
	{
		// Nothing to do here!
	}

	@Override
	public void loadGroups()
	{
		// Nothing to do here!
	}

	@Override
	public void loadPermissions()
	{
		// Nothing to do here!
	}

	@Override
	public void nodeCommit( Permission perm )
	{
		Permissions.L.fine( "MemoryPermission nodes can not be saved. Sorry for the inconvenience. Might you consider changing permission backend. :(" );
	}

	@Override
	public void nodeDestroy( Permission perm )
	{
		// Nothing to do here!
	}

	@Override
	public void nodeReload( Permission perm )
	{
		// Nothing to do here!
	}

	@Override
	public void reloadBackend() throws PermissionBackendException
	{
		// Nothing to do here!
	}

	@Override
	public void setDefaultGroup( UUID uuid, References refs )
	{
		// Nothing to do here!
	}
}
