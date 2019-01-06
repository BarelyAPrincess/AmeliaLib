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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import io.amelia.events.AbstractEvent;
import io.amelia.support.Lists;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.users.UserPermissible;

/**
 * Represents a account related event
 */
public abstract class UserEvent extends AbstractEvent
{
	private Set<UserPermissible> permissibles;
	private EntityPrincipal entityPrincipal;

	public UserEvent()
	{
		// New Sub Class?
	}

	public UserEvent( EntityPrincipal entityPrincipal )
	{
		this( entityPrincipal, new HashSet<>() );
	}

	public UserEvent( EntityPrincipal entityPrincipal, UserPermissible permissible )
	{
		this( entityPrincipal, Lists.newHashSet( permissible ) );
	}

	public UserEvent( EntityPrincipal entityPrincipal, boolean async )
	{
		this( entityPrincipal, new HashSet<>(), async );
	}

	public UserEvent( EntityPrincipal entityPrincipal, Set<UserPermissible> permissibles )
	{
		this.entityPrincipal = entityPrincipal;
		this.permissibles = permissibles;
	}

	UserEvent( EntityPrincipal entityPrincipal, Set<UserPermissible> permissibles, boolean async )
	{
		super( async );
		this.entityPrincipal = entityPrincipal;
		this.permissibles = permissibles;
	}

	public final Stream<UserPermissible> getPermissibles()
	{
		return permissibles.stream();
	}

	/**
	 * Returns the User involved in this event
	 *
	 * @return User who is involved in this event
	 */
	public final EntityPrincipal getEntityPrincipal()
	{
		return entityPrincipal;
	}
}
