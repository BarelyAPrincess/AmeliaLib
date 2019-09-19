/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant.types;

/**
 * Provides the Skeleton Interface for SQL Queries implementing the Limit Methods
 */
public interface Limit<T>
{
	int getLimit();

	int getOffset();

	T limit( int limit );

	T limit( int limit, int offset );

	T offset( int offset );

	T skip( int skip );

	T take( int take );
}
