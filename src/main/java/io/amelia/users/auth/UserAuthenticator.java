/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.amelia.users.UserAttachment;
import io.amelia.users.UserException;
import io.amelia.support.Objs;
import io.amelia.users.UserPermissible;
import io.amelia.foundation.EntityPrincipal;

/**
 * References available Account Authenticators
 */
public abstract class UserAuthenticator
{
	/**
	 * Typically only used for authenticating the NONE login
	 * This will fail for all other logins
	 */
	public static final NullUserAuthenticator NULL = new NullUserAuthenticator();
	/**
	 * Used to authenticate any Account that supports plain text passwords
	 */
	public static final PlainTextUserAuthenticator PASSWORD = new PlainTextUserAuthenticator();
	/**
	 * Typically only used to authenticate relogins, for security, token will change with each successful auth
	 */
	public static final OnetimeTokenUserAuthenticator TOKEN = new OnetimeTokenUserAuthenticator();
	/**
	 * Holds reference to loaded Account Authenticators
	 */
	private static final List<UserAuthenticator> authenticators = new ArrayList<>();

	@SuppressWarnings( "unchecked" )
	public static <T extends UserAuthenticator> T byName( String name )
	{
		Objs.notEmpty( name );

		for ( UserAuthenticator aa : authenticators )
			if ( name.equalsIgnoreCase( aa.name ) )
				return ( T ) aa;
		return null;
	}

	public static Stream<UserAuthenticator> getAuthenticators()
	{
		return authenticators.stream();
	}

	private String name;

	UserAuthenticator( String name )
	{
		this.name = name;
		authenticators.add( this );
	}

	/**
	 * Used to resume a saved session login
	 *
	 * @param entityPrincipal The Account HookMeta
	 * @param permissible     An instance of the {@link UserAttachment}
	 *
	 * @return The authorized account credentials
	 */
	public abstract UserCredentials authorize( EntityPrincipal entityPrincipal, UserPermissible permissible ) throws UserException.Error;

	/**
	 * Used to check Account Credentials prior to creating the Account Instance
	 *
	 * @param entityPrincipal The Account HookMeta
	 * @param credentials     The Credentials to use for authentication
	 *
	 * @return An instance of the Account Credentials
	 */
	public abstract UserCredentials authorize( EntityPrincipal entityPrincipal, Object... credentials ) throws UserException.Error;
}
