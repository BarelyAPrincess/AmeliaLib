/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.parcel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.data.yaml.YamlConstructor;
import io.amelia.data.yaml.YamlRepresenter;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ParcelableException;
import io.amelia.support.Encrypt;
import io.amelia.support.IO;
import io.amelia.support.Maps;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;

public class ParcelLoader
{
	// TODO Implement the ability to decode directories containing files to parcels. Maybe? Technically this is implemented by the StorageConversions class.

	private static final Gson gson = new GsonBuilder().serializeNulls().setLenient().create();
	private static final DumperOptions yamlOptions = new DumperOptions();
	private static final Representer yamlRepresenter = new YamlRepresenter();
	private static final Yaml yaml = new Yaml( new YamlConstructor(), yamlRepresenter, yamlOptions );

	private static Type autoDetect( @Nonnull String name )
	{
		name = name.toLowerCase();

		if ( name.endsWith( ".json" ) )
			return Type.JSON;

		if ( name.endsWith( ".list" ) )
			return Type.LIST;

		if ( name.endsWith( ".properties" ) )
			return Type.PROP;

		if ( name.endsWith( ".yaml" ) || name.endsWith( ".yml" ) )
			return Type.YAML;

		// TODO Add support for scripting factory scripts, e.g., Groovy

		throw new ParcelableException.Ignorable( null, "AUTO_DETECT couldn't determine the file type based on the file extension." );
	}

	public static Parcel decode( @Nonnull Path path, @Nonnull Type type ) throws IOException, ParcelableException.Error
	{
		if ( type == Type.AUTO_DETECT )
			type = autoDetect( path.getFileName().toString() );

		return decode( IO.readFileToString( path ), type );
	}

	public static Parcel decode( @Nonnull File file, Type type ) throws IOException, ParcelableException.Error
	{
		if ( type == Type.AUTO_DETECT )
			type = autoDetect( file.getName() );

		return decode( IO.readFileToString( file ), type );
	}

	public static Parcel decode( @Nonnull InputStream inputStream, Type type ) throws IOException, ParcelableException.Error
	{
		return decode( IO.readStreamToString( inputStream ), type );
	}

	public static Parcel decode( @Nonnull String encoded, Type type ) throws ParcelableException.Error
	{
		if ( type == Type.AUTO_DETECT )
			throw new ParcelableException.Ignorable( null, "AUTO_DETECT can only be used on files for now. Future use will be to inspect streams and strings content for the type." );
		if ( type == Type.JSON )
			return decodeJson( encoded );
		if ( type == Type.LIST )
			return decodeList( encoded );
		if ( type == Type.PROP )
			return decodeProp( encoded );
		if ( type == Type.YAML )
			return decodeYaml( encoded );

		throw new ParcelableException.Ignorable( null, "Could not decode." );
	}

	public static Parcel decodeJson( String jsonEncoded ) throws ParcelableException.Error
	{
		return decodeMap( decodeJsonToMap( jsonEncoded ) );
	}

	public static Parcel decodeJson( Path path ) throws IOException, ParcelableException.Error
	{
		return decodeJson( IO.readFileToString( path ) );
	}

	public static Parcel decodeJson( File file ) throws IOException, ParcelableException.Error
	{
		return decodeJson( IO.readFileToString( file ) );
	}

	public static Parcel decodeJson( InputStream inputStream ) throws IOException, ParcelableException.Error
	{
		return decodeJson( IO.readStreamToString( inputStream ) );
	}

	public static Map<String, Object> decodeJsonToMap( String jsonEncoded )
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( jsonEncoded, Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeJsonToMap( Path path ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( IO.readFileToString( path ), Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeJsonToMap( File file ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( IO.readFileToString( file ), Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeJsonToMap( InputStream inputStream ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) gson.fromJson( IO.readStreamToString( inputStream ), Map.class ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Parcel decodeList( String listEncoded ) throws ParcelableException.Error
	{
		return decodeMap( decodeListToMap( listEncoded ) );
	}

	public static Parcel decodeList( Path path ) throws IOException, ParcelableException.Error
	{
		return decodeMap( decodeListToMap( path ) );
	}

	public static Parcel decodeList( File file ) throws FileNotFoundException, ParcelableException.Error
	{
		return decodeMap( decodeListToMap( file ) );
	}

	public static Parcel decodeList( InputStream inputStream ) throws ParcelableException.Error
	{
		return decodeMap( decodeListToMap( inputStream ) );
	}

	public static Map<String, Object> decodeListToMap( InputStream inputStream )
	{
		return decodeListToMap( IO.readStreamToLines( inputStream, "#" ) );
	}

	public static Map<String, Object> decodeListToMap( String encodedList, String delimiter )
	{
		return decodeListToMap( Strs.split( encodedList, delimiter ).collect( Collectors.toList() ) );
	}

	public static Map<String, Object> decodeListToMap( String encodedList )
	{
		return decodeListToMap( Strs.split( encodedList, "\n" ).collect( Collectors.toList() ) );
	}

	public static Map<String, Object> decodeListToMap( List<String> encodedList )
	{
		return Maps.builder().increment( encodedList ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeListToMap( Path path ) throws IOException
	{
		return decodeListToMap( IO.readFileToLines( path, "#" ) );
	}

	public static Map<String, Object> decodeListToMap( File file ) throws FileNotFoundException
	{
		return decodeListToMap( IO.readFileToLines( file, "#" ) );
	}

	public static Parcel decodeMap( Map<String, Object> mapEncoded ) throws ParcelableException.Error
	{
		Parcel dataMap = Parcel.empty();
		decodeMap( mapEncoded, dataMap );
		return dataMap;
	}

	@SuppressWarnings( "unchecked" )
	public static <ValueType, ExceptionClass extends ApplicationException.Error> void decodeMap( Map<String, ValueType> mapEncoded, ContainerWithValue<? extends ContainerWithValue, ValueType, ExceptionClass> root ) throws ExceptionClass
	{
		for ( Map.Entry<String, ValueType> entry : mapEncoded.entrySet() )
		{
			if ( entry.getKey().equals( "__value" ) )
				root.setValue( entry.getValue() );
			else
			{
				ContainerWithValue<? extends ContainerWithValue, ValueType, ExceptionClass> child = root.getChildOrCreate( entry.getKey() );

				if ( entry.getValue() instanceof Map )
					decodeMap( ( Map<String, ValueType> ) entry.getValue(), child );
				else
					child.setValue( entry.getValue() );
			}
		}
	}

	public static Parcel decodeProp( String propEncoded ) throws ParcelableException.Error
	{
		return decodeMap( decodePropToMap( propEncoded ) );
	}

	public static Parcel decodeProp( Path path ) throws IOException, ParcelableException.Error
	{
		return decodeMap( decodePropToMap( path ) );
	}

	public static Parcel decodeProp( File file ) throws IOException, ParcelableException.Error
	{
		return decodeMap( decodePropToMap( file ) );
	}

	public static Parcel decodeProp( InputStream inputStream ) throws IOException, ParcelableException.Error
	{
		return decodeMap( decodePropToMap( inputStream ) );
	}

	public static Map<String, Object> decodePropToMap( String propEncoded )
	{
		Properties prop = new Properties();
		try
		{
			prop.load( new StringReader( propEncoded ) );
		}
		catch ( IOException e )
		{
			// Ignore - very unlikely to throw.
		}
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodePropToMap( InputStream inputStream ) throws IOException
	{
		Properties prop = new Properties();
		prop.load( inputStream );
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodePropToMap( Path path ) throws IOException
	{
		Properties prop = new Properties();
		prop.load( Files.newInputStream( path ) );
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodePropToMap( File file ) throws IOException
	{
		Properties prop = new Properties();
		prop.load( new FileReader( file ) );
		return Maps.builder( prop ).castTo( String.class, Object.class ).hashMap();
	}

	/* public static Parcel decodeXml( String xml )
	{
		TODO Implement
	} */

	public static Map<String, Object> decodeToMap( @Nonnull Path path, Type type ) throws IOException
	{
		if ( type == Type.AUTO_DETECT )
			type = autoDetect( path.getFileName().toString() );

		return decodeToMap( IO.readFileToString( path ), type );
	}

	public static Map<String, Object> decodeToMap( @Nonnull File file, Type type ) throws IOException
	{
		if ( type == Type.AUTO_DETECT )
			type = autoDetect( file.getName() );

		return decodeToMap( IO.readFileToString( file ), type );
	}

	public static Map<String, Object> decodeToMap( @Nonnull InputStream inputStream, Type type ) throws IOException
	{
		return decodeToMap( IO.readStreamToString( inputStream ), type );
	}

	public static Map<String, Object> decodeToMap( @Nonnull String encoded, Type type )
	{
		if ( type == Type.AUTO_DETECT )
			throw new ParcelableException.Ignorable( null, "AUTO_DETECT can only be used on files for now. Future use will be to inspect stream and string contents for type." );
		if ( type == Type.JSON )
			return decodeJsonToMap( encoded );
		if ( type == Type.LIST )
			return decodeListToMap( encoded );
		if ( type == Type.PROP )
			return decodePropToMap( encoded );
		if ( type == Type.YAML )
			return decodeYamlToMap( encoded );

		throw new ParcelableException.Ignorable( null, "Could not decode." );
	}

	public static Parcel decodeYaml( Path path ) throws IOException, ParcelableException.Error
	{
		return decodeYaml( IO.readStreamToString( Files.newInputStream( path ) ) );
	}

	public static Parcel decodeYaml( File file ) throws IOException, ParcelableException.Error
	{
		return decodeYaml( IO.readFileToString( file ) );
	}

	public static Parcel decodeYaml( InputStream inputStream ) throws IOException, ParcelableException.Error
	{
		return decodeYaml( IO.readStreamToString( inputStream ) );
	}

	public static Parcel decodeYaml( String yamlEncoded ) throws ParcelableException.Error
	{
		return decodeMap( decodeYamlToMap( yamlEncoded ) );
	}

	public static Map<String, Object> decodeYamlToMap( String yamlEncoded )
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( yamlEncoded ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeYamlToMap( Path path ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( IO.readFileToString( path ) ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeYamlToMap( File file ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( IO.readFileToString( file ) ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static Map<String, Object> decodeYamlToMap( InputStream inputStream ) throws IOException
	{
		return Maps.builder().putAll( ( Map<?, ?> ) yaml.load( IO.readStreamToString( inputStream ) ) ).castTo( String.class, Object.class ).hashMap();
	}

	public static String encodeJson( Parcel encoded )
	{
		return gson.toJson( encodeMap( encoded ) );
	}

	/* public static void encodeXml( Parcel encoded )
	{
		TODO Implement
	} */

	public static <ValueType, ExceptionClass extends ApplicationException.Error> Map<String, Object> encodeMap( ContainerWithValue<? extends ContainerWithValue, ValueType, ExceptionClass> encoded )
	{
		Map<String, Object> map = new HashMap<>();

		encoded.getChildren().forEach( child -> {
			Voluntary<ValueType, ExceptionClass> value = child.getValue();

			if ( child.hasChildren() )
			{
				map.put( child.getName(), encodeMap( child ) );
				value.ifPresent( o -> map.put( "__value", o ) );
			}
			else
				value.ifPresent( o -> map.put( child.getName(), o ) );
		} );

		return map;
	}

	public static String encodeYaml( Parcel encoded )
	{
		return yaml.dump( encodeMap( encoded ) );
	}

	public static String hashObject( Object obj )
	{
		// yaml.dump( obj ) OR gson.toJson( obj )?
		return obj == null ? null : Encrypt.md5Hex( obj instanceof String ? ( String ) obj : gson.toJson( obj ) );
	}

	private ParcelLoader()
	{
		// Static Class
	}

	public enum Type
	{
		JSON,
		LIST,
		PROP,
		YAML,
		/**
		 * Currently only usable when decoding a file. Future use for decoding using stream content.
		 */
		AUTO_DETECT
	}
}
