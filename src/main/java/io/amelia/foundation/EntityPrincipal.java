/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.util.UUID;

public interface EntityPrincipal
{
	/**
	 * Gets the "friendly" name to display of this user. This may include color from the {@link io.amelia.support.EnumColor} class.
	 *
	 * @return the friendly name
	 */
	String name();

	/**
	 * Gets the universal id to track this user.
	 *
	 * @return the UUID
	 */
	UUID uuid();
}
