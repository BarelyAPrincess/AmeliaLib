/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.wip;

import java.util.function.Supplier;
import io.amelia.net.wip.udp.UDPPacketHandler;

/**
 * The HoneyPotServer clustering feature is very unique when compared to other clustering mechanisms.
 * <p>
 * First off the application makes use of a decentralized voting system where one member is always selected
 * as the MASTER with the power to veto any action the cluster might take. The role of MASTER is very important
 * to a healthy responsive cluster, so the role could be transferred at anytime to another member of the cluster.
 * Reasons for a transfer of power would include low-latency and the instance going silent, no IAH packets.
 * <p>
 * Each cluster member consistency records the latency seen by the cluster members. Once the MASTER has gone
 * silent or deemed lagging, each member will broadcast there latencies in a timeout/lagging acknowledgement
 * packet, these packets are then received by their peers for analyzing. Each member will collect these latency
 * reports including its own and compile a rank based on the results, then broadcast these rankings. Once
 * each member has gathered all the ranking reports, the new MASTER is selected based on a majority. The
 * MEMBER who won the vote, will then send a MASTER acknowledgement packet.
 * <p>
 * When new members wishes to join, it will broadcast a cluster status packet, asking for each MEMBER of the cluster
 * to acknowledge itself and other key details such as the software version number. If the new MEMBER is running an
 * older version of the software it will notify the log and shutdown (unless auto-updates are enabled and the update
 * is only a minor version. Major version will always shutdown and request they are manually updated with the --update
 * argument.) If the new MEMBER is running a newer version, it will enter limbo (virtually creating a second cluster
 * within a cluster) awaiting old members to leave the cluster and update. Once all old MEMBERS leave, a new MASTER
 * vote takes place making the new virtual cluster the primary. This method ensures that when a cluster-wide software
 * update takes place each member has a fair amount of time to shutdown, install, and reboot.
 * Depending if the update was expected, the old MEMBERS will try to coordinate with the new MEMBERS for a peaceful
 * takeover.
 */
public enum ClusterRole
{
	/**
	 * Watching UDP traffic and sending queries to the cluster members.
	 */
	MONITOR( UDPPacketHandler::new ),
	/**
	 * Negotiating with the cluster, waiting to be promoted to a member of the cluster.
	 */
	NEGOTIATING( UDPPacketHandler::new ),
	/**
	 * Actively a voting member of the cluster.
	 */
	MEMBER( UDPPacketHandler::new ),
	/**
	 * Actively the master member of the cluster.
	 */
	MASTER( UDPPacketHandler::new );

	final Supplier<UDPPacketHandler> packetHandlerSupplier;

	ClusterRole( Supplier<UDPPacketHandler> packetHandlerSupplier )
	{
		this.packetHandlerSupplier = packetHandlerSupplier;
	}

	/**
	 * Produces a new UDP Packet Handler for use on this UDPHandler.
	 *
	 * @return new UDPPacketHandler instance;
	 */
	public UDPPacketHandler newPacketHandler()
	{
		return packetHandlerSupplier.get();
	}
}
