package io.amelia.support;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.StackerWithValue;
import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistry;

public class ExtTypes
{
	public static void clearType( String ext )
	{
		getConfigMap().destroyChild( ext );
	}

	public static Stream<String> getAllTypes()
	{
		return getConfigMap().getChildren().map( StackerWithValue::getValue ).filter( Optional::isPresent ).map( Optional::get ).map( Objs::castToString ).flatMap( str -> Strs.split( str, "," ) );
	}

	private static ConfigMap getConfigMap()
	{
		return ConfigRegistry.config.getChildOrCreate( ConfigRegistry.Config.EXT_TYPES );
	}

	@Nonnull
	public static Stream<String> getExtTypes( @Nonnull String filename )
	{
		String ext = Strs.regexCapture( filename, "\\.(\\w+)$" );
		return Stream.concat( getConfigMap().getChildren().filter( child -> child.getName().equalsIgnoreCase( ext ) && child.hasValue() ).flatMap( child -> Strs.split( child.getString().get(), "," ) ), Stream.of( "application/octet-stream" ) );
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

	public static void setType( String ext, String type )
	{
		ConfigMap map = getConfigMap().getChildOrCreate( ext );
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
