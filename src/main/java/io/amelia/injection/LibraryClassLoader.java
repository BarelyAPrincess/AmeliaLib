/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.injection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Acts as the classloader for libraries downloaded from Maven.
 */
@SuppressWarnings( {"unchecked", "rawtypes"} )
public class LibraryClassLoader
{
	private static final Class[] parameters = new Class[] {URL.class};

	public static void addPath( File file ) throws IOException
	{
		addPath( file.toURI().toURL() );
	}

	public static void addPath( Path path ) throws IOException
	{
		addPath( path.toUri().toURL() );
	}

	public static void addPath( String path ) throws IOException
	{
		addPath( Paths.get( path ) );
	}

	public static void addPath( URL url ) throws IOException
	{
		URLClassLoader sysloader = ( URLClassLoader ) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		try
		{
			Method method = sysclass.getDeclaredMethod( "addURL", parameters );
			method.setAccessible( true );
			method.invoke( sysloader, url );
		}
		catch ( Throwable t )
		{
			throw new IOException( String.format( "Error, could not add path '%s' to system classloader", url.toString() ), t );
		}

	}

	public static boolean isPathLoaded( Path path ) throws MalformedURLException
	{
		return isPathLoaded( path.toUri().toURL() );
	}

	public static boolean isPathLoaded( File file ) throws MalformedURLException
	{
		return isPathLoaded( file.toURI().toURL() );
	}

	public static boolean isPathLoaded( String path ) throws MalformedURLException
	{
		return isPathLoaded( Paths.get( path ) );
	}

	public static boolean isPathLoaded( URL url )
	{
		URLClassLoader sysloader = ( URLClassLoader ) ClassLoader.getSystemClassLoader();
		return Arrays.asList( sysloader.getURLs() ).contains( url );
	}
}
