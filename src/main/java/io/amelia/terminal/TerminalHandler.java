/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.terminal;

import io.amelia.users.UserResult;

/**
 * Represents a terminal connection end-point
 */
public interface TerminalHandler
{
	enum TerminalType
	{
		LOCAL,
		TELNET,
		WEBSOCKET
	}

	// String getIpAddress();

	UserResult kick( String reason );

	boolean disconnect();

	void println( String... msg );

	void print( String... msg );

	TerminalType type();
}
