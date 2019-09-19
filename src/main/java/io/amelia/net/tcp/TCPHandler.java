/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.tcp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TCPHandler extends ChannelInboundHandlerAdapter
{
	@Override
	public void channelRead( ChannelHandlerContext ctx, Object msg )
	{
		Packet packet = ( Packet ) msg;


	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause )
	{
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void handlerAdded( ChannelHandlerContext ctx )
	{
		// buf = ctx.alloc().buffer( 4 ); // (1)
	}

	@Override
	public void handlerRemoved( ChannelHandlerContext ctx )
	{
		// buf.release(); // (1)
		// buf = null;
	}
}
