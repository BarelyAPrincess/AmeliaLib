/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a complete registration of receivers and senders available at each {@link ParcelInterface}.
 */
class ApplicationRegistration
{
	private final Map<String, Object> registered = new HashMap<>();
}
