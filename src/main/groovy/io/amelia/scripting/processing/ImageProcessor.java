/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.processing;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.scripting.ScriptingOption;
import io.amelia.scripting.ScriptingProcessor;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.netty.buffer.ByteBufInputStream;

/**
 * Allows for image manipulation to be applied to an image before it's outputted.
 */
public class ImageProcessor implements ScriptingProcessor
{
	public static class Options
	{
		public static final ScriptingOption.Int width = new ScriptingOption.Int( "width", -1, "x", "w" );
		public static final ScriptingOption.Int height = new ScriptingOption.Int( "height", -1, "y", "h" );
		public static final ScriptingOption.Bool thumb = new ScriptingOption.Bool( "thumb", false );
	}

	@Override
	public void postEvaluate( ScriptingContext scriptingContext )
	{
		return;

		/* try
		{
			if ( scriptingContext.getContentType() == null && !scriptingContext.getContentType().toLowerCase().startsWith( "image" ) )
				return;

			float x = -1;
			float y = -1;

			boolean cacheEnabled = ConfigRegistry.config.getValue( ScriptingFactory.Config.PROCESSORS_IMAGES_CACHE );
			boolean grayscale = false;

			x = request.getArgumentInt( "width" ).orElse( x );
			y = request.getArgumentInt( "height" ).orElse( y );
			x = request.getArgumentInt( "x" ).orElse( x );
			y = request.getArgumentInt( "y" ).orElse( y );
			x = request.getArgumentInt( "w" ).orElse( x );
			y = request.getArgumentInt( "h" ).orElse( y );

			ScriptingContext.DefinedScriptingOption option = scriptingContext.getOption( Options.thumb ).orElse( null );
			if ( isTrue() )
			{
				x = 150;
				y = 0;
			}
			else
			{
				x = scriptingContext.getOption( width ).getInt();
				y = scriptingContext.getOption( height ).getInt();
			}

			if ( request.hasArgument( "bw" ) || request.hasArgument( "grayscale" ) )
				grayscale = true;

			// Test if our Post Processor can process the current image
			List<String> readerFormats = Arrays.asList( ImageIO.getReaderFormatNames() );
			List<String> writerFormats = Arrays.asList( ImageIO.getWriterFormatNames() );
			if ( scriptingContext.getContentType() != null && !readerFormats.contains( scriptingContext.getContentType().split( "/" )[1].toLowerCase() ) )
				return;

			int inx = scriptingContext.getBuffer().readerIndex();
			BufferedImage img = ImageIO.read( new ByteBufInputStream( scriptingContext.getBuffer() ) );
			scriptingContext.getBuffer().readerIndex( inx );

			if ( img == null )
				return;

			float w = img.getWidth();
			float h = img.getHeight();
			float w1 = w;
			float h1 = h;

			if ( x < 1 && y < 1 )
			{
				x = w;
				y = h;
			}
			else if ( x > 0 && y < 1 )
			{
				w1 = x;
				h1 = x * ( h / w );
			}
			else if ( y > 0 && x < 1 )
			{
				w1 = y * ( w / h );
				h1 = y;
			}
			else if ( x > 0 && y > 0 )
			{
				w1 = x;
				h1 = y;
			}

			boolean resize = w1 > 0 && h1 > 0 && w1 != w && h1 != h;
			boolean argb = request.hasArgument( "argb" ) && request.getArgument( "argb" ).length() == 8;

			if ( !resize && !argb && !grayscale )
				return;

			// Produce a unique encapsulated id based on this request
			String encapId = Encrypt.sha1( scriptingContext.getFileName() + w1 + h1 + request.getArgument( "argb" ) + grayscale );
			Path tempFile = Paths.get( encapId + "_" + scriptingContext.getFileName().toLowerCase() ).resolve( scriptingContext.getCachePath() );

			if ( cacheEnabled && Files.isRegularFile( tempFile ) )
			{
				scriptingContext.resetAndWrite( IO.readStreamToBytes( Files.newInputStream( tempFile ) ) );
				return;
			}

			Image image = resize ? img.getScaledInstance( Math.round( w1 ), Math.round( h1 ), ConfigRegistry.config.getBoolean( "advanced.processors.useFastGraphics" ).orElse( true ) ? Image.SCALE_FAST : Image.SCALE_SMOOTH ) : img;

			// TODO Report malformed parameters to user

			if ( argb )
			{
				FilteredImageSource filteredSrc = new FilteredImageSource( image.getSource(), new RGBColorFilter( ( int ) Long.parseLong( request.getArgument( "argb" ), 16 ) ) );
				image = Toolkit.getDefaultToolkit().createImage( filteredSrc );
			}

			BufferedImage rtn = new BufferedImage( Math.round( w1 ), Math.round( h1 ), img.getType() );
			Graphics2D graphics = rtn.createGraphics();
			graphics.drawImage( image, 0, 0, null );
			graphics.dispose();

			if ( grayscale )
			{
				ColorConvertOp op = new ColorConvertOp( ColorSpace.getInstance( ColorSpace.CS_GRAY ), null );
				op.filter( rtn, rtn );
			}

			if ( resize )
				ScriptingFactory.L.info( EnumColor.GRAY + "Image resized from " + Math.round( w ) + "px by " + Math.round( h ) + "px to " + Math.round( w1 ) + "px by " + Math.round( h1 ) + "px" );

			if ( rtn != null )
			{
				ByteArrayOutputStream bs = new ByteArrayOutputStream();

				if ( scriptingContext.getContentType() != null && writerFormats.contains( scriptingContext.getContentType().split( "/" )[1].toLowerCase() ) )
					ImageIO.write( rtn, scriptingContext.getContentType().split( "/" )[1].toLowerCase(), bs );
				else
					ImageIO.write( rtn, "png", bs );

				if ( cacheEnabled )
				{
					OutputStream out = Files.newOutputStream( tempFile );
					out.write( bs.toByteArray() );
					IO.closeQuietly( out );
				}

				scriptingContext.resetAndWrite( bs.toByteArray() );
			}
		}
		catch ( Throwable e )
		{
			e.printStackTrace();
		} */
	}

	@Override
	public void preEvaluate( ScriptingContext scriptingContext )
	{

	}

	static class RGBColorFilter extends RGBImageFilter
	{
		private final int filter;

		RGBColorFilter( int filter )
		{
			this.filter = filter;
			canFilterIndexColorModel = true;
		}

		@Override
		public int filterRGB( int x, int y, int rgb )
		{
			return rgb & filter;
		}
	}
}
