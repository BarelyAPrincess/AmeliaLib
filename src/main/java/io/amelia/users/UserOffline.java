package io.amelia.users;

import java.util.UUID;

import io.amelia.data.parcel.Parcel;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.EntitySubject;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.support.Parcels;

/**
 * Provides context to a user without actually existing within the users implementation.
 * These classes are a dime-a-dozen and are not unique. The purpose is to carry parcel data from the UserCreator to the users implementation.
 */
public class UserOffline implements EntitySubject
{
	final Parcel parcel = Parcel.empty();
	private final UserCreator userCreator;
	private final UUID uuid;
	private UserContext userContext;

	public UserOffline( UserCreator userCreator, UUID uuid )
	{
		this.uuid = uuid;
		this.userCreator = userCreator;
	}

	public UserContext getContext( boolean create )
	{
		UserContext userContext = userCreator.getUsers().getUsers().filter( user -> uuid.equals( user.uuid() ) ).findAny().orElse( null );
		if ( create )
			try
			{
				userContext = new UserContext( this );
				userCreator.getUsers().put( userContext );
			}
			catch ( UserException.Error error )
			{
				// Ignore
			}
		return userContext;
	}

	@Override
	public UserContext getContext()
	{
		return getContext( true );
	}

	@Override
	public UserEntity getEntity()
	{
		return getContext().getEntity();
	}

	@Override
	public PermissibleEntity getPermissible()
	{
		return getContext().getPermissible();
	}

	public UserCreator getUserCreator()
	{
		return userCreator;
	}

	@Override
	public String name()
	{
		return Parcels.parseFormatString( parcel, ConfigRegistry.config.getValue( HoneyUsers.ConfigKeys.DISPLAY_NAME_FORMAT ) ).orElse( "{error}" );
	}

	@Override
	public UUID uuid()
	{
		return uuid;
	}
}
