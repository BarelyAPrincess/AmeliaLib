package io.amelia.users;

import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.data.parcel.ParcelSender;
import io.amelia.permission.PermissibleEntity;
import io.amelia.support.UserPrincipal;

public interface UserSubject extends UserPrincipal
{
	/**
	 * Gets the "friendly" name to display of this user. This may include color from the {@link io.amelia.support.EnumColor} class.
	 *
	 * @return the friendly name
	 */
	String getDisplayName();

	UserContext getContext();

	UserEntity getEntity();

	PermissibleEntity getPermissible();
}
