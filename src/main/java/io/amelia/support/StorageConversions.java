/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.lang.StorageException;

public class StorageConversions
{
	@SuppressWarnings( "unchecked" )
	public static <Stacker extends ContainerWithValue> void loadToStacker( @Nonnull Path path, @Nonnull Stacker stacker, @Nonnull String nestingPrefix ) throws StorageException.Error
	{
		Streams.forEachWithException( Exceptions.tryCatch( () -> Files.list( path ), exp -> new StorageException.Error( "Failed to load configuration " + IO.relPath( path ), exp ) ), nextPath -> {
			String newNestingPrefix = Strs.join( new String[] {nestingPrefix, IO.getLocalName( nextPath )}, "." );

			try
			{
				if ( Files.isDirectory( nextPath ) )
					loadToStacker( nextPath, stacker, newNestingPrefix );
				else
				{
					String content = IO.readFileToString( nextPath );
					Map<String, Object> map;

					// TODO Add more supported types, e.g., `.groovy` using the ScriptingFactory.
					if ( nextPath.endsWith( ".yaml" ) || nextPath.endsWith( ".yml" ) )
						map = ParcelLoader.decodeYamlToMap( content );
					else if ( nextPath.endsWith( ".json" ) )
						map = ParcelLoader.decodeJsonToMap( content );
					else if ( nextPath.endsWith( ".list" ) )
						map = ParcelLoader.decodeListToMap( content );
					else if ( nextPath.endsWith( ".properties" ) )
						map = ParcelLoader.decodePropToMap( content );
					else
						throw StorageException.ignorable( "Could not parse file " + IO.relPath( nextPath ) );

					ParcelLoader.decodeMap( map, stacker );
				}
			}
			catch ( Exception e )
			{
				throw StorageException.error( "Failed to load configuration node " + IO.relPath( nextPath ), e );
			}
		} );
	}

	public static <Stacker extends ContainerWithValue> void loadToStacker( @Nonnull Path path, @Nonnull Stacker stacker ) throws StorageException.Error
	{
		loadToStacker( path, stacker, "" );
	}

	private StorageConversions()
	{
		// Static Access
	}
}
