/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;
import io.amelia.support.Maths;
import io.amelia.support.Objs;
import io.amelia.support.VoluntaryBoolean;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryLong;
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
public interface KeyValueTypesTrait<ExceptionClass extends ApplicationException.Error>
{
	@Nonnull
	default String getDefaultKey()
	{
		// TODO Should we check for empty keys and replace them with this default? e.g., key.isEmpty ? key = getDefaultKey();
		return "";
	}

	default VoluntaryBoolean getBoolean()
	{
		return VoluntaryBoolean.ofNullable( getValue( getDefaultKey() ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default VoluntaryBoolean getBoolean( @Nonnull String key )
	{
		return VoluntaryBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default Voluntary<Color, ExceptionClass> getColor()
	{
		return getColor( getDefaultKey() );
	}

	default Voluntary<Color, ExceptionClass> getColor( @Nonnull String key )
	{
		return getValue( key ).filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	default OptionalDouble getDouble()
	{
		return getDouble( getDefaultKey() );
	}

	default OptionalDouble getDouble( @Nonnull String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToDouble ), OptionalDouble::of, OptionalDouble::empty );
	}

	default <T extends Enum<T>> Voluntary<T, ExceptionClass> getEnum( @Nonnull Class<T> enumClass )
	{
		return getEnum( getDefaultKey(), enumClass );
	}

	default <T extends Enum<T>> Voluntary<T, ExceptionClass> getEnum( @Nonnull String key, @Nonnull Class<T> enumClass )
	{
		return getString( key ).map( e -> Enum.valueOf( enumClass, e ) );
	}

	default OptionalInt getInteger()
	{
		return getInteger( getDefaultKey() );
	}

	default OptionalInt getInteger( @Nonnull String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToInt ), OptionalInt::of, OptionalInt::empty );
	}

	default <T> Voluntary<List<T>, ExceptionClass> getList()
	{
		return getList( getDefaultKey() );
	}

	default <T> void getList( @Nonnull List<T> list )
	{
		getList( getDefaultKey(), list );
	}

	default <T> void getList( @Nonnull String key, @Nonnull List<T> list )
	{
		getValue( key ).filter( v -> v instanceof List ).ifPresent( v -> list.addAll( ( List<T> ) v ) );
	}

	default <T> Voluntary<List<T>, ExceptionClass> getList( @Nonnull String key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	default <T> Voluntary<List<T>, ExceptionClass> getList( @Nonnull Class<T> expectedObjectClass )
	{
		return getList( getDefaultKey(), expectedObjectClass );
	}

	default <T> Voluntary<List<T>, ExceptionClass> getList( @Nonnull String key, @Nonnull Class<T> expectedObjectClass )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> Objs.castList( ( List<?> ) v, expectedObjectClass ) );
	}

	default VoluntaryLong getLong()
	{
		return getLong( getDefaultKey() );
	}

	default VoluntaryLong getLong( @Nonnull String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToLong ), VoluntaryLong::of, VoluntaryLong::empty );
	}

	default Voluntary<String, ExceptionClass> getString()
	{
		return getString( getDefaultKey() );
	}

	default Voluntary<String, ExceptionClass> getString( @Nonnull String key )
	{
		return getValue( key ).map( Objs::castToString );
	}

	default <T> Voluntary<Class<T>, ExceptionClass> getStringAsClass()
	{
		return getStringAsClass( getDefaultKey() );
	}

	default <T> Voluntary<Class<T>, ExceptionClass> getStringAsClass( @Nonnull String key )
	{
		return getStringAsClass( key, null );
	}

	@SuppressWarnings( "unchecked" )
	default <T> Voluntary<Class<T>, ExceptionClass> getStringAsClass( @Nonnull String key, @Nullable Class<T> expectedClass )
	{
		return getString( key ).map( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( cls -> expectedClass != null && expectedClass.isAssignableFrom( cls ) );
	}

	default Voluntary<File, ExceptionClass> getStringAsFile( @Nonnull File rel )
	{
		return getStringAsFile( getDefaultKey(), rel );
	}

	default Voluntary<File, ExceptionClass> getStringAsFile( @Nonnull String key, @Nonnull File rel )
	{
		return getString( key ).map( s -> IO.buildFile( rel, s ) );
	}

	default Voluntary<File, ExceptionClass> getStringAsFile( @Nonnull String key )
	{
		return getString( key ).map( IO::buildFile );
	}

	default Voluntary<File, ExceptionClass> getStringAsFile()
	{
		return getStringAsFile( getDefaultKey() );
	}

	default Voluntary<Path, ExceptionClass> getStringAsPath( @Nonnull Path rel )
	{
		return getStringAsPath( getDefaultKey(), rel );
	}

	default Voluntary<Path, ExceptionClass> getStringAsPath( @Nonnull String key, @Nonnull Path rel )
	{
		return getString( key ).map( s -> IO.buildPath( rel, s ) );
	}

	default Voluntary<Path, ExceptionClass> getStringAsPath( @Nonnull String key )
	{
		return getString( key ).map( IO::buildPath );
	}

	default Voluntary<Path, ExceptionClass> getStringAsPath()
	{
		return getStringAsPath( getDefaultKey() );
	}

	default Voluntary<List<String>, ExceptionClass> getStringList()
	{
		return getStringList( getDefaultKey(), "|" );
	}

	default Voluntary<List<String>, ExceptionClass> getStringList( @Nonnull String key )
	{
		return getStringList( key, "|" );
	}

	@SuppressWarnings( "unchecked" )
	default Voluntary<List<String>, ExceptionClass> getStringList( @Nonnull String key, @Nonnull String delimiter )
	{
		Object value = getValue( key ).orElse( null );
		if ( value == null )
			return Voluntary.empty();
		if ( value instanceof List )
			return Voluntary.of( ( List<String> ) value );
		return Voluntary.of( value ).map( Objs::castToString ).map( s -> Strs.split( s, delimiter ).collect( Collectors.toList() ) ).removeException();
	}

	default Double getValue( @Nonnull TypeBase.TypeDouble type )
	{
		return getDouble( type.getPath() ).orElse( type.getDefault() );
	}

	default <T extends Enum<T>> T getValue( @Nonnull TypeBase.TypeEnum<T> type )
	{
		return getEnum( type.getPath(), type.getEnumClass() ).orElse( type.getDefault() );
	}

	default Integer getValue( @Nonnull TypeBase.TypeInteger type )
	{
		return getInteger( type.getPath() ).orElse( type.getDefault() );
	}

	default Long getValue( @Nonnull TypeBase.TypeLong type )
	{
		return getLong( type.getPath() ).orElse( type.getDefault() );
	}

	default String getValue( @Nonnull TypeBase.TypeString type )
	{
		return getString( type.getPath() ).orElse( type.getDefault() );
	}

	default File getValue( @Nonnull TypeBase.TypeFile type )
	{
		return getStringAsFile( type.getPath() ).orElse( type.getDefault() );
	}

	default Path getValue( @Nonnull TypeBase.TypePath type )
	{
		return getStringAsPath( type.getPath() ).orElse( type.getDefault() );
	}

	default List<String> getValue( @Nonnull TypeBase.TypeStringList type )
	{
		return getStringList( type.getPath() ).orElse( type.getDefault() );
	}

	default Boolean getValue( @Nonnull TypeBase.TypeBoolean type )
	{
		return getBoolean( type.getPath() ).orElse( type.getDefault() );
	}

	default Color getValue( @Nonnull TypeBase.TypeColor type )
	{
		return getColor( type.getPath() ).orElse( type.getDefault() );
	}

	Voluntary<?, ExceptionClass> getValue( @Nonnull String key );

	Voluntary<?, ExceptionClass> getValue();

	default boolean isColor()
	{
		return isColor( getDefaultKey() );
	}

	default boolean isColor( @Nonnull String key )
	{
		return getValue( key ).map( v -> v instanceof Color ).orElse( false );
	}

	default boolean isEmpty()
	{
		return isEmpty( getDefaultKey() );
	}

	default boolean isEmpty( @Nonnull String key )
	{
		return getValue( key ).map( Objs::isEmpty ).orElse( true );
	}

	default boolean isList()
	{
		return isList( getDefaultKey() );
	}

	default boolean isList( @Nonnull String key )
	{
		return getValue( key ).map( o -> o instanceof List ).orElse( false );
	}

	default boolean isNumber()
	{
		return isNumber( getDefaultKey() );
	}

	default boolean isNumber( String key )
	{
		return getValue( key ).map( Maths::isNumber ).orElse( false );
	}

	default boolean isLong()
	{
		return isLong( getDefaultKey() );
	}

	default boolean isLong( @Nonnull String key )
	{
		return getValue( key ).map( o -> o instanceof Long ).orElse( false );
	}

	default boolean isNull()
	{
		return isNull( getDefaultKey() );
	}

	default boolean isNull( @Nonnull String key )
	{
		return getValue( key ).map( Objs::isNull ).orElse( true );
	}

	default boolean isSet()
	{
		return !isNull();
	}

	default boolean isSet( @Nonnull String key )
	{
		return !isNull( key );
	}

	default boolean isTrue()
	{
		return isTrue( getDefaultKey() );
	}

	default boolean isTrue( @Nonnull TypeBase.TypeBoolean type )
	{
		return isTrue( type.getPath(), type.getDefault() );
	}

	default boolean isTrue( boolean def )
	{
		return getValue( getDefaultKey() ).map( Objs::isTrue ).orElse( def );
	}

	default boolean isTrue( @Nonnull String key )
	{
		return isTrue( key, false );
	}

	default boolean isTrue( @Nonnull String key, boolean def )
	{
		return getValue( key ).map( Objs::isTrue ).orElse( def );
	}

	default boolean isType( @Nonnull String key, @Nonnull Class<?> type )
	{
		Voluntary<?, ExceptionClass> result = getValue( key );
		return result.isPresent() && type.isAssignableFrom( result.get().getClass() );
	}
}
