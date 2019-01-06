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

import java.util.UUID;

import io.amelia.events.Events;
import io.amelia.foundation.Foundation;
import io.amelia.lang.ParcelableException;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.permissions.PermissionDefault;
import io.amelia.permissions.event.PermissibleEntityEvent;
import io.amelia.support.DateAndTime;

class UserCreatorMemory extends UserCreator
{
	public UserCreatorMemory()
	{
		super( "memory", Foundation.getUsers().getUserCreators().noneMatch( UserCreator::isDefault ) );

		Events.getInstance().listen( Foundation.getApplication(), PermissibleEntityEvent.class, this::onPermissibleEntityEvent );
	}

	private void onPermissibleEntityEvent( PermissibleEntityEvent event )
	{
		// XXX Prevent the root user from losing it's OP permissions
		if ( event.getAction() == PermissibleEntityEvent.Action.PERMISSIONS_CHANGED )
			if ( Foundation.getUsers().isRootUser( event.getEntity() ) )
			{
				event.getEntity().addPermission( PermissionDefault.OP.getNode(), true, null );
				event.getEntity().setVirtual( true );
			}
	}

	@Override
	public UserContext create( UUID uuid ) throws UserException.Error
	{
		UserContext context = new UserContext( this, uuid, true );
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
	public boolean hasUser( UUID uuid )
	{
		return Foundation.getUsers().isNullEntity( uuid ) || Foundation.getUsers().isRootUser( uuid );
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
	public void loginBegin( UserContext userContext, UserPermissible userPermissible, UUID uuid, Object... credentials )
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
		if ( userContext.getCreator() == this && Foundation.getUsers().isRootUser( userContext ) )
		{
			permissibleEntity.addPermission( PermissionDefault.OP.getNode(), true, null );
			permissibleEntity.setVirtual( true );
			// getUserContext.registerAttachment( ApplicationTerminal.terminal() );
		}

		if ( userContext.getCreator() == this && Foundation.getUsers().isNullEntity( userContext ) )
			permissibleEntity.setVirtual( true );
	}

	@Override
	public void reload( UserContext userContext )
	{
		// Do Nothing
	}

	@Override
	public UserResult resolve( UUID uuid )
	{
		return null;
	}

	@Override
	public void save( UserContext userContext )
	{
		// Do Nothing
	}
}
