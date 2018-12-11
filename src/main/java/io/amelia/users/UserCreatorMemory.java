/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import io.amelia.events.AbstractEvent;
import io.amelia.events.EventException;
import io.amelia.events.EventHandler;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.facades.Events;
import io.amelia.foundation.facades.Users;
import io.amelia.lang.DescriptiveReason;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.UserException;
import io.amelia.permission.PermissibleEntity;
import io.amelia.permission.PermissionDefault;
import io.amelia.permission.event.PermissibleEntityEvent;
import io.amelia.support.DateAndTime;

class UserCreatorMemory extends UserCreator
{
	public UserCreatorMemory()
	{
		super( "memory", Users.getInstance().getUserCreators().noneMatch( UserCreator::isDefault ) );

		Events.listen( Foundation.getApplication(), PermissibleEntityEvent.class, this::onPermissibleEntityEvent );
	}

	private void onPermissibleEntityEvent( PermissibleEntityEvent event )
	{
		// XXX Prevent the root user from losing it's OP permissions
		if ( event.getAction() == PermissibleEntityEvent.Action.PERMISSIONS_CHANGED )
			if ( BaseUsers.isRootUser( event.getEntity() ) )
			{
				event.getEntity().addPermission( PermissionDefault.OP.getNode(), true, null );
				event.getEntity().setVirtual( true );
			}
	}

	@Override
	public UserContext create( String uuid ) throws UserException.Error
	{
		UserContext context = new UserContext( this, uuid );
		try
		{
			context.setValue( "data", DateAndTime.epoch() );
		}
		catch ( ParcelableException.Error e )
		{
			throw new UserException.Error( context, e );
		}
		return context;
	}

	@Override
	public boolean hasUser( String uuid )
	{
		return BaseUsers.isNullUser( uuid ) || BaseUsers.isRootUser( uuid );
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void load()
	{
		// Do Nothing
	}

	@Override
	public void loginBegin( UserContext userContext, UserPermissible userPermissible, String acctId, Object... credentials )
	{
		// Do Nothing
	}

	@Override
	public void loginFailed( UserResult result )
	{
		// Do Nothing
	}

	@Override
	public void loginSuccess( UserResult result )
	{
		// Do Nothing
	}

	@Override
	public void loginSuccessInit( UserContext userContext, PermissibleEntity permissibleEntity )
	{
		if ( userContext.getCreator() == this && BaseUsers.isRootUser( userContext ) )
		{
			permissibleEntity.addPermission( PermissionDefault.OP.getNode(), true, null );
			permissibleEntity.setVirtual( true );
			// getUserContext.registerAttachment( ApplicationTerminal.terminal() );
		}

		if ( userContext.getCreator() == this && BaseUsers.isNullUser( userContext ) )
			permissibleEntity.setVirtual( true );
	}

	@Override
	public void reload( UserContext userContext )
	{
		// Do Nothing
	}

	@Override
	public UserResult resolve( String uuid )
	{
		return null;
	}

	@Override
	public void save( UserContext userContext )
	{
		// Do Nothing
	}
}
