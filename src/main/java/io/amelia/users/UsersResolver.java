package io.amelia.users;

import java.util.UUID;

import io.amelia.bindings.BindingResolver;
import io.amelia.bindings.BindingsException;
import io.amelia.bindings.ProvidesClass;
import io.amelia.bindings.Singular;
import io.amelia.foundation.EntityPrincipal;
import io.amelia.support.Priority;

public class UsersResolver extends BindingResolver
{
	private Users usersInstance = null;

	public UsersResolver() throws BindingsException.Error
	{
		super( Priority.NORMAL );
	}

	@ProvidesClass( EntityPrincipal.class )
	public EntityPrincipal entityPrincipal( UUID uuid )
	{
		return users().getUser( uuid );
	}

	@Singular
	@ProvidesClass( Users.class )
	public Users users()
	{
		if ( usersInstance == null )
			usersInstance = new HoneyUsers();
		return usersInstance;
	}
}
