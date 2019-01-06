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

import io.amelia.foundation.EntityPrincipal;

/**
 * Fired when an User is being loaded into memory
 */
public class UserLoadEvent extends UserEvent
{
	public UserLoadEvent( EntityPrincipal entityPrincipal )
	{
		super( entityPrincipal );
	}
}
