/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.auth;

import io.amelia.foundation.EntityPrincipal;
import io.amelia.foundation.Foundation;
import io.amelia.users.UserException;
import io.amelia.users.DescriptiveReason;
import io.amelia.users.UserPermissible;

/**
 * Used to authenticate the NULL user
 */
public final class NullUserAuthenticator extends UserAuthenticator
{
	class NullUserCredentials extends UserCredentials
	{
		NullUserCredentials( EntityPrincipal entity, DescriptiveReason descriptiveReason )
		{
			super( NullUserAuthenticator.this, entity, descriptiveReason );
		}
	}

	NullUserAuthenticator()
	{
		super( "null" );
	}

	@Override
	public UserCredentials authorize( EntityPrincipal entity, UserPermissible permissible ) throws UserException.Error
	{
		return new NullUserCredentials( entity, Foundation.isNullEntity( entity ) ? DescriptiveReason.LOGIN_SUCCESS : DescriptiveReason.INCORRECT_LOGIN );
	}

	@Override
	public UserCredentials authorize( EntityPrincipal entity, Object... credentials ) throws UserException.Error
	{
		return new NullUserCredentials( entity, Foundation.isNullEntity( entity ) ? DescriptiveReason.LOGIN_SUCCESS : DescriptiveReason.INCORRECT_LOGIN );
	}
}
