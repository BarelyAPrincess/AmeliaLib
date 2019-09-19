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

public abstract class PacketResponse<T extends PacketRequest> extends RawPacket
{
	public PacketResponse( T requestPacket )
	{
		super( requestPacket.getPacketId() );
	}
}
