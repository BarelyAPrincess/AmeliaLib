/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper;

import io.amelia.foundation.Kernel;
import io.amelia.looper.queue.AbstractQueue;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.looper.queue.EntryAbstract;
import io.amelia.looper.queue.EntryRunnable;

public final class DefaultLooper extends AbstractLooper<DefaultQueue> implements LooperTaskTrait
{
	public static final LooperFactory<DefaultLooper> FACTORY = new LooperFactory<>( DefaultLooper::new );

	public static DefaultLooper newParallelLooper()
	{
		DefaultLooper looper = new DefaultLooper();
		Kernel.getExecutorParallel().execute( looper::joinLoop );
		return looper;
	}

	public DefaultLooper()
	{
		setQueue( new DefaultQueue( getLooperControl() ) );
	}

	public DefaultLooper( Flag... flags )
	{
		super( flags );
		setQueue( new DefaultQueue( getLooperControl() ) );
	}

	@Override
	public boolean isAsync()
	{
		return getQueue().isAsync();
	}

	public boolean isDisposed()
	{
		return !FACTORY.hasLooper( this );
	}

	@Override
	public boolean isPermitted( EntryAbstract entry )
	{
		// TODO Check known built-in AbstractEntry sub-classes, so someone doesn't add one we don't know how to handle.
		return entry instanceof EntryRunnable;
	}

	@Override
	protected void quitFinal()
	{
		FACTORY.remove( this );
	}

	@Override
	protected void signalPostJoinLoop()
	{
		// Do Nothing
	}

	@Override
	protected void tick( long loopStartMillis )
	{
		// Call the actual loop logic.
		AbstractQueue.Result result = getQueue().next( loopStartMillis );

		// A queue entry was successful returned, process it and then recycle it.
		if ( result == AbstractQueue.Result.SUCCESS )
		{
			// As of now, the only entry returned on the SUCCESS result is the RunnableEntry (or more so TaskEntry and ParcelEntry).
			EntryRunnable entry = ( EntryRunnable ) getQueue().getActiveEntry();

			entry.markFinalized();
			entry.run();
			entry.recycle();
		}
		// The queue is empty and this looper quits in such cases.
		else if ( result == AbstractQueue.Result.EMPTY && hasFlag( Flag.AUTO_QUIT ) && !isQuitting() )
		{
			quitSafely();
		}
	}
}
