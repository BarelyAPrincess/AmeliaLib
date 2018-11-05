/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper.queue;

import javax.annotation.Nonnull;

import io.amelia.looper.AbstractLooper;
import io.amelia.support.Maths;

public abstract class EntryAbstract implements Comparable<EntryAbstract>
{
	protected final boolean async;
	protected final long id = AbstractLooper.getGloballyUniqueId();
	protected final DefaultQueue queue;

	/**
	 * Indicates when the entry has been processed by the queue
	 * <p>
	 * This boolean is set when the message is enqueued and remains set while it
	 * is delivered and afterwards when it is recycled. The flag is only cleared
	 * once {@link #recycle()} is called and it's contents are zeroed.
	 * <p>
	 * It is an error to attempt to enqueue or recycle a message that is already finalized.
	 */
	private boolean finalized;

	public EntryAbstract( @Nonnull DefaultQueue queue )
	{
		this( queue, false );
	}

	public EntryAbstract( @Nonnull DefaultQueue queue, boolean async )
	{
		this.queue = queue;
		this.async = async;
	}

	public void cancel()
	{
		synchronized ( queue.entries )
		{
			if ( queue.getActiveEntry() == this )
			{
				queue.clearState();
				queue.wake();
			}
			else
				queue.entries.remove( this );
		}
	}

	@Override
	public int compareTo( @Nonnull EntryAbstract entryAbstract )
	{
		return Maths.nonZero( Long.compare( getWhen(), entryAbstract.getWhen() ), Long.compare( getId(), entryAbstract.getId() ) ).orElse( 0 );
	}

	public long getId()
	{
		return id;
	}

	public int getPositionInQueue()
	{
		synchronized ( queue.entries )
		{
			int pos = 0;
			for ( EntryAbstract queueTask : queue.entries )
				if ( queueTask == this )
					return pos;
				else
					pos++;

			return -1;
		}
	}

	/**
	 * Used for sorting, indicates when the entry is scheduled for processing.
	 */
	public abstract long getWhen();

	public boolean isActive()
	{
		return queue.getActiveEntry() == this;
	}

	public boolean isAsync()
	{
		return async;
	}

	public boolean isEnqueued()
	{
		synchronized ( queue.entries )
		{
			return queue.entries.contains( this );
		}
	}

	public boolean isFinalized()
	{
		return finalized;
	}

	/**
	 * Determines that the entry can be removed from the queue without causing any bugs to the Application.
	 *
	 * @return True if removal is permitted and this task doesn't have to run.
	 */
	public abstract boolean isSafe();

	/**
	 * @hide
	 */
	public void markFinalized()
	{
		finalized = true;
	}

	/**
	 * @hide
	 */
	public void recycle()
	{
		cancel();
		finalized = false;
	}
}
