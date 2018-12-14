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

import java.util.UUID;

import io.amelia.lang.UserException;
import io.amelia.permission.PermissibleEntity;

public abstract class UserCreator
{
	private final String name;
	private boolean isDefault;
	private DefaultUsers users;

	public UserCreator( DefaultUsers users, String name, boolean isDefault )
	{
		this.users = users;
		this.name = name;
		this.isDefault = isDefault;

		if ( isDefault )
			users.getUserCreators().forEach( backend -> backend.isDefault = false );
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

	public DefaultUsers getUsers()
	{
		return users;
	}

	public abstract boolean hasUser( UUID uuid );

	public boolean isDefault()
	{
		return isDefault;
	}

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

	public void setDefault()
	{
		users.getUserCreators().forEach( backend -> backend.isDefault = false );
		this.isDefault = true;
	}
}
