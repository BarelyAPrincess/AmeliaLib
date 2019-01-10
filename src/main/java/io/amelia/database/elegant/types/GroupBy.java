/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant.types;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface GroupBy<T>
{
	List<String> getGroupBy();

	T groupBy( String... columns );

	T groupBy( String column );

	T groupBy( Collection<String> columns );
}
