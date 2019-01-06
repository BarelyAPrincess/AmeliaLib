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

/**
 * SQL Skel for Order By builder methods
 */
public interface OrderBy<T>
{
	List<String> getOrderBy();

	boolean isOrderAscending();

	boolean isOrderRand();

	T orderAsc();

	T orderBy( Collection<String> columns, String dir );

	T orderBy( Collection<String> columns );

	T orderBy( String column, String dir );

	T orderBy( String column );

	T orderDesc();

	T rand();

	T rand( boolean rand );
}
