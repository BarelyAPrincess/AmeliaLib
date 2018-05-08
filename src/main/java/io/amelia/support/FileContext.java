package io.amelia.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class FileContext
{
	private static final Charset DEFAULT_BINARY_CHARSET = Charset.forName( ConfigRegistry.config.getString( ConfigRegistry.Config.DEFAULT_BINARY_CHARSET ) );
	private static final Charset DEFAULT_TEXT_CHARSET = Charset.forName( ConfigRegistry.config.getString( ConfigRegistry.Config.DEFAULT_TEXT_CHARSET ) );

	private final Map<String, String> values = new TreeMap<>();
	private Charset charset = null;
	private ByteBuf content = Unpooled.buffer();
	private String contentType = null;
	private String ext = null;
	private Path filePath = null;
	@Maps.Key( "reqlogin" )
	private boolean reqLogin = false;
	@Maps.Key( "reqperm" )
	private String reqPerm = null;
	@Maps.Key( "title" )
	private String title = null;

	public FileContext( Path filePath ) throws IOException
	{
		readFromFile( filePath );
	}

	public Map<String, String> getAnnotations()
	{
		return Maps.objectToStringMap( this );
	}

	public Charset getCharset()
	{
		if ( charset == null )
		{
			if ( contentType.startsWith( "text" ) )
				charset = DEFAULT_TEXT_CHARSET;
			else
				charset = DEFAULT_BINARY_CHARSET;
		}

		return charset;
	}

	public void setCharset( String charset )
	{
		if ( Charset.isSupported( charset ) )
			this.charset = Charset.forName( charset );
		else
			Kernel.L.warning( "The charset " + charset + " was set but it's not supported by the JVM!" );
	}

	@Maps.Key( "charset" )
	public String getCharsetName()
	{
		return Objs.ifPresentGet( getCharset(), Charset::name );
	}

	public byte[] getContentBytes()
	{
		byte[] bytes = new byte[content.readableBytes()];
		content.getBytes( content.readerIndex(), bytes );
		return bytes;
	}

	public String getContentString()
	{
		return new String( getContentBytes(), charset );
	}

	@Maps.Key( "contenttype" )
	public String getContentType()
	{
		if ( contentType == null && filePath != null )
			contentType = ContentTypes.getContentTypes( filePath ).findFirst().orElse( null );

		if ( contentType == null )
			contentType = "application/octet-stream";

		getCharset();

		return contentType;
	}

	@Maps.Key( "ext" )
	public String getExt()
	{
		if ( ext == null )
			ext = ExtTypes.getExtTypes( filePath ).findFirst().orElse( null );
		return ext;
	}

	public Path getFilePath()
	{
		return filePath;
	}

	@Maps.Key( "file" )
	public String getFilePathRel( Path relTo )
	{
		return IO.relPath( filePath, relTo );
	}

	public String getTitle()
	{
		return title;
	}

	public String getValue( @Nonnull String key )
	{
		return values.get( key.toLowerCase() );
	}

	public boolean hasFilePath()
	{
		return filePath != null && Files.exists( filePath );
	}

	public void putValue( @Nonnull String key, @Nullable String value )
	{
		key = key.toLowerCase();

		if ( key.equals( "charset" ) )
			setCharset( value );
		else if ( key.equals( "contentType" ) )
			contentType = value;
		else if ( key.equals( "ext" ) )
			ext = value;
		else if ( key.equals( "reqlogin" ) )
			reqLogin = Objs.castToBoolean( value );
		else if ( key.equals( "reqperm" ) )
			reqPerm = value;
		else if ( key.equals( "title" ) )
			title = value;
		else if ( Objs.isEmpty( value ) )
			values.remove( key );
		else
			values.put( key, value );
	}

	public void readFromFile( Path filePath ) throws IOException
	{
		if ( !Files.exists( filePath ) )
			throw new FileNotFoundException( "File must exist." );

		this.filePath = filePath;

		if ( Files.isDirectory( filePath ) )
		{
			ext = "directory";
			return;
		}

		InputStream in = null;
		try
		{
			in = Files.newInputStream( filePath );
			ByteBuf inBuf = IO.readStreamToByteBuf( in );
			ByteBuf outBuf = Unpooled.buffer();

			int lastInx;
			int lineCount = 0;

			for ( ; ; )
			{
				lastInx = inBuf.readerIndex();
				String line = IO.readLine( inBuf );
				if ( line == null )
					break;
				line = line.trim();

				if ( line.startsWith( "@" ) )
					try
					{
						// Temporary solution for CSS files since they too use @annotations - so we'll share them for now.
						if ( ContentTypes.isContentType( filePath, "css" ) )
							outBuf.writeBytes( ( line + "\n" ).getBytes() );
						else
							lineCount++;

						String key;
						String val = "";

						if ( line.contains( " " ) )
						{
							key = line.substring( 1, line.indexOf( " " ) );
							val = line.substring( line.indexOf( " " ) + 1 );
						}
						else
							key = line;

						if ( val.endsWith( ";" ) )
							val = val.substring( 0, val.length() - 1 );

						if ( val.startsWith( "\"" ) && val.endsWith( "\"" ) )
							val = val.substring( 1, val.length() - 1 );

						if ( val.startsWith( "'" ) && val.endsWith( "'" ) )
							val = val.substring( 1, val.length() - 1 );

						Kernel.L.fine( "Reading annotation " + key + " with value " + val + " for file " + IO.relPath( filePath ) );
						putValue( key, val );
					}
					catch ( NullPointerException | ArrayIndexOutOfBoundsException e )
					{
						// Ignore
					}
				else if ( line.length() == 0 )
					lineCount++;
					// Continue, empty line
				else
				{
					// We've encountered the beginning of the content, notify the look that we can quit now.
					inBuf.readerIndex( lastInx ); // This rewinds the buffer to the last reader index
					break;
				}
			}

			// Write empty line returns so script exceptions still match up to their source file
			outBuf.writeBytes( Strs.repeat( "\n", lineCount ).getBytes() );

			// Write remaining data to output
			outBuf.writeBytes( inBuf );
			this.content = outBuf;
		}
		finally
		{
			IO.closeQuietly( in );
		}
	}
}
