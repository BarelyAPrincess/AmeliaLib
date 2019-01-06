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

import io.amelia.support.Objs;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;

public class PermissionMeta
{
	private final PermissionNamespace namespace;
	private String description = "";
	private VoluntaryBoolean value = VoluntaryBoolean.empty();
	private VoluntaryBoolean valueDefault = VoluntaryBoolean.empty();

	public PermissionMeta( PermissionNamespace namespace )
	{
		this.namespace = namespace;
	}

	public VoluntaryBoolean getDefault()
	{
		if ( !value.isPresent() || this != PermissionDefault.DEFAULT.getNode().getPermissionMeta() )
			if ( this != PermissionDefault.DEFAULT.getNode().getPermissionMeta() )
				return PermissionDefault.DEFAULT.getNode().getPermissionMeta().getValueDefault();
			else
				return VoluntaryBoolean.empty();

		return valueDefault;
	}

	/**
	 * Gets a brief description of this permission, if set
	 *
	 * @return Brief description of this permission
	 */
	public Voluntary<String> getDescription()
	{
		return Voluntary.ofNullable( description );
	}

	public Voluntary<Permission> getPermission()
	{
		return namespace.getPermission();
	}

	public VoluntaryBoolean getValue()
	{
		if ( value == null || !value.isPresent() )
			return getValueDefault();

		return value;
	}

	public VoluntaryBoolean getValueDefault()
	{
		return valueDefault;
	}

	public boolean hasDescription()
	{
		return !Objs.isEmpty( description );
	}

	/**
	 * Sets the description of this permission.
	 * <p>
	 * This will not be saved to disk, and is a temporary operation until the server reloads permissions.
	 *
	 * @param description The new description to set
	 */
	public PermissionMeta setDescription( String description )
	{
		this.description = description == null ? "" : description;
		return this;
	}

	public PermissionMeta setValue( @Nonnull VoluntaryBoolean value )
	{
		this.value = value;
		return this;
	}

	public PermissionMeta setValueDefault( @Nonnull VoluntaryBoolean valueDefault )
	{
		this.valueDefault = valueDefault;
		return this;
	}
}
