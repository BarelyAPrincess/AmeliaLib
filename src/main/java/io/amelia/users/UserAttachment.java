package io.amelia.users;

import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.foundation.EntityPrincipal;

public interface UserAttachment extends EntityPrincipal, ParcelReceiver //, CommandSender
{
	String getIpAddress();

	UserPermissible getPermissible();
}
