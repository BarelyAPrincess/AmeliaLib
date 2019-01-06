/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions;

import javax.annotation.Nonnull;

import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;

public final class ChildPermission implements Comparable<ChildPermission>
{
	private final PermissibleEntity entity;
	private final Permission perm;
	private final VoluntaryBoolean value;
	private final int weight;

	/**
	 * References a permission state/value against an entity
	 *
	 * @param entity The PermissibleEntity
	 * @param perm   The permission this value ordains to
	 * @param value  The custom value assigned to this permission. Can be null to use default assigned value.
	 * @param weight The sorting weight of this ChildPermission
	 */
	public ChildPermission( @Nonnull PermissibleEntity entity, @Nonnull Permission perm, @Nonnull VoluntaryBoolean value, int weight )
	{
		this.entity = entity;
		this.perm = perm;
		this.value = value;
		this.weight = weight;
	}

	@Override
	public int compareTo( ChildPermission child )
	{
		if ( getWeight() == -1 && child.getWeight() == -1 )
			return 0;
		if ( getWeight() == -1 )
			return -1;
		if ( child.getWeight() == -1 )
			return 1;
		return getWeight() - child.getWeight();
	}

	public Boolean getBoolean()
	{
		return value.orElse( null );
	}

	public Permission getPermission()
	{
		return perm;
	}

	public Voluntary<References> getReferences()
	{
		return Voluntary.of( entity.getPermissionReferences( perm ) );
	}

	public VoluntaryBoolean getValue()
	{
		return value;
	}

	public int getWeight()
	{
		return weight;
	}

	public boolean isInherited()
	{
		return weight >= 0;
	}

	@Override
	public String toString()
	{
		return String.format( "ChildPermission{entity=%s,node=%s,value=%s,weight=%s}", entity.uuid(), perm.getNamespace(), value.getString(), weight );
	}
}
