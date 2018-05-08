/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant.types;

import java.util.Map;

/**
 * Provides the Skeleton Interface for SQL Queries implementing the Values Methods
 */
public interface Values<T>
{
	T values( Map<String, Object> map );
	
	T value( String key, Object val );
	
	T values( String[] keys, Object[] valuesArray );

	Map<String, Object> getValues();
}
