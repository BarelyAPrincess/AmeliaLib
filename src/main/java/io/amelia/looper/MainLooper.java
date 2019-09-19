/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.looper;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.data.parcel.EntryParcel;
import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.foundation.Kernel;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.looper.queue.EntryRunnable;

public abstract class MainLooper extends AbstractLooper<DefaultQueue> implements LooperTaskTrait
{
	public MainLooper()
	{
		setQueue( new DefaultQueue( getLooperControl() ) );
	}

	final boolean enqueueParcel( @Nonnull ParcelCarrier parcelCarrier, @Nonnegative long when )
	{
		if ( isQuitting() )
		{
			Kernel.L.warning( "Looper is quiting." );
			parcelCarrier.recycle();
			return false;
		}

		getQueue().postEntry( new EntryParcel( getQueue(), parcelCarrier, when ) );

		return true;
	}

	public abstract ParcelReceiver getParcelReceiver();

	@Override
	protected final void tick( long loopStartMillis, long lastPolledMillis, long lastOverloadMillis )
	{
		// Call the actual loop logic.
		DefaultQueue.Result result = getQueue().next( loopStartMillis, lastPolledMillis, lastOverloadMillis );

		// A queue entry was successfully returned and can now be invoked.
		if ( result == DefaultQueue.Result.SUCCESS )
		{
			// As of now, the only entry returned on the SUCCESS result is the EntryRunnable (or more so TaskEntry and ParcelEntry).
			EntryRunnable entry = ( EntryRunnable ) getQueue().getActiveEntry();

			entry.markFinalized();
			entry.run();
			entry.recycle();
		}
		// The queue is empty and this looper quits in such cases.
		else if ( result == DefaultQueue.Result.EMPTY && hasFlag( MainLooper.Flag.AUTO_QUIT ) && !isQuitting() )
		{
			quitSafely();
		}
	}
}
