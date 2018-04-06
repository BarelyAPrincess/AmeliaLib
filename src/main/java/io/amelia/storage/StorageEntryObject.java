package io.amelia.storage;

import java.io.IOException;

import javax.annotation.Nonnull;

import io.amelia.support.NIO;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class StorageEntryObject extends StorageEntry
{
	private ByteBuf content;

	public StorageEntryObject( @Nonnull StorageContext storageContext ) throws IOException
	{
		super( storageContext );

		content = storageContext.getContent();
	}

	public ByteBuf getContent()
	{
		return content.duplicate();
	}

	public void setContent( @Nonnull ByteBuf content )
	{
		content = Unpooled.wrappedBuffer( content );

		fields.put( SIZE, String.valueOf( content.readableBytes() ) );

		this.content = content;
	}

	public String getContentAsString()
	{
		return NIO.decodeStringFromByteBuf( content.duplicate() );
	}

	@Override
	public boolean isContainer()
	{
		return false;
	}
}
