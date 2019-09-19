/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.wip.messages;

import java.util.function.Supplier;

public class Messages
{
	public static final Supplier<ApplicationInfoMessage> MESSAGE_APPLICATION_INFO = ApplicationInfoMessage::new;

	private Messages()
	{
		// Static Class
	}
}
