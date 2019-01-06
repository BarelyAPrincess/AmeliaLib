/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users.events;

import io.amelia.support.UserPrincipal;

/**
 * Called when a User leaves a server
 */
public class UserLogoutEvent extends UserEvent
{
	private String quitMessage;

	public UserLogoutEvent( final UserPrincipal userPrincipal, final String quitMessage )
	{
		super( userPrincipal );
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
