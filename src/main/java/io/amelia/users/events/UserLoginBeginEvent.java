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

import io.amelia.events.Cancellable;
import io.amelia.events.RegisteredListener;
import io.amelia.users.DescriptiveReason;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.users.UserPermissible;

/**
 * Stores details for Users attempting to log in
 */
public class UserLoginBeginEvent extends UserEvent implements Cancellable
{
	private final Object[] credentials;
	private final UserPermissible userPermissible;
	private DescriptiveReason reason = DescriptiveReason.NULL;

	public UserLoginBeginEvent( EntityPrincipal entityPrincipal, UserPermissible userPermissible, Object[] credentials )
	{
		super( entityPrincipal, userPermissible );
		this.userPermissible = userPermissible;
		this.credentials = credentials;
	}

	/**
	 * Notifies the user that log has failed with the given reason
	 *
	 * @param reason fail message to display to the user
	 */
	public void fail( final DescriptiveReason reason )
	{
		this.reason = reason;
	}

	public UserPermissible getAttachment()
	{
		return userPermissible;
	}

	public Object[] getCredentials()
	{
		return credentials;
	}

	/**
	 * Gets the current result of the login, as an enum
	 *
	 * @return Current AccountResult of the login
	 */
	public DescriptiveReason getDescriptiveReason()
	{
		return reason;
	}

	@Override
	public boolean isCancelled()
	{
		return reason == DescriptiveReason.CANCELLED_BY_EVENT;
	}

	@Override
	protected boolean onEventConditional( RegisteredListener registeredListener )
	{
		// If the result returned is an error then we skip the remaining EventListeners
		return reason.getReportingLevel().isSuccess();
	}

	@Override
	public void setCancelled( boolean cancel )
	{
		reason = DescriptiveReason.CANCELLED_BY_EVENT;
	}

	/**
	 * Sets the new result of the login
	 *
	 * @param reason reason to set
	 */
	public void setDescriptiveReason( final DescriptiveReason reason )
	{
		this.reason = reason;
	}

	/**
	 * Allows the User to log in
	 */
	public void success()
	{
		reason = DescriptiveReason.NULL;
	}
}
