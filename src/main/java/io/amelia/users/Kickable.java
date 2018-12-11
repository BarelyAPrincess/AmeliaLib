/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

/**
 * Indicates a kickable user
 */
public interface Kickable
{
	String uuid();

	/**
	 * Attempts to kick Account from server
	 *
	 * @param reason The reason for kick
	 *
	 * @return Result of said kick attempt
	 */
	UserResult kick( String reason );
}
