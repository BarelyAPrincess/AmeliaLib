package io.amelia.users;

import io.amelia.support.UserPrincipal;

public interface UserAttachment extends UserPrincipal//, MessageReceiver, CommandSender
{
	String getIpAddress();

	UserPermissible getPermissible();
}
