/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

import io.amelia.support.LocalBoolean;

public interface Cancellable
{
	/**
	 * Gets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
	 * pass to other plugins
	 *
	 * @return true if this event is cancelled
	 */
	default boolean isCancelled()
	{
		return LocalBoolean
				.getHolder( Cancellable.class )
				.getState( this );
	}

	/**
	 * Sets the cancellation state of this event. A cancelled event will not be executed in the server, but will still
	 * pass to other plugins.
	 *
	 * @param cancel true if you wish to cancel this event
	 */
	default void setCancelled( boolean cancel )
	{
		LocalBoolean.getHolder( Cancellable.class ).setState( this, cancel );
	}
}
