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

import java.util.UUID;

/**
 * Represents entities with the ability to execute commands through the {@link CommandDispatch}
 */
public interface CommandSender
{
	UUID uuid();

	String name();

	TerminalEntity getTerminalEntity();

	String getVariable( String key );

	String getVariable( String key, String def );

	void sendMessage( String message );

	void setVariable( String key, String val );
}
