/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.events;

import io.amelia.users.UserPermissible;
import io.amelia.users.UserResult;

/**
 * Fired when an Account login failed
 */
public class UserLoginFailedEvent extends UserEvent
{
	private UserResult result;

	UserLoginFailedEvent( UserPermissible userPermissible, UserResult result )
	{
		super( result.getContext(), userPermissible );
		this.result = result;
	}

	public UserLoginFailedEvent( UserResult result )
	{
		super( result.getContext() );
		this.result = result;
	}

	public UserResult getResult()
	{
		return result;
	}
}
