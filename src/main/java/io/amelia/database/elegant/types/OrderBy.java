/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
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
	T orderBy( Collection<String> columns, String dir );

	T orderBy( Collection<String> columns );

	T orderBy( String column, String dir );

	T orderBy( String column );

	T orderAsc();

	T orderDesc();

	T rand();

	T rand( boolean rand );

	boolean isOrderRand();

	boolean isOrderAscending();

	List<String> getOrderBy();
}
