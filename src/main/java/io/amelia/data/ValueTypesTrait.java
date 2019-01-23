/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
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
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;
import io.amelia.support.VoluntaryLong;

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
	default VoluntaryBoolean getBoolean()
	{
		return VoluntaryBoolean.ofNullable( getValue().map( Objs::castToBoolean ).orElse( null ) );
	}

	default Voluntary<Color> getColor()
	{
		return getValue().filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	default OptionalDouble getDouble()
	{
		return Objs.ifPresent( getValue().map( Objs::castToDouble ), OptionalDouble::of, OptionalDouble::empty );
	}

	default <T extends Enum<T>> Voluntary<T> getEnum( Class<T> enumClass )
	{
		return getString().map( e -> Enum.valueOf( enumClass, e ) );
	}

	default OptionalInt getInteger()
	{
		return Objs.ifPresent( getValue().map( Objs::castToInt ), OptionalInt::of, OptionalInt::empty );
	}

	default <T> Voluntary<List<T>> getList()
	{
		return getValue().filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	default <T> void getList( @Nonnull List<T> list )
	{
		getValue().filter( v -> v instanceof List ).ifPresent( v -> list.addAll( ( List<T> ) v ) );
	}

	default <T> Voluntary<List<T>> getList( @Nonnull Class<T> expectedObjectClass )
	{
		return getValue().filter( v -> v instanceof List ).map( v -> Objs.castList( ( List<?> ) v, expectedObjectClass ) );
	}

	default VoluntaryLong getLong()
	{
		return Objs.ifPresent( getValue().map( Objs::castToLong ), VoluntaryLong::of, VoluntaryLong::empty );
	}

	default Voluntary<String> getString()
	{
		return getString();
	}

	default <T> Voluntary<Class<T>> getStringAsClass()
	{
		return getStringAsClass( Object.class ).map( value -> ( Class<T> ) value );
	}

	default <T> Voluntary<Class<T>> getStringAsClass( @Nonnull Class<T> expectedClass )
	{
		return getString().map( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( expectedClass::isAssignableFrom );
	}

	default Voluntary<File> getStringAsFile( File rel )
	{
		return getString().map( s -> IO.buildFile( rel, s ) );
	}

	default Voluntary<File> getStringAsFile()
	{
		return getString().map( IO::buildFile );
	}

	default Voluntary<Path> getStringAsPath( Path rel )
	{
		return getString().map( s -> IO.buildPath( rel, s ) );
	}

	default Voluntary<Path> getStringAsPath()
	{
		return getString().map( IO::buildPath );
	}

	default Voluntary<List<String>> getStringList()
	{
		return getStringList( "|" );
	}

	@SuppressWarnings( "unchecked" )
	default Voluntary<List<String>> getStringList( String delimiter )
	{
		return Voluntary.of( getStringStream( delimiter ).collect( Collectors.toList() ) );
	}

	default Stream<String> getStringStream()
	{
		return getStringStream( "|" );
	}

	default Stream<String> getStringStream( @Nonnull String delimiter )
	{
		Object value = getValue().orElse( null );
		if ( value == null )
			return Stream.empty();
		if ( value instanceof List )
			return ( ( List<String> ) value ).stream();
		return Stream.of( value ).map( Objs::castToString ).flatMap( s -> Strs.split( s, delimiter ) );
	}

	Voluntary<?> getValue();

	default boolean isColor()
	{
		return getValue().map( v -> v instanceof Color ).orElse( false );
	}

	default boolean isEmpty()
	{
		return getValue().map( Objs::isEmpty ).orElse( true );
	}

	default boolean isList()
	{
		return getValue().map( o -> o instanceof List ).orElse( false );
	}

	default boolean isNull()
	{
		return getValue().map( Objs::isNull ).orElse( true );
	}

	default boolean isSet()
	{
		return !isNull();
	}

	default boolean isTrue()
	{
		return isTrue( false );
	}

	default boolean isTrue( boolean def )
	{
		return getValue().map( Objs::isTrue ).orElse( def );
	}

	default boolean isType( @Nonnull Class<?> type )
	{
		Voluntary<?> result = getValue();
		return result.isPresent() && type.isAssignableFrom( result.get().getClass() );
	}
}
