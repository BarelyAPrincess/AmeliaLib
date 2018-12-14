package io.amelia.users;

import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.data.parcel.ParcelSender;
import io.amelia.permission.PermissibleEntity;
import io.amelia.support.UserPrincipal;

public interface UserSubject extends UserPrincipal
{
	UserContext getContext();

	UserEntity getEntity();

	PermissibleEntity getPermissible();
}
