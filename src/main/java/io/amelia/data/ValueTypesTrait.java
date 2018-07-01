/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.OptionalBoolean;
import io.amelia.support.OptionalExt;
import io.amelia.support.OptionalLongExt;
import io.amelia.support.Strs;

/**
 * Provides common methods for converting an unknown value to (and from) {@link Object} using the Java 8 Optional feature.
 * <p>
 * These types include:
 * Boolean
 * Color
 * Double
 * Enum
 * Integer
 * Long
 * String
 * File
 * List
 * Class
 */
public interface ValueTypesTrait<ExceptionClass extends ApplicationException.Error>
{
	default Boolean getBoolean( TypeBase.TypeBoolean type )
	{
		return getBoolean( type.getPath() ).orElse( type.getDefault() );
	}

	default OptionalBoolean getBoolean( String key )
	{
		return OptionalBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default OptionalExt<Color, ExceptionClass> getColor()
	{
		return getColor( "" );
	}

	default OptionalExt<Color, ExceptionClass> getColor( String key )
	{
		return getValue( key ).filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	default Color getColor( TypeBase.TypeColor type )
	{
		return getColor( type.getPath() ).orElse( type.getDefault() );
	}

	default OptionalDouble getDouble()
	{
		return getDouble( "" );
	}

	default OptionalDouble getDouble( String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToDouble ), OptionalDouble::of, OptionalDouble::empty );
	}

	default Double getDouble( TypeBase.TypeDouble type )
	{
		return getDouble( type.getPath() ).orElse( type.getDefault() );
	}

	default <T extends Enum<T>> OptionalExt<T, ExceptionClass> getEnum( Class<T> enumClass )
	{
		return getEnum( "", enumClass );
	}

	default <T extends Enum<T>> OptionalExt<T, ExceptionClass> getEnum( String key, Class<T> enumClass )
	{
		return getString( key ).map( e -> Enum.valueOf( enumClass, e ) );
	}

	default <T extends Enum<T>> T getEnum( TypeBase.TypeEnum<T> type )
	{
		return getEnum( type.getPath(), type.getEnumClass() ).orElse( type.getDefault() );
	}

	default OptionalInt getInteger()
	{
		return getInteger( "" );
	}

	default OptionalInt getInteger( String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToInt ), OptionalInt::of, OptionalInt::empty );
	}

	default Integer getInteger( TypeBase.TypeInteger type )
	{
		return getInteger( type.getPath() ).orElse( type.getDefault() );
	}

	default <T> OptionalExt<List<T>, ExceptionClass> getList()
	{
		return getList( "" );
	}

	default <T> void getList( @Nonnull List<T> list )
	{
		getList( "", list );
	}

	default <T> void getList( @Nonnull String key, @Nonnull List<T> list )
	{
		getValue( key ).filter( v -> v instanceof List ).ifPresent( v -> list.addAll( ( List<T> ) v ) );
	}

	default <T> OptionalExt<List<T>, ExceptionClass> getList( @Nonnull String key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	default <T> OptionalExt getList( @Nonnull Class<T> expectedObjectClass )
	{
		return getList( "", expectedObjectClass );
	}

	default <T> OptionalExt<List<T>, ExceptionClass> getList( @Nonnull String key, @Nonnull Class<T> expectedObjectClass )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> Objs.castList( ( List<?> ) v, expectedObjectClass ) );
	}

	default OptionalLongExt getLong()
	{
		return getLong( "" );
	}

	default OptionalLongExt getLong( @Nonnull String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToLong ), OptionalLongExt::of, OptionalLongExt::empty );
	}

	default Long getLong( TypeBase.TypeLong type )
	{
		return getLong( type.getPath() ).orElse( type.getDefault() );
	}

	default OptionalExt<String, ExceptionClass> getString()
	{
		return getString( "" );
	}

	default OptionalExt<String, ExceptionClass> getString( @Nonnull String key )
	{
		return getValue( key ).map( Objs::castToString );
	}

	default String getString( TypeBase.TypeString type )
	{
		return getString( type.getPath() ).orElse( type.getDefault() );
	}

	default <T> OptionalExt<Class<T>, ExceptionClass> getStringAsClass()
	{
		return getStringAsClass( "" );
	}

	default <T> OptionalExt<Class<T>, ExceptionClass> getStringAsClass( @Nonnull String key )
	{
		return getStringAsClass( key, null );
	}

	@SuppressWarnings( "unchecked" )
	default <T> OptionalExt<Class<T>, ExceptionClass> getStringAsClass( @Nonnull String key, @Nonnull Class<T> expectedClass )
	{
		return getString( key ).mapCompat( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( expectedClass::isAssignableFrom );
	}

	default OptionalExt<File, ExceptionClass> getStringAsFile( File rel )
	{
		return getStringAsFile( "", rel );
	}

	default OptionalExt<File, ExceptionClass> getStringAsFile( String key, File rel )
	{
		return getString( key ).mapCompat( s -> IO.buildFile( rel, s ) );
	}

	default OptionalExt<File, ExceptionClass> getStringAsFile( String key )
	{
		return getString( key ).mapCompat( IO::buildFile );
	}

	default OptionalExt<File, ExceptionClass> getStringAsFile()
	{
		return getStringAsFile( "" );
	}

	default File getStringAsFile( TypeBase.TypeFile type )
	{
		return getStringAsFile( type.getPath() ).orElse( type.getDefault() );
	}

	default OptionalExt<Path, ExceptionClass> getStringAsPath( Path rel )
	{
		return getStringAsPath( "", rel );
	}

	default OptionalExt<Path, ExceptionClass> getStringAsPath( String key, Path rel )
	{
		return getString( key ).mapCompat( s -> IO.buildPath( rel, s ) );
	}

	default OptionalExt<Path, ExceptionClass> getStringAsPath( String key )
	{
		return getString( key ).mapCompat( IO::buildPath );
	}

	default OptionalExt<Path, ExceptionClass> getStringAsPath()
	{
		return getStringAsPath( "" );
	}

	default Path getStringAsPath( TypeBase.TypePath type )
	{
		return getStringAsPath( type.getPath() ).orElse( type.getDefault() );
	}

	default OptionalExt<List<String>, ExceptionClass> getStringList()
	{
		return getStringList( "", "|" );
	}

	default OptionalExt<List<String>, ExceptionClass> getStringList( String key )
	{
		return getStringList( key, "|" );
	}

	@SuppressWarnings( "unchecked" )
	default OptionalExt<List<String>, ExceptionClass> getStringList( String key, String delimiter )
	{
		Object value = getValue( key ).orElse( null );
		if ( value == null )
			return OptionalExt.empty();
		if ( value instanceof List )
			return OptionalExt.ofNeverNull( ( List<String> ) value );
		return OptionalExt.ofNeverNull( value ).map( Objs::castToString ).map( s -> Strs.split( s, delimiter ).collect( Collectors.toList() ) );
	}

	default List<String> getStringList( TypeBase.TypeStringList type )
	{
		return getStringList( type.getPath() ).orElse( type.getDefault() );
	}

	OptionalExt<?, ExceptionClass> getValue( String key );

	OptionalExt<?, ExceptionClass> getValue();

	default boolean isColor()
	{
		return isColor( "" );
	}

	default boolean isColor( String key )
	{
		return getValue( key ).map( v -> v instanceof Color ).orElse( false );
	}

	default boolean isEmpty()
	{
		return isEmpty( "" );
	}

	default boolean isEmpty( String key )
	{
		return getValue( key ).map( Objs::isEmpty ).orElse( true );
	}

	default boolean isList( String key )
	{
		return getValue( key ).map( o -> o instanceof List ).orElse( false );
	}

	default boolean isNull()
	{
		return isNull( "" );
	}

	default boolean isNull( String key )
	{
		return getValue( key ).map( Objs::isNull ).orElse( true );
	}

	default boolean isSet()
	{
		return !isNull();
	}

	default boolean isSet( String key )
	{
		return !isNull( key );
	}

	default boolean isTrue()
	{
		return isTrue( "" );
	}

	default boolean isTrue( TypeBase.TypeBoolean type )
	{
		return isTrue( type.getPath(), type.getDefault() );
	}

	default boolean isTrue( String key )
	{
		return isTrue( key, false );
	}

	default boolean isTrue( String key, boolean def )
	{
		return getValue( key ).map( Objs::isTrue ).orElse( def );
	}

	default boolean isType( @Nonnull String key, @Nonnull Class<?> type )
	{
		OptionalExt<?, ExceptionClass> result = getValue( key );
		return result.isPresent() && type.isAssignableFrom( result.get().getClass() );
	}
}
