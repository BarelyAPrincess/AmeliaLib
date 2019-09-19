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

import io.amelia.users.Kickable;

public interface Terminal extends Kickable, CommandSender
{
	void getPrompt();

	void resetPrompt();

	void setPrompt( String prompt );

	void showPrompt();
}
