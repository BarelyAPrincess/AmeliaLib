/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

import javax.annotation.Nonnull;

import io.amelia.foundation.RegistrarBase;

public class AuthorNagEvent extends AbstractEvent
{
	private final String message;
	private final RegistrarBase registrarBase;

	public AuthorNagEvent( @Nonnull RegistrarBase registrarBase, @Nonnull String message )
	{
		this.registrarBase = registrarBase;
		this.message = message;
	}

	public RegistrarBase getRegistrarBase()
	{
		return registrarBase;
	}

	public String getMessage()
	{
		return message;
	}
}
