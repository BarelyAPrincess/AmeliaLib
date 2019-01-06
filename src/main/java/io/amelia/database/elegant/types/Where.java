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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides the Skeleton Interface for SQL Queries implementing the Where Methods
 */
public interface Where<B extends Where<?, ?>, P>
{
	B and();

	List<WhereItem> getElements();

	WhereItem.Group<B, P> group();

	B or();

	WhereItem.Divider separator();

	WhereItem.KeyValue<B> where( String key );

	B where( WhereItem element );

	B where( Map<String, Object> map );

	B whereMatches( Map<String, Object> values );

	B whereMatches( String key, Object value );

	B whereMatches( Collection<String> valueKeys, Collection<Object> valueValues );
}
