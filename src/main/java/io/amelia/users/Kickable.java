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

/**
 * Indicates a kickable user
 */
public interface Kickable
{
	/**
	 * Attempts to kick User from server
	 *
	 * @param reason The reason for kick
	 *
	 * @return Result of said kick attempt
	 */
	UserResult kick( String reason );

	UUID uuid();
}
