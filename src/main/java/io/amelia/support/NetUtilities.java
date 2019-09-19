/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import io.amelia.lang.PacketDecodeException;
import io.netty.buffer.ByteBuf;

public class NetUtilities
{
	public static byte[] readBytesFromByteBuf( int length, ByteBuf in )
	{
		byte[] bytes = new byte[length];
		for ( int i = 0; i < length; i++ )
			bytes[i] = in.readByte();
		return bytes;
	}

	public static String readStringFromByteBuf( ByteBuf in ) throws PacketDecodeException
	{
		byte startByte = in.getByte( in.readerIndex() );
		if ( startByte != 0x1b )
			throw new PacketDecodeException( "ByteBuf does not properly start for reading a string. {expected=0x1b,found=0x" + Byte.toString( startByte ) + "}" );
		in.skipBytes( 1 );
		return new String( readBytesFromByteBuf( in.readInt(), in ) );
	}

	public static void writeStringToByteBuf( String stringVar, ByteBuf out )
	{
		byte[] bytes = stringVar.getBytes();
		out.writeByte( 0x1b ); // Method Identifier
		out.writeInt( bytes.length ); // String Length
		out.writeBytes( bytes ); // String Data
	}
}
