package io.amelia.users;

import java.util.UUID;

import io.amelia.bindings.BindingResolver;
import io.amelia.bindings.DynamicBinding;
import io.amelia.foundation.EntityPrincipal;

public class UsersResolver extends BindingResolver
{
	private Users usersInstance = null;

	public UsersResolver()
	{
		addAlias( EntityPrincipal.class, "entityPrincipal" );
	}

	@DynamicBinding
	public EntityPrincipal entityPrincipal( UUID uuid )
	{
		return getUsers().getUser( uuid );
	}

	public Users getUsers()
	{
		if ( usersInstance == null )
			usersInstance = new HoneyUsers();
		return usersInstance;
	}
}
