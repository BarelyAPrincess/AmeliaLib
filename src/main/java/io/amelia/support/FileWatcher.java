/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.nio.file.Files;
import java.nio.file.Path;

import io.amelia.looper.DefaultLooper;
import io.amelia.looper.Delays;
import io.amelia.looper.LooperTaskTrait;

public abstract class FileWatcher
{
	private static final DefaultLooper looper = DefaultLooper.newParallelLooper();

	protected final Path path;
	private long cycleCoolDown = 1;
	private long lastCheck;
	private long lastModified = 0;
	private LooperTaskTrait.RepeatingTaskEntry task;

	public FileWatcher( Path path )
	{
		this.path = path;
		task = looper.postTaskRepeatingNext( entry -> {
			boolean changesDetected = false;
			lastCheck = Delays.ticks();

			if ( Files.exists( path ) )
			{
				long newLastModified = IO.getLastModified( path );
				changesDetected = newLastModified > lastModified;

				if ( changesDetected )
				{
					lastModified = newLastModified;
					cycleCoolDown = 1;
					readChanges();
				}
			}

			if ( !changesDetected && Delays.SECOND_5 * cycleCoolDown < Delays.MINUTE_15 )
				cycleCoolDown++;

			task.setDelay( Delays.SECOND_5 * cycleCoolDown );
		}, Delays.MINUTE );
		// The delay doesn't really matter - it's updated each cycle.
	}

	public abstract void readChanges();

	/**
	 * Resets the cool down period and runs the task sooner
	 */
	public final void reviveTask()
	{
		if ( cycleCoolDown == 1 )
			return;
		cycleCoolDown = 1;

		task.cancel();
		task = looper.postTaskRepeating( task.getTask(), Delays.ticks() - lastCheck >= Delays.MINUTE_5 ? 0L : Delays.SECOND_5 - ( Delays.SECOND * ( Delays.ticks() - lastCheck ) ) );
	}
}
