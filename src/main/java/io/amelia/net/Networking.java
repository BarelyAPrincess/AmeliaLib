/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;

import io.amelia.data.TypeBase;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.net.ssl.SslRegistry;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.ConcurrentSet;

public class Networking
{
	public static final Kernel.Logger L = Kernel.getLogger( Networking.class );

	public static final EventLoopGroup IO_LOOP_GROUP = new NioEventLoopGroup( 0, Executors.newCachedThreadPool( new ThreadFactoryBuilder().setNameFormat( "Netty Client IO #%d" ).setDaemon( true ).build() ) );
	public static final Set<NetworkService> NETWORK_SERVICE_LIST = new ConcurrentSet<>();

	static
	{
		if ( Security.getProvider( "BC" ) == null )
			Security.addProvider( new BouncyCastleProvider() );
	}

	public static Optional<NetworkService> getService( String id )
	{
		return NETWORK_SERVICE_LIST.stream().filter( service -> id.equalsIgnoreCase( service.getId() ) ).findFirst();
	}

	public static <Service extends NetworkService> Optional<Service> getService( Class<Service> serviceClass )
	{
		return NETWORK_SERVICE_LIST.stream().filter( service -> serviceClass.isAssignableFrom( service.getClass() ) ).map( service -> ( Service ) service ).findFirst();
	}

	public static void heartbeat( long currentTicks )
	{
		// Ignore
	}

	/* public static void start() throws ApplicationException.Error
	{
		startNetworkService( new WebService() );
	} */

	public static void startNetworkService( NetworkService networkService ) throws ApplicationException.Error
	{
		NETWORK_SERVICE_LIST.add( networkService );
		networkService.start();
	}

	public static void stop()
	{
		NETWORK_SERVICE_LIST.forEach( NetworkService::shutdown );
		NETWORK_SERVICE_LIST.clear();
		IO_LOOP_GROUP.shutdownGracefully();
	}

	private Networking()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		public final static TypeBase NET_BASE = new TypeBase( "net" );

		public final static TypeBase SESSION_BASE = new TypeBase( NET_BASE, "sessions" );
		public final static TypeBase.TypeBoolean SESSION_DEBUG = new TypeBase.TypeBoolean( SESSION_BASE, "debug", false );
		public final static TypeBase SESSION_TIMEOUT = new TypeBase( SESSION_BASE, "timeout" );
		public final static TypeBase.TypeInteger SESSION_TIMEOUT_DEFAULT = new TypeBase.TypeInteger( SESSION_TIMEOUT, "default", 3600 );
		public final static TypeBase.TypeInteger SESSION_TIMEOUT_LOGIN = new TypeBase.TypeInteger( SESSION_TIMEOUT, "login", 86400 );
		public final static TypeBase.TypeInteger SESSION_TIMEOUT_EXTENDED = new TypeBase.TypeInteger( SESSION_TIMEOUT, "extended", 604800 );
		public final static TypeBase.TypeString SESSION_COOKIE_NAME = new TypeBase.TypeString( SESSION_BASE, "defaultCookieName", "SessionId" );
		public final static TypeBase.TypeInteger SESSION_MAX_PER_IP = new TypeBase.TypeInteger( SESSION_BASE, "maxSessionsPerIP", 6 );
		public final static TypeBase.TypeInteger SESSION_CLEANUP_INTERVAL = new TypeBase.TypeInteger( SESSION_BASE, "cleanupInterval", 5 );

		public final static TypeBase SECURITY_BASE = new TypeBase( NET_BASE, "security" );
		public final static TypeBase.TypeBoolean SECURITY_DISABLE_REQUEST = new TypeBase.TypeBoolean( SECURITY_BASE, "disableRequestData", false );
	}
}
