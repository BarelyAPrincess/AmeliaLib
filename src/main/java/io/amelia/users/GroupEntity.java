/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

public class GroupEntity implements GroupPrincipal
{
	private final String name;
	private final String uuid;

	public GroupEntity( String uuid, String name )
	{
		this.uuid = uuid;
		this.name = name;
	}

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public String uuid()
	{
		return uuid;
	}
}
