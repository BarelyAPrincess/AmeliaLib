/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant;

import io.amelia.database.cache.QueryCache;
import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.foundation.Kernel;
import io.amelia.lang.DatabaseException;
import io.amelia.support.Objs;

/**
 * Provides the Base Class for all database query types
 */
@SuppressWarnings( {"unchecked", "rawtypes"} )
public abstract class ElegantQuery<T extends ElegantQuery> implements Cloneable
{
	/**
	 * Do we auto update the cache when changes are made?
	 */
	private boolean autoUpdate;
	/**
	 * Is debug enabled for this query?
	 */
	private boolean debug = Kernel.isDevelopment(); // XXX Temporary. Will change in future.
	/**
	 * DatabaseDriver instance
	 */
	private DatabaseDriver driver;
	/**
	 * Holds the last exception encountered
	 */
	private DatabaseException lastException = null;
	/**
	 * Query Driver Cache
	 */
	private QueryCache queryCache = null;
	/**
	 * The table name
	 */
	private String table;

	protected ElegantQuery( DatabaseDriver driver, String table, boolean autoUpdate )
	{
		Objs.notNull( driver );
		this.autoUpdate = autoUpdate;
		this.driver = driver;
		this.table = table;
	}

	/**
	 * @return autoUpdate value
	 */
	public boolean autoUpdate()
	{
		return autoUpdate;
	}

	/**
	 * Sets if this SQL class will auto execute any changes made to it's query
	 *
	 * @param autoUpdate The new autoUpdate value
	 */
	public T autoUpdate( boolean autoUpdate )
	{
		this.autoUpdate = autoUpdate;
		return ( T ) this;
	}

	public T clearError()
	{
		lastException = null;
		return ( T ) this;
	}

	@Override
	public final T clone()
	{
		T clone = clone0();
		clone.debug( debug );
		return clone;
	}

	public abstract T clone0();

	public T debug( boolean debug )
	{
		this.debug = debug;
		return ( T ) this;
	}

	/**
	 * @return Is debug enabled for this query?
	 */
	public boolean debug()
	{
		return debug;
	}

	void exceptionCheck()
	{
		if ( hasException() )
			throw new IllegalStateException( "The Elegant subsystem has encountered a error. Check developer documentation on how to properly handle this programming bug.", lastException );
	}

	public final T execute()
	{
		try
		{
			return executeWithException();
		}
		catch ( DatabaseException e )
		{
			// Ignore
		}
		return ( T ) this;
	}

	public final T executeWithException() throws DatabaseException
	{
		update( false );
		return ( T ) this;
	}

	public final T forceUpdate() throws DatabaseException
	{
		update( true );
		return ( T ) this;
	}

	/**
	 * @return The current DatabaseDriver
	 */
	public DatabaseDriver getDriver()
	{
		return driver;
	}

	public String getTable()
	{
		return table;
	}

	public boolean hasException()
	{
		return lastException != null;
	}

	/**
	 * @return Is the driver actively connected to the database server?
	 */
	public boolean isConnected()
	{
		return driver.isConnected();
	}

	public boolean isUpdateQuery()
	{
		return true;
	}

	/**
	 * @return The last exception encountered
	 */
	public DatabaseException lastException()
	{
		return lastException;
	}

	protected final void notifyChanges()
	{
		try
		{
			/*
			 * If autoUpdate is enabled, we force the driver to make a new cache.
			 * Otherwise we clear the cache is one is generated on the next request for result.
			 */
			if ( autoUpdate )
				update( true );
			else
				queryCache = null;
		}
		catch ( DatabaseException e )
		{
			// Ignore
		}
	}

	/**
	 * Called just before this instance is passed to the driver for execution.
	 *
	 * @throws Exception thrown if there is a problem with the query instance.
	 */
	protected abstract void preExecute() throws DatabaseException;

	protected final void update( boolean force ) throws DatabaseException
	{
		if ( force || queryCache == null )
			try
			{
				preExecute();
				queryCache = driver.execute( this, debug() );
			}
			catch ( DatabaseException e )
			{
				lastException = e;
				throw e;
			}
	}
}
