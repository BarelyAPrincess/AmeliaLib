/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

public class UserEntity implements UserPrincipal
{
	private final UserContext userContext;

	UserEntity( UserContext userContext )
	{
		this.userContext = userContext;
	}

	@Override
	public String uuid()
	{
		return userContext.uuid();
	}

	@Override
	public String name()
	{
		return userContext.name();
	}
}
