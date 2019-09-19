/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import io.amelia.permissions.PermissibleEntity;
import io.amelia.users.UserContext;
import io.amelia.users.UserEntity;

public interface EntitySubject extends EntityPrincipal
{
	UserContext getContext();

	UserEntity getEntity();

	PermissibleEntity getPermissibleEntity();
}
