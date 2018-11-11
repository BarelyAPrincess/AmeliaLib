/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ConfigException;

/**
 * Provides an easy translator for content-types specified from configuration.
 */
public class ContentTypes
{
	public static void clearType( String ext ) throws ConfigException.Error
	{
		getConfigMap().destroyChild( ext );
	}

	public static Stream<String> getAllTypes()
	{
		return getConfigMap().getChildren().map( ContainerWithValue::getValue ).filter( Voluntary::isPresent ).map( Voluntary::get ).map( Objs::castToString );
	}

	private static ConfigData getConfigMap()
	{
		return ConfigRegistry.config.getChildOrCreate( ConfigRegistry.ConfigKeys.CONTENT_TYPES );
	}

	@Nonnull
	public static Stream<String> getContentTypes( @Nonnull Path path )
	{
		if ( Files.isDirectory( path ) )
			return Stream.of( "directory" );

		return getContentTypes( path.getFileName().toString() );
	}

	@Nonnull
	public static Stream<String> getContentTypes( @Nonnull String filename )
	{
		String ext = Strs.regexCapture( filename, "\\.(\\w+)$" );
		return Stream.concat( getConfigMap().getChildren().filter( child -> child.getName().equalsIgnoreCase( ext ) && child.hasValue() ).flatMap( child -> Strs.split( child.getString().get(), "," ) ), Stream.of( "application/octet-stream" ) );
	}

	@Nonnull
	public static Stream<String> getContentTypes( @Nonnull File file )
	{
		if ( file.isDirectory() )
			return Stream.of( "directory" );

		return getContentTypes( file.getName() );
	}

	public static boolean isContentType( Path path, String test )
	{
		return getContentTypes( path ).anyMatch( contentType -> contentType.contains( test ) );
	}

	public static void setType( String ext, String type ) throws ConfigException.Error
	{
		ConfigData map = getConfigMap().getChildOrCreate( ext );
		if ( map.hasValue() )
			map.setValue( map.getString().get() + "," + type );
		else
			map.setValue( type );
	}

	private ContentTypes()
	{
		// Static Access
	}
}
