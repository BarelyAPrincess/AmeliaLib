package io.amelia.foundation;

import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.UserException;
import io.amelia.users.UserContext;
import io.amelia.users.UserResult;

public interface BaseUsers
{
	Kernel.Logger L = Kernel.getLogger( BaseUsers.class );

	UserContext createUser( @Nonnull UUID uuid ) throws UserException.Error;

	UserResult getUser( @Nonnull UUID uuid );

	Stream<UserContext> getUsers();
}
