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

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.Encrypt;

public abstract class Users
{
	public static final Kernel.Logger L = Kernel.getLogger( Users.class );
	public static final String PATH_USERS = "__users";

	static
	{
		Kernel.setPath( PATH_USERS, Kernel.PATH_STORAGE, "users" );
	}

	public final UserCreatorMemory MEMORY = new UserCreatorMemory();
	public final ReportingLevel[] reportingLevelSeverityArray = new ReportingLevel[] {ReportingLevel.E_ERROR, ReportingLevel.L_SECURITY, ReportingLevel.L_ERROR, ReportingLevel.L_EXPIRED, ReportingLevel.L_DENIED};
	protected boolean isDebugEnabled = false;

	public Users()
	{
		addUserCreator( MEMORY );
	}

	public abstract void addUserCreator( UserCreator userCreator );

	abstract UserContext createUser( @Nonnull UUID uuid ) throws UserException.Error;

	public abstract UserContext createUser( @Nonnull UUID uuid, @Nonnull UserCreator userCreator ) throws UserException.Error;

	public String generateUuid()
	{
		String uuid;
		do
			uuid = Encrypt.uuid();
		while ( userExists( uuid ) );
		return uuid;
	}

	public abstract UserCreator getDefaultCreator();

	public String getDisplayNameFormat()
	{
		return "${fname} ${name}";
	}

	public abstract String getSingleSignonMessage();

	abstract UserResult getUser( @Nonnull UUID uuid );

	abstract void getUser( @Nonnull UserResult userResult );

	public abstract Optional<UserCreator> getUserCreator( String name );

	public abstract Stream<UserCreator> getUserCreators();

	abstract Stream<UserContext> getUsers();

	public boolean isDebugEnabled()
	{
		return isDebugEnabled;
	}

	public abstract boolean isSingleSignonEnabled();

	abstract void put( UserContext userContext ) throws UserException.Error;

	protected abstract void removeUserContext( UserContext userContext );

	public void setDebugEnabled( boolean isDebugEnabled )
	{
		this.isDebugEnabled = isDebugEnabled;
	}

	abstract void unload( @Nonnull UUID uuid ) throws UserException.Error;

	abstract void unload() throws UserException.Error;

	public abstract boolean userExists( @Nonnull String uuid );

	public static class ConfigKeys
	{
		public static final TypeBase USERS_BASE = new TypeBase( "users" );
		public static final TypeBase.TypeInteger MAX_LOGINS = new TypeBase.TypeInteger( USERS_BASE, "maxLogins", -1 );
		public static final TypeBase CREATORS = new TypeBase( USERS_BASE, "userCreators" );
		public static final TypeBase.TypeString DISPLAY_NAME_FORMAT = new TypeBase.TypeString( USERS_BASE, "displayNameFormat", "${fname} ${name}" );
		public static final TypeBase.TypeBoolean DEBUG_ENABLED = new TypeBase.TypeBoolean( USERS_BASE, "debugEnabled", false );
		public static final TypeBase.TypeBoolean SINGLE_SIGNON = new TypeBase.TypeBoolean( USERS_BASE, "singleSignon", false );
		public static final TypeBase.TypeString SINGLE_SIGNON_MESSAGE = new TypeBase.TypeString( USERS_BASE, "singleSignonMessage", "You logged in from another location." );
		public static final TypeBase SESSIONS_BASE = new TypeBase( USERS_BASE, "sessions" );
		public static final TypeBase.TypeInteger SESSIONS_CLEANUP_INTERVAL = new TypeBase.TypeInteger( SESSIONS_BASE, "cleanupInterval", 5 );

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
