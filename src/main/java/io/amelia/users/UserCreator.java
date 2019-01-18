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

import java.util.UUID;
import java.util.stream.Stream;

import io.amelia.foundation.Foundation;
import io.amelia.permissions.PermissibleEntity;

public abstract class UserCreator
{
	private final String name;

	public UserCreator( String name )
	{
		this.name = name;
	}

	public abstract UserContext create( UUID uuid ) throws UserException.Error;

	public String[] getLoginFields()
	{
		return new String[] {"username", "phone", "email"};
	}

	public String getUUIDField()
	{
		return "uuid";
	}

	public abstract boolean hasUser( UUID uuid );

	public abstract boolean isEnabled();

	public final boolean isMemory()
	{
		return this instanceof UserCreatorMemory;
	}

	public abstract void load();

	public abstract void loginBegin( UserContext userContext, UserPermissible userPermissible, UUID uuid, Object... credentials );

	public abstract void loginFailed( UserResult result );

	public abstract void loginSuccess( UserResult result );

	public abstract void loginSuccessInit( UserContext userContext, PermissibleEntity permissibleEntity );

	public String name()
	{
		return name;
	}

	public abstract void reload( UserContext userContext ) throws UserException.Error;

	public abstract UserResult resolve( UUID uuid );

	/**
	 * Attempts to save the supplied {@link UserContext}.
	 *
	 * @param userContext the savable User
	 *
	 * @throws UserException.Error per implementation
	 */
	public abstract void save( UserContext userContext ) throws UserException.Error;
}
