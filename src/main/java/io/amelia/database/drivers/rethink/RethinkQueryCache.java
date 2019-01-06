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
