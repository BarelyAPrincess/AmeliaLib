package io.amelia.users;

import java.util.UUID;

import io.amelia.data.parcel.Parcel;
import io.amelia.foundation.facades.Users;
import io.amelia.permission.PermissibleEntity;

/**
 * Provides context to a user without actually existing within the users implementation.
 * These classes are a dime-a-dozen and are not unique. The purpose is to carry parcel data from the UserCreator to the users implementation.
 */
public class UserOffline implements UserSubject
{
	private final UserCreator creator;

	private final Parcel parcel = Parcel.empty();
	private final UUID uuid;
	private String name;

	public UserOffline( UserCreator creator, UUID uuid )
	{
		this.uuid = uuid;
		this.creator = creator;
		this.name = null;
	}

	@Override
	public String getDisplayName()
	{
		return;
	}

	@Override
	public UserContext getContext()
	{
		return Users.getInstance().getUsers().filter( user -> uuid.equals( user.uuid() ) ).findAny().orElse( null );
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

	@Override
	public String name()
	{
		return name;
	}

	@Override
	public UUID uuid()
	{
		return uuid;
	}
}
