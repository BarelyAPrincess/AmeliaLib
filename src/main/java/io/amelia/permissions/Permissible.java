/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions;

import java.util.UUID;

import io.amelia.foundation.Foundation;
import io.amelia.permissions.lang.PermissionDeniedException;

public abstract class Permissible
{
	/**
	 * Used to reference the PermissibleEntity for the Permissible object.
	 */
	private PermissibleEntity entity = null;

	private boolean checkEntity()
	{
		if ( Foundation.isNullEntity(entity) )
			entity = Foundation.getPermissions().getPermissibleEntity( uuid() );
		if ( entity == null )
			entity = Foundation.getNullEntity().getPermissibleEntity();

		return !Foundation.isNullEntity( entity );
	}

	public final PermissionResult checkPermission( String perm )
	{
		return checkPermission( Foundation.getPermissions().createNode( PermissionNamespace.of( perm ) ) );
	}

	public final PermissionResult checkPermission( String perm, References refs )
	{
		return checkPermission( Foundation.getPermissions().createNode( PermissionNamespace.of( perm ) ), refs );
	}

	public final PermissionResult checkPermission( Permission perm, References refs )
	{
		PermissibleEntity entity = getPermissibleEntity();
		return entity.checkPermission( perm, refs );
	}

	public final PermissionResult checkPermission( String perm, String... refs )
	{
		return checkPermission( perm, References.format( refs ) );
	}

	public final PermissionResult checkPermission( Permission perm, String... refs )
	{
		return checkPermission( perm, References.format( refs ) );
	}

	public final PermissionResult checkPermission( Permission perm )
	{
		return checkPermission( perm, References.format( "" ) );
	}

	public final void destroyEntity()
	{
		entity = Foundation.getNullEntity().getPermissibleEntity();
	}

	/**
	 * Get the unique identifier for this Permissible
	 *
	 * @return String a unique identifier
	 */
	public abstract UUID uuid();

	public final PermissibleEntity getPermissibleEntity()
	{
		checkEntity();
		return entity;
	}

	public final boolean isAdmin()
	{
		if ( !checkEntity() )
			return false;

		return entity.isAdmin();
	}

	public final boolean isBanned()
	{
		if ( !checkEntity() )
			return false;

		return entity.isBanned();
	}

	/**
	 * Is this permissible on the OP list.
	 *
	 * @return true if OP
	 */
	public final boolean isOp()
	{
		if ( !checkEntity() )
			return false;

		return entity.isOp();
	}

	public final boolean isWhitelisted()
	{
		if ( !checkEntity() )
			return false;

		return entity.isWhitelisted();
	}

	/**
	 * -1, everybody, everyone = Allow All!
	 * 0, op, root | sys.op = OP Only!
	 * admin | sys.admin = Admin Only!
	 */
	public final PermissionResult requirePermission( String req, References refs ) throws PermissionDeniedException
	{
		return requirePermission( Foundation.getPermissions().createNode( PermissionNamespace.of( req ) ), refs );
	}

	public final PermissionResult requirePermission( String req, String... refs ) throws PermissionDeniedException
	{
		return requirePermission( Foundation.getPermissions().createNode( PermissionNamespace.of( req ) ), References.format( refs ) );
	}

	public final PermissionResult requirePermission( Permission req, String... refs ) throws PermissionDeniedException
	{
		return requirePermission( req, References.format( refs ) );
	}

	public final PermissionResult requirePermission( Permission req, References refs ) throws PermissionDeniedException
	{
		PermissionResult result = checkPermission( req );

		if ( result.getPermission() != PermissionDefault.EVERYBODY.getNode() )
		{
			if ( result.getEntity() == null || Foundation.isNullEntity( result.getEntity() ) )
				throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.LOGIN_PAGE );

			if ( !result.isTrue() )
			{
				if ( result.getPermission() == PermissionDefault.OP.getNode() )
					throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.OP_ONLY );

				result.recalculatePermissions( refs );
				if ( result.isTrue() )
					return result;

				throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.DENIED.setPermission( req ) );
			}
		}

		return result;
	}
}
