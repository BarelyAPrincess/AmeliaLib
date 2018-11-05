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

public class ApplicationRegistry
{
	private final static Map<ParcelInterface, ApplicationRegistration> registrationMap = new HashMap<>();

	public static ApplicationRegistration getApplicationRegistration( ParcelInterface parcelInterface )
	{
		return registrationMap.computeIfAbsent( parcelInterface, k -> new ApplicationRegistration() );
	}

	public static ApplicationRegistration registerChannel( ParcelInterface applicationChannel )
	{
		return registrationMap.computeIfAbsent( applicationChannel, k -> new ApplicationRegistration() );
	}

	public static void unregisterChannel( ParcelInterface applicationChannel )
	{
		// TODO Mark registration as invalid!
		registrationMap.remove( applicationChannel );
	}

	private ApplicationRegistry()
	{
		// Static Access
	}
}
