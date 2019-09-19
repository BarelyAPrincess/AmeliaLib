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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ConfigException;

public class ExtTypes
{
	public static void clearType( String ext ) throws ConfigException.Error
	{
		getConfigData().childDestroy( ext );
	}

	public static Stream<String> getAllTypes()
	{
		return getConfigData().getChildren().map( ContainerWithValue::getValue ).filter( Voluntary::isPresent ).map( Voluntary::get ).map( Objs::castToString ).flatMap( str -> Strs.split( str, "," ) );
	}

	private static ConfigData getConfigData()
	{
		return ConfigRegistry.config.getChildOrCreate( ConfigRegistry.ConfigKeys.EXT_TYPES );
	}

	@Nonnull
	public static Stream<String> getExtTypes( @Nonnull String filename )
	{
		String ext = Strs.regexCapture( filename, "\\.(\\w+)$" );
		return Stream.concat( getConfigData().getChildren().filter( child -> child.getLocalName().equalsIgnoreCase( ext ) && child.hasValue() ).flatMap( child -> Strs.split( child.getString().get(), "," ) ), Stream.of( "application/octet-stream" ) );
	}

	@Nonnull
	public static Stream<String> getExtTypes( @Nonnull File file )
	{
		if ( file.isDirectory() )
			return Stream.of( "directory" );

		return getExtTypes( file.getName() );
	}

	@Nonnull
	public static Stream<String> getExtTypes( @Nonnull Path path )
	{
		if ( Files.isDirectory( path ) )
			return Stream.of( "directory" );

		return getExtTypes( path.getFileName().toString() );
	}

	public static boolean isExtType( @Nonnull Path path, String test )
	{
		return getExtTypes( path ).anyMatch( contentType -> contentType.contains( test ) );
	}

	public static void setType( String ext, String type ) throws ConfigException.Error
	{
		ConfigData map = getConfigData().getChildOrCreate( ext );
		if ( map.hasValue() )
			map.setValue( map.getString().orElse( null ) + "," + type );
		else
			map.setValue( type );
	}

	private ExtTypes()
	{
		// Static Access
	}
}
