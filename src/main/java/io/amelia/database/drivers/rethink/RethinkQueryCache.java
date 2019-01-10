/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.drivers.rethink;

import io.amelia.database.drivers.RethinkDBDriver;
import io.amelia.database.elegant.support.ElegantQuery;
import io.amelia.database.support.QueryCache;

public class RethinkQueryCache extends QueryCache
{
	public RethinkQueryCache( RethinkDBDriver rethinkDriver, ElegantQuery elegantQuery, Object result )
	{

	}

	@Override
	public int count()
	{
		return 0;
	}
}
