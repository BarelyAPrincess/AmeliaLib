/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.wip.packets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.NetworkException;
import io.amelia.support.SupplierWithException;

public class PacketValidationException extends NetworkException.Error
{
	public static <Rtn> Rtn tryCatch( @Nonnull RawPacket packet, SupplierWithException<Rtn, Exception> fn ) throws PacketValidationException
	{
		return tryCatch( packet, fn, null );
	}

	public static <Rtn> Rtn tryCatch( @Nonnull RawPacket packet, SupplierWithException<Rtn, Exception> fn, @Nullable String detailMessage ) throws PacketValidationException
	{
		try
		{
			return fn.get();
		}
		catch ( Exception e )
		{
			if ( detailMessage == null )
				throw new PacketValidationException( packet, e );
			else
				throw new PacketValidationException( packet, detailMessage, e );
		}
	}

	private final RawPacket packet;

	public PacketValidationException( @Nonnull RawPacket packet, @Nonnull String message )
	{
		super( message );
		this.packet = packet;
	}

	public PacketValidationException( @Nonnull RawPacket packet, @Nonnull String message, @Nonnull Throwable cause )
	{
		super( message, cause );
		this.packet = packet;
	}

	public PacketValidationException( @Nonnull RawPacket packet, @Nonnull Throwable cause )
	{
		super( cause );
		this.packet = packet;
	}

	public RawPacket getPacket()
	{
		return packet;
	}
}
