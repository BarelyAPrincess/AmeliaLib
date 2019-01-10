/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.apache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.amelia.support.IO;

public class ApacheConfiguration extends ApacheSection
{
	public ApacheConfiguration()
	{

	}

	public ApacheConfiguration( Path sourcePath ) throws IOException
	{
		if ( Files.exists( sourcePath ) )
			if ( Files.isDirectory( sourcePath ) )
				appendWithDir( sourcePath );
			else
				appendWithFile( sourcePath );

	}

	public ApacheConfiguration( String text ) throws IOException
	{
		appendRaw( text, "<source unknown>" );
	}

	public ApacheConfiguration appendWithDir( Path dir ) throws IOException
	{
		if ( Files.isDirectory( dir ) )
		{
			Path htaccessFile = dir.resolve( ".htaccess" );
			if ( Files.isRegularFile( htaccessFile ) )
				appendWithFile( htaccessFile );

			htaccessFile = dir.resolve( "htaccess" );
			if ( Files.isRegularFile( htaccessFile ) )
				appendWithFile( htaccessFile );
		}

		return this;
	}

	public ApacheConfiguration appendWithFile( Path file ) throws FileNotFoundException
	{
		if ( Files.isRegularFile( file ) )
			try ( BufferedReader br = new BufferedReader( new FileReader( file.toFile() ) ) )
			{
				appendRaw( br, file.toRealPath().toString() );
				IO.closeQuietly( br );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

		return this;
	}
}
