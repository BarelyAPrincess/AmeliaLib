/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.parcel;

import java.util.HashMap;
import java.util.Map;

/**
 * Not Finished!
 */
public class ParcelRegistry
{
	private final static Map<ParcelInterface, Registration> registrationMap = new HashMap<>();

	public static Registration getApplicationRegistration( ParcelInterface parcelInterface )
	{
		return registrationMap.computeIfAbsent( parcelInterface, k -> new Registration() );
	}

	public static Registration registerChannel( ParcelInterface applicationChannel )
	{
		return registrationMap.computeIfAbsent( applicationChannel, k -> new Registration() );
	}

	public static void unregisterChannel( ParcelInterface applicationChannel )
	{
		// TODO Mark registration as invalid!
		registrationMap.remove( applicationChannel );
	}

	private ParcelRegistry()
	{
		// Static Access
	}

	/**
	 * Provides a complete registration of receivers and senders available at each {@link ParcelInterface}.
	 */
	static class Registration
	{
		private final Map<String, Object> registered = new HashMap<>();
	}
}
