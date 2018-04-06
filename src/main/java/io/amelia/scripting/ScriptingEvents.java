/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

/**
 * Provides an interface for which the Scripting Engine to notify Scripts of events, such as exception or before execution.
 */
public interface ScriptingEvents
{
	void onAfterExecute( ScriptingContext context );

	void onBeforeExecute( ScriptingContext context );

	void onException( ScriptingContext context, Throwable throwable );
}
