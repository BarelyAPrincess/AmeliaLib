/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.parcel;

/**
 * Represents a point of contact for each {@link ParcelRegistry}.
 * e.g., Network Connection (remote) or Application instance (local).
 */
public interface ParcelInterface
{
	/**
	 * Does this represent a channel that is remote from this JVM instance, e.g, over network or process?
	 *
	 * @return True if so, otherwise false.
	 */
	default boolean isRemote()
	{
		return true;
	}

	void sendToAll( ParcelCarrier parcel );
}
