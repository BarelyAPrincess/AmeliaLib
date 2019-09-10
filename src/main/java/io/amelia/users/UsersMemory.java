package io.amelia.users;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class UsersMemory extends Users
{
	@Override
	public void addUserCreator( UserCreator userCreator )
	{

	}

	@Override
	UserContext createUser( @Nonnull UUID uuid ) throws UserException.Error
	{
		return null;
	}

	@Override
	public UserContext createUser( @Nonnull UUID uuid, @Nonnull UserCreator userCreator ) throws UserException.Error
	{
		return null;
	}

	@Override
	public String getSingleSignonMessage()
	{
		return null;
	}

	@Override
	UserResult getUser( @Nonnull UUID uuid )
	{
		return null;
	}

	@Override
	void getUser( @Nonnull UserResult userResult )
	{

	}

	@Override
	public Optional<UserCreator> getUserCreator( String name )
	{
		return Optional.empty();
	}

	@Override
	public Stream<UserCreator> getUserCreators()
	{
		return null;
	}

	@Override
	Stream<UserContext> getUsers()
	{
		return null;
	}

	@Override
	protected boolean hasUserCreator( String userCreatorName )
	{
		return false;
	}

	@Override
	public boolean isSingleSignonEnabled()
	{
		return false;
	}

	@Override
	void put( UserContext userContext ) throws UserException.Error
	{

	}

	@Override
	protected void removeUserContext( UserContext userContext )
	{

	}

	@Override
	void unload( @Nonnull UUID uuid ) throws UserException.Error
	{

	}

	@Override
	void unload() throws UserException.Error
	{

	}

	@Override
	public boolean userExists( @Nonnull String uuid )
	{
		return false;
	}
}
