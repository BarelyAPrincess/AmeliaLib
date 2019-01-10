/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.event;

public class PermissibleSystemEvent extends PermissibleEvent
{
	protected Action action;

	public PermissibleSystemEvent( Action action )
	{
		super( action.toString() );

		this.action = action;
	}

	public Action getAction()
	{
		return action;
	}

	public enum Action
	{
		BACKEND_CHANGED,
		RELOADED,
		DEFAULTGROUP_CHANGED,
		DEBUGMODE_TOGGLE,
		WHITELIST_TOGGLE,
		ALLOWOP_TOGGLE,
		REINJECT_PERMISSIBLES,
	}
}
