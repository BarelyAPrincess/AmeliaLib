/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.facades;

import io.amelia.bindings.Bindings;
import io.amelia.bindings.BindingsException;
import io.amelia.users.BaseUsers;

public class Users
{
	private static BaseUsers baseUsers = null;

	@SuppressWarnings( "unchecked" )
	public static <T extends BaseUsers> T getInstance()
	{
		if ( baseUsers == null )
			baseUsers = Bindings.resolveClassOrFail( BaseUsers.class, () -> new BindingsException.Ignorable( "The Users are not loaded. This is either an application or initialization bug." ) );
		return ( T ) baseUsers;
	}

	private Users()
	{
		// Static Access
	}
}
