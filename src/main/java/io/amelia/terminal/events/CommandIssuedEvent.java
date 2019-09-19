/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.terminal.events;

import io.amelia.events.Cancellable;
import io.amelia.terminal.Terminal;

public class CommandIssuedEvent extends TerminalEvent implements Cancellable
{
	private final String command;
	private final Terminal terminal;

	public CommandIssuedEvent( String command, Terminal terminal )
	{
		this.terminal = terminal;
		this.command = command;
	}

	public String getCommand()
	{
		return command;
	}

	public Terminal getTerminal()
	{
		return terminal;
	}
}
