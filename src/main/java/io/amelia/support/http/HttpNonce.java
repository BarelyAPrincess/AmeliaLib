/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support.http;

import java.util.Map;

import io.amelia.data.parcel.Parcel;

public interface HttpNonce
{
	Parcel getData();

	String key();

	void put( String key, String val );

	void putAll( Map<String, String> values );

	String query();

	boolean validate( String token );

	String value();
}
