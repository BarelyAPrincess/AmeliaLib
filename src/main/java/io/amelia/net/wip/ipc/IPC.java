/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.wip.ipc;

import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.lang.ParcelableException;
import io.amelia.looper.LooperRouter;
import io.amelia.net.wip.NetworkLoader;
import io.amelia.net.wip.packets.PacketRequestInfo;
import io.amelia.net.wip.packets.PacketRequestStop;
import io.amelia.net.wip.packets.PacketValidationException;
import io.amelia.net.wip.udp.UDPWorker;

public class IPC
{
	/**
	 * temp?
	 */
	public static void processIncomingParcel( Parcel src ) throws ParcelableException.Error
	{
		LooperRouter.sendParcel( Parcel.Factory.deserialize( src, ParcelCarrier.class ) );
	}

	public static void start() throws PacketValidationException
	{
		udp().sendPacket( new PacketRequestInfo( () -> null ), ( request, response ) -> {

		} );
	}

	public static void status() throws PacketValidationException
	{
		udp().sendPacket( new PacketRequestInfo( () -> null ), ( request, response ) -> {
			//Kernel.L.info( "Found Instance: " + r.instanceId + " with IP " + r.ipAddress );
		} );
	}

	public static void stop( String instanceId )
	{
		try
		{
			udp().sendPacket( new PacketRequestStop( instanceId ), ( request, response ) -> {

			} );
		}
		catch ( PacketValidationException e )
		{

		}
	}

	private static UDPWorker udp()
	{
		return NetworkLoader.UDP();
	}

	private IPC()
	{
		// Static Class
	}
}
