package io.amelia.database.cache;

import io.amelia.database.drivers.RethinkDBDriver;
import io.amelia.database.elegant.ElegantQuery;

public class RethinkQueryCache implements QueryCache
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
