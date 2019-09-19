/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.events;

import io.amelia.foundation.EntityPrincipal;

/**
 * Called when a User leaves a server
 */
public class UserLogoutEvent extends UserEvent
{
	private String quitMessage;

	public UserLogoutEvent( final EntityPrincipal entityPrincipal, final String quitMessage )
	{
		super( entityPrincipal );
		this.quitMessage = quitMessage;
	}

	/**
	 * Gets the quit message to send to all online Users
	 *
	 * @return string quit message
	 */
	public String getLeaveMessage()
	{
		return quitMessage;
	}

	/**
	 * Sets the quit message to send to all online Users
	 *
	 * @param quitMessage quit message
	 */
	public void setLeaveMessage( String quitMessage )
	{
		this.quitMessage = quitMessage;
	}
}
