/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.events;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import io.amelia.events.AbstractEvent;
import io.amelia.support.Lists;
import io.amelia.users.UserPermissible;
import io.amelia.support.UserPrincipal;

/**
 * Represents a account related event
 */
public abstract class UserEvent extends AbstractEvent
{
	private UserPrincipal userPrincipal;
	private Set<UserPermissible> permissibles;

	public UserEvent()
	{
		// New Sub Class?
	}

	public UserEvent( UserPrincipal userPrincipal )
	{
		this( userPrincipal, new HashSet<>() );
	}

	public UserEvent( UserPrincipal userPrincipal, UserPermissible permissible )
	{
		this( userPrincipal, Lists.newHashSet( permissible ) );
	}

	public UserEvent( UserPrincipal userPrincipal, boolean async )
	{
		this( userPrincipal, new HashSet<>(), async );
	}

	public UserEvent( UserPrincipal userPrincipal, Set<UserPermissible> permissibles )
	{
		this.userPrincipal = userPrincipal;
		this.permissibles = permissibles;
	}

	UserEvent( UserPrincipal userPrincipal, Set<UserPermissible> permissibles, boolean async )
	{
		super( async );
		this.userPrincipal = userPrincipal;
		this.permissibles = permissibles;
	}

	/**
	 * Returns the User involved in this event
	 *
	 * @return User who is involved in this event
	 */
	public final UserPrincipal getUserPrincipal()
	{
		return userPrincipal;
	}

	public final Stream<UserPermissible> getPermissibles()
	{
		return permissibles.stream();
	}
}
