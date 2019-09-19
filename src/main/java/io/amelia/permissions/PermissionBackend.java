/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions;

import java.io.OutputStreamWriter;
import java.util.UUID;
import java.util.stream.Stream;

import io.amelia.foundation.Foundation;
import io.amelia.permissions.lang.PermissionBackendException;

/**
 * Provides the basis of Permission Backend classes
 */
public abstract class PermissionBackend
{
	private final String aliasName;
	private boolean isDefault;
	private Permissions permissions;

	public PermissionBackend( Permissions permissions, String aliasName, boolean isDefault )
	{
		this.permissions = permissions;
		this.aliasName = aliasName;
		this.isDefault = isDefault;

		if ( isDefault )
			Foundation.getPermissions().getBackends().forEach( backend -> backend.isDefault = false );
	}

	public abstract void commit();

	public void dumpData( OutputStreamWriter outputStreamWriter )
	{
		// TODO Auto-generated method stub
	}

	public String getAliasName()
	{
		return aliasName;
	}

	/**
	 * Returns default group, a group that is assigned to a entity without a group set
	 *
	 * @return Default group instance
	 */
	public abstract PermissibleGroup getDefaultGroup( References refs );

	/**
	 * Returns new PermissibleEntity object for specified id
	 *
	 * @param uuid
	 *
	 * @return PermissibleEntity for specified id, or null on error.
	 */
	public abstract PermissibleEntity getEntity( UUID uuid );

	/**
	 * This method loads all entity names from the backend.
	 */
	public abstract Stream<String> getEntityNames();

	public abstract Stream<String> getEntityNames( int type );

	/**
	 * Returns new PermissibleGroup object for specified id
	 *
	 * @param uuid
	 *
	 * @return PermissibleGroup object, or null on error
	 */
	public abstract PermissibleGroup getGroup( UUID uuid );

	/**
	 * This method loads all group names from the backend.
	 */
	public abstract Stream<String> getGroupNames();

	public Permissions getPermissions()
	{
		return permissions;
	}

	/**
	 * Backend initialization should be done here
	 */
	public abstract void initialize() throws PermissionBackendException;

	/**
	 * This method loads all entities from the backend.
	 *
	 * @throws PermissionBackendException
	 */
	public abstract void loadEntities() throws PermissionBackendException;

	/**
	 * This method loads all groups from the backend.
	 *
	 * @throws PermissionBackendException
	 */
	public abstract void loadGroups() throws PermissionBackendException;

	/**
	 * This method loads all permissions from the backend.
	 *
	 * @throws PermissionBackendException
	 */
	public abstract void loadPermissions() throws PermissionBackendException;

	/**
	 * Commits any changes made to the permission node to the backend for saving
	 */
	public abstract void nodeCommit( Permission perm );

	/**
	 * Destroys the permission node and it's children, removing it from both the backend and memory.<br>
	 * <br>
	 * Warning: could be considered unsafe to destroy a permission node without first removing all child values
	 */
	public abstract void nodeDestroy( Permission perm );

	/**
	 * Disregards any changes made to the permission node and reloads from the backend
	 */
	public abstract void nodeReload( Permission perm );

	public abstract void reloadBackend() throws PermissionBackendException;

	/**
	 * Sets the default group
	 */
	public abstract void setDefaultGroup( UUID uuid, References references );
}
