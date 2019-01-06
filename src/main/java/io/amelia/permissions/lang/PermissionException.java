/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.lang;

import java.sql.SQLException;

import io.amelia.lang.ApplicationException;

public class PermissionException extends ApplicationException.Error
{
	private static final long serialVersionUID = -7126640838300697969L;

	public PermissionException( SQLException e )
	{
		super( e );
	}

	public PermissionException( String message )
	{
		super( message );
	}
}
