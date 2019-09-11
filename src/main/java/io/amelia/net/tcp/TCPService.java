package io.amelia.net.tcp;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.StartupException;
import io.amelia.looper.Delays;
import io.amelia.looper.LooperRouter;
import io.amelia.net.NetworkService;
import io.amelia.net.Networking;
import io.amelia.support.NIO;
import io.amelia.support.WeakReferenceList;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TCPService implements NetworkService
{
	private Channel tcpChannel;
	private InetSocketAddress tcpSocket;

	@Override
	public String getId()
	{
		return "tcp";
	}

	@Override
	public void shutdown()
	{

	}

	public boolean isTCPRunning()
	{
		// TODO
		return true;
	}

	@Override
	public void start() throws ApplicationException.Error
	{
		int tcpPort = ConfigRegistry.config.getValue( ConfigKeys.TCP_PORT );

		if ( tcpPort <= 0 )
			throw new RuntimeException( "Invalid Port Number" );

		if ( NIO.isPrivilegedPort( tcpPort ) )
		{
			Networking.L.warning( "It would seem that you are trying to start the TCP Service on a privileged port without root access." );
			Networking.L.warning( "We will attempt to start the service but we can't guarantee it's success. http://www.w3.org/Daemon/User/Installation/PrivilegedPorts.html" );
		}

		tcpSocket = new InetSocketAddress( tcpPort );

		try
		{
			if ( tcpPort > 0 )
			{
				Networking.L.info( "Starting TCP Service on port " + tcpPort + "!" );

				ServerBootstrap serverBootstrap = new ServerBootstrap();
				serverBootstrap.group( Networking.IO_LOOP_GROUP ).channel( NioServerSocketChannel.class ).childHandler( new TCPInitializer() );

				tcpChannel = serverBootstrap.bind( tcpSocket ).sync().channel();

				Kernel.getExecutorParallel().execute( () -> {
					try
					{
						tcpChannel.closeFuture().sync();
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
					}

					Networking.L.info( "The TCP Service has been shutdown!" );
				} );
			}
		}
		catch ( NullPointerException e )
		{
			throw new StartupException( "There was a problem starting the TCP Service. Check logs and try again.", e );
		}
		catch ( Throwable e )
		{
			if ( e instanceof ExceptionContext )
				ExceptionReport.handleSingleException( e );
			else
				throw e instanceof StartupException ? ( StartupException ) e : new StartupException( e );
		}

		LooperRouter.getMainLooper().postTaskRepeatingLater( entry -> {
			for ( WeakReference<SocketChannel> ref : TCPInitializer.activeChannels )
				if ( ref.get() == null )
					TCPInitializer.activeChannels.remove( ref );
		}, Delays.SECOND_15, Delays.SECOND_15, true );
	}

	public static class ConfigKeys
	{
		public static final TypeBase ROOT_BASE = new TypeBase( Networking.ConfigKeys.NET_BASE, "tcp" );

		public static final TypeBase.TypeInteger TCP_PORT = new TypeBase.TypeInteger( ROOT_BASE, "TCPPort", 5454 );
	}
}
