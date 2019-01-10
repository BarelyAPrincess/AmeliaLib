/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.plugins.events;

import io.amelia.plugins.Plugin;

/**
 * Called when a plugin is enabled.
 */
public class PluginEnableEvent extends PluginEvent
{
	public PluginEnableEvent( final Plugin plugin )
	{
		super( plugin );
	}
}
