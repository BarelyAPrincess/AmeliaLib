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

import io.amelia.foundation.Foundation;
import io.amelia.support.DateAndTime;
import io.amelia.support.Objs;
import io.amelia.support.VoluntaryBoolean;

/**
 * Holds the union between {@link Permission} and {@link PermissibleEntity}<br>
 * Also provides access to {@link #assign(References)} and {@link #assign(VoluntaryBoolean, References)}
 */
public class PermissionResult
{
	public static final PermissionResult DUMMY = new PermissionResult( Foundation.getNullEntity().getPermissibleEntity(), PermissionDefault.DEFAULT.getNode() );
	private final PermissibleEntity entity;
	private final Permission perm;
	protected long epoch = DateAndTime.epoch();
	private ChildPermission childPerm = null;
	private References refs;

	PermissionResult( PermissibleEntity entity, Permission perm )
	{
		this( entity, perm, References.format( "" ) );
	}

	PermissionResult( PermissibleEntity entity, Permission perm, References refs )
	{
		Objs.notNull( entity );
		Objs.notNull( perm );

		this.entity = entity;
		this.perm = perm;
		this.refs = refs;
		childPerm = entity.getChildPermissionRecursive( perm, refs ).orElse( null );
	}

	public PermissionResult assign()
	{
		return assign( null );
	}

	public PermissionResult assign( References refs )
	{
		return assign( null, refs );
	}

	public PermissionResult assign( VoluntaryBoolean value, References references )
	{
		if ( references == null )
			references = References.format();

		entity.addPermission( perm, value, references );

		recalculatePermissions();
		return this;
	}

	/**
	 * See {@link Permission#commitToBackend()}<br>
	 * Caution: will commit changes made to other child values of the same permission node
	 *
	 * @return The {@link PermissionResult} for chaining
	 */
	public PermissionResult commit()
	{
		perm.commitToBackend();
		entity.save();
		return this;
	}

	public PermissibleEntity getEntity()
	{
		return entity;
	}

	public Permission getPermission()
	{
		return perm;
	}

	public References getReference()
	{
		return refs;
	}

	/**
	 * Returns a final object based on assignment of permission.
	 *
	 * @return Unassigned will return the default value.
	 */
	public VoluntaryBoolean getValue()
	{
		if ( childPerm == null )
			return perm.getPermissionMeta().getDefault();
		if ( !childPerm.getValue().isPresent() )
			return PermissionDefault.DEFAULT.getNode().getPermissionMeta().getValue();

		return childPerm.getValue();
	}

	public int getWeight()
	{
		return childPerm == null ? 9999 : childPerm.getWeight();
	}

	/**
	 * @return was this entity assigned an custom value for this permission.
	 */
	public boolean hasValue()
	{
		return childPerm != null && childPerm.getValue().isPresent();
	}

	/**
	 * @return was this permission assigned to our entity?
	 */
	public boolean isAssigned()
	{
		return childPerm != null;
	}

	/**
	 * @return was this permission assigned to our entity thru a group? Will return false if not assigned.
	 */
	public boolean isInherited()
	{
		if ( !isAssigned() )
			return false;

		return childPerm.isInherited();
	}

	/**
	 * Used strictly for BOOLEAN permission nodes.
	 *
	 * @return is this permission true
	 */
	public boolean isTrue()
	{
		return isTrue( true );
	}

	/**
	 * Used strictly for BOOLEAN permission nodes.
	 *
	 * @param allowOps Return true if the entity is a server operator
	 *
	 * @return is this permission true
	 *
	 * TODO VoluntaryBooleanWithCause<PermissionException>
	 */
	public boolean isTrue( boolean allowOps )
	{
		// We can check and allow OPs but ONLY if we are not checking a PermissionDefault node, for one 'sys.op' is the node we check for OPs.
		if ( allowOps && Foundation.getPermissions().allowOps && !perm.getPermissionNamespace().matches( PermissionDefault.OP.getPermissionNamespace() ) && entity.isOp() )
			return perm.getPermissionMeta().getValue().get();

		return getValue().get();
	}

	public PermissionResult recalculatePermissions()
	{
		return recalculatePermissions( refs );
	}

	public PermissionResult recalculatePermissions( References refs )
	{
		this.refs = refs;
		childPerm = entity.getChildPermissionRecursive( perm, refs ).orElse( null );

		// Loader.getLogger().debug( "Recalculating permission " + perm.getPermissionNamespace() + " for " + entity.uuid() + " with result " + ( childPerm != null ) );

		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "PermissionResult{name=%s,value=%s,isAssigned=%s}", perm.getNamespace(), getValue(), isAssigned() );
	}

	public PermissionResult unassign()
	{
		return assign( null );
	}

	public PermissionResult unassign( References refs )
	{
		if ( refs == null )
			refs = References.format();

		entity.removePermission( perm, refs );

		recalculatePermissions();
		return this;
	}
}
