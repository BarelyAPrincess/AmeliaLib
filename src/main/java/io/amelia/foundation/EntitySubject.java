package io.amelia.foundation;

import io.amelia.permissions.PermissibleEntity;
import io.amelia.users.UserContext;
import io.amelia.users.UserEntity;

public interface EntitySubject extends EntityPrincipal
{
	UserContext getContext();

	UserEntity getEntity();

	PermissibleEntity getPermissible();
}
