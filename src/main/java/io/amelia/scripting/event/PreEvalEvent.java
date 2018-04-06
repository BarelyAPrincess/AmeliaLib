/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.event;

import io.amelia.foundation.events.AbstractEvent;
import io.amelia.foundation.events.Cancellable;
import io.amelia.scripting.ScriptingContext;

public class PreEvalEvent extends AbstractEvent implements Cancellable
{
	private final ScriptingContext context;
	private boolean cancelled;

	public PreEvalEvent( ScriptingContext context )
	{
		this.context = context;
	}

	public ScriptingContext context()
	{
		return context;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled( boolean cancelled )
	{
		this.cancelled = cancelled;
	}
}
