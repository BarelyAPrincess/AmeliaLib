/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.parcel;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.looper.queue.EntryAbstract;
import io.amelia.looper.queue.EntryRunnable;
import io.amelia.support.Objs;

public class EntryParcel extends EntryRunnable
{
	ParcelCarrier parcelCarrier;
	long when;

	public EntryParcel( @Nonnull DefaultQueue queue, @Nonnull ParcelCarrier parcelCarrier, @Nonnegative long when )
	{
		super( queue );

		Objs.notNull( parcelCarrier );
		Objs.notNegative( when );

		parcelCarrier.markFinalized();

		this.parcelCarrier = parcelCarrier;
		this.when = when;
	}

	@Override
	public long getWhen()
	{
		return when;
	}

	@Override
	public boolean isSafe()
	{
		return true;
	}

	@Override
	public void recycle()
	{
		parcelCarrier.recycle();
	}

	@Override
	protected void run0( EntryAbstract entry ) throws ApplicationException.Error
	{
		// TODO?
		parcelCarrier.getTargetReceiver().handleParcel( parcelCarrier );
	}
}
