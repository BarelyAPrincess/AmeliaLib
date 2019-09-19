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

import io.amelia.foundation.Runlevel;

public class RunlevelEvent extends ApplicationEvent
{
	private final Runlevel currentRunlevel;
	private final Runlevel previousRunlevel;

	public RunlevelEvent( Runlevel previousRunlevel, Runlevel currentRunlevel )
	{
		this.previousRunlevel = previousRunlevel;
		this.currentRunlevel = currentRunlevel;
	}

	public Runlevel getLastRunLevel()
	{
		return previousRunlevel;
	}

	public Runlevel getRunLevel()
	{
		return currentRunlevel;
	}
}
