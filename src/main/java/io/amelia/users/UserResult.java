/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.amelia.foundation.EntityPrincipal;
import io.amelia.lang.ReportingLevel;

public class UserResult implements EntityPrincipal
{
	@Nonnull
	private final UUID uuid;
	private Throwable cause = null;
	@Nonnull
	private DescriptiveReason descriptiveReason = DescriptiveReason.NULL;
	private UserContext userContext = null;

	public UserResult( @Nonnull UserContext userContext )
	{
		this.userContext = userContext;
		this.uuid = userContext.uuid();
	}

	public UserResult( @Nonnull UUID uuid )
	{
		this.uuid = uuid;
	}

	public UserException.Error getCause()
	{
		return new UserException.Error( userContext, descriptiveReason, cause );
	}

	@Nonnull
	public DescriptiveReason getDescriptiveReason()
	{
		return descriptiveReason;
	}

	public ReportingLevel getReportingLevel()
	{
		return descriptiveReason.getReportingLevel();
	}

	public UserContext getUser()
	{
		return userContext;
	}

	public boolean hasCause()
	{
		return cause != null;
	}

	public boolean hasResult()
	{
		return descriptiveReason != DescriptiveReason.NULL;
	}

	@Override
	public String name()
	{
		return userContext.name();
	}

	public UserResult reset()
	{
		descriptiveReason = DescriptiveReason.NULL;
		cause = null;
		return this;
	}

	public UserResult setCause( Throwable cause )
	{
		this.cause = cause;
		return this;
	}

	public UserResult setDescriptiveReason( @Nonnull DescriptiveReason descriptiveReason )
	{
		this.descriptiveReason = descriptiveReason;
		return this;
	}

	public UserResult setUser( UserContext userContext )
	{
		if ( !uuid.equals( userContext.uuid() ) )
			throw new IllegalArgumentException( "UserContext did not match the uuid this UserResult was constructed with." );
		this.userContext = userContext;
		return this;
	}

	@Override
	public UUID uuid()
	{
		return userContext == null ? uuid : userContext.uuid();
	}
}
