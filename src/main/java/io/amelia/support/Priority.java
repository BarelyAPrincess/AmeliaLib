/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.function.Function;

/**
 * Represents an event's priority in execution
 */
public enum Priority
{
	/**
	 * Event call is of very low importance and should be ran first, to allow other plugins to further customize the
	 * outcome
	 */
	LOWEST( 0 ),
	/**
	 * Event call is of low importance
	 */
	LOW( 1 ),
	/**
	 * Event call is neither important or unimportant, and may be ran normally
	 */
	NORMAL( 2 ),
	/**
	 * Event call is of high importance
	 */
	HIGH( 3 ),
	/**
	 * Event call is critical and must have the final say in what happens to the event
	 */
	HIGHEST( 4 ),
	/**
	 * Event is listened to purely for monitoring the outcome of an event.
	 * <p/>
	 * No modifications to the event should be made under this priority
	 */
	MONITOR( 5 );
	
	private final int value;
	
	Priority( int value )
	{
		this.value = value;
	}
	
	public int intValue()
	{
		return value;
	}

	public static class Comparator<Obj> implements java.util.Comparator<Obj>
	{
		private final Function<Obj, Priority> priorityMethod;

		public Comparator( Function<Obj, Priority> priorityMethod )
		{
			this.priorityMethod = priorityMethod;
		}

		@Override
		public int compare( Obj o1, Obj o2 )
		{
			if ( o1 == null && o2 == null )
				return 0;
			if ( o1 == null )
				return 1;
			if ( o2 == null )
				return -1;

			Priority p1 = priorityMethod.apply( o1 );
			Priority p2 = priorityMethod.apply( o2 );

			return p1.compareTo( p2 );
		}
	}}
