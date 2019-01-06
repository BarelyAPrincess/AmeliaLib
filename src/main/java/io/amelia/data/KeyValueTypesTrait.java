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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.ApplicationException;
import io.amelia.support.IO;
import io.amelia.support.Maths;
import io.amelia.support.Objs;
import io.amelia.support.Streams;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;
import io.amelia.support.VoluntaryLong;
import io.amelia.support.VoluntaryWithCause;

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
	default VoluntaryBoolean getBoolean()
	{
		return VoluntaryBoolean.ofNullable( getValue( getDefaultKey() ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default VoluntaryBoolean getBoolean( @Nonnull String key )
	{
		return VoluntaryBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default Boolean getBoolean( @Nonnull TypeBase.TypeBoolean type )
	{
		return getBoolean( type.getPath() ).orElse( type.getDefault() );
	}

	default VoluntaryWithCause<Color, ExceptionClass> getColor()
	{
		return getColor( getDefaultKey() );
	}

	default VoluntaryWithCause<Color, ExceptionClass> getColor( @Nonnull String key )
	{
		return getValue( key ).filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	default Color getColor( @Nonnull TypeBase.TypeColor type )
	{
		return getColor( type.getPath() ).orElse( type.getDefault() );
	}

	@Nonnull
	default String getDefaultKey()
	{
		// TODO Should we check for empty keys and replace them with this default? e.g., key.isEmpty ? key = getDefaultKey();
		return "";
	}

	default OptionalDouble getDouble()
	{
		return getDouble( getDefaultKey() );
	}

	default OptionalDouble getDouble( @Nonnull String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToDouble ), OptionalDouble::of, OptionalDouble::empty );
	}

	default Double getDouble( @Nonnull TypeBase.TypeDouble type )
	{
		return getDouble( type.getPath() ).orElse( type.getDefault() );
	}

	default <T extends Enum<T>> VoluntaryWithCause<T, ExceptionClass> getEnum( @Nonnull Class<T> enumClass )
	{
		return getEnum( getDefaultKey(), enumClass );
	}

	default <T extends Enum<T>> VoluntaryWithCause<T, ExceptionClass> getEnum( @Nonnull String key, @Nonnull Class<T> enumClass )
	{
		return getString( key ).map( e -> Enum.valueOf( enumClass, e ) );
	}

	default <T extends Enum<T>> T getEnum( @Nonnull TypeBase.TypeEnum<T> type )
	{
		return getEnum( type.getPath(), type.getEnumClass() ).orElse( type.getDefault() );
	}

	default OptionalInt getInteger()
	{
		return getInteger( getDefaultKey() );
	}

	default OptionalInt getInteger( @Nonnull String key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToInt ), OptionalInt::of, OptionalInt::empty );
	}

	default Integer getInteger( @Nonnull TypeBase.TypeInteger type )
	{
		return getInteger( type.getPath() ).orElse( type.getDefault() );
	}

	default <T> VoluntaryWithCause<List<T>, ExceptionClass> getList()
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

	default <T> VoluntaryWithCause<List<T>, ExceptionClass> getList( @Nonnull String key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	default <T> VoluntaryWithCause<List<T>, ExceptionClass> getList( @Nonnull Class<T> expectedObjectClass )
	{
		return getList( getDefaultKey(), expectedObjectClass );
	}

	default <T> VoluntaryWithCause<List<T>, ExceptionClass> getList( @Nonnull String key, @Nonnull Class<T> expectedObjectClass )
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

	default Long getLong( @Nonnull TypeBase.TypeLong type )
	{
		return getLong( type.getPath() ).orElse( type.getDefault() );
	}

	default VoluntaryWithCause<String, ExceptionClass> getString()
	{
		return getString( getDefaultKey() );
	}

	default VoluntaryWithCause<String, ExceptionClass> getString( @Nonnull String key )
	{
		return getValue( key ).map( Objs::castToString );
	}

	default String getString( @Nonnull TypeBase.TypeString type )
	{
		return getString( type.getPath() ).orElse( type.getDefault() );
	}

	default <T> VoluntaryWithCause<Class<T>, ExceptionClass> getStringAsClass()
	{
		return getStringAsClass( getDefaultKey() );
	}

	default <T> VoluntaryWithCause<Class<T>, ExceptionClass> getStringAsClass( @Nonnull String key )
	{
		return getStringAsClass( key, null );
	}

	@SuppressWarnings( "unchecked" )
	default <T> VoluntaryWithCause<Class<T>, ExceptionClass> getStringAsClass( @Nonnull String key, @Nullable Class<T> expectedClass )
	{
		return getString( key ).map( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( cls -> expectedClass != null && expectedClass.isAssignableFrom( cls ) );
	}

	default VoluntaryWithCause<File, ExceptionClass> getStringAsFile( @Nonnull File rel )
	{
		return getStringAsFile( getDefaultKey(), rel );
	}

	default VoluntaryWithCause<File, ExceptionClass> getStringAsFile( @Nonnull String key, @Nonnull File rel )
	{
		return getString( key ).map( s -> IO.buildFile( rel, s ) );
	}

	default VoluntaryWithCause<File, ExceptionClass> getStringAsFile( @Nonnull String key )
	{
		return getString( key ).map( IO::buildFile );
	}

	default VoluntaryWithCause<File, ExceptionClass> getStringAsFile()
	{
		return getStringAsFile( getDefaultKey() );
	}

	default File getStringAsFile( @Nonnull TypeBase.TypeFile type )
	{
		return getStringAsFile( type.getPath() ).orElse( type.getDefault() );
	}

	default VoluntaryWithCause<Path, ExceptionClass> getStringAsPath( @Nonnull Path rel )
	{
		return getStringAsPath( getDefaultKey(), rel );
	}

	default VoluntaryWithCause<Path, ExceptionClass> getStringAsPath( @Nonnull String key, @Nonnull Path rel )
	{
		return getString( key ).map( s -> IO.buildPath( rel, s ) );
	}

	default VoluntaryWithCause<Path, ExceptionClass> getStringAsPath( @Nonnull String key )
	{
		return getString( key ).map( IO::buildPath );
	}

	default VoluntaryWithCause<Path, ExceptionClass> getStringAsPath()
	{
		return getStringAsPath( getDefaultKey() );
	}

	default Path getStringAsPath( @Nonnull TypeBase.TypePath type )
	{
		return getStringAsPath( type.getPath() ).orElse( type.getDefault() );
	}

	default Voluntary<List<String>> getStringList()
	{
		return getStringList( getDefaultKey(), "|" );
	}

	default Voluntary<List<String>> getStringList( @Nonnull String key )
	{
		return getStringList( key, "|" );
	}

	@SuppressWarnings( "unchecked" )
	default Voluntary<List<String>> getStringList( @Nonnull String key, @Nonnull String delimiter )
	{
		return Voluntary.of( getStringStream( key, delimiter ).collect( Collectors.toList() ) );
	}

	default List<String> getStringList( @Nonnull TypeBase.TypeStringList type )
	{
		return getStringList( type.getPath() ).orElse( type.getDefault() );
	}

	default Stream<String> getStringStream()
	{
		return getStringStream( getDefaultKey(), "|" );
	}

	default Stream<String> getStringStream( @Nonnull String key )
	{
		return getStringStream( key, "|" );
	}

	@SuppressWarnings( "unchecked" )
	default Stream<String> getStringStream( @Nonnull String key, @Nonnull String delimiter )
	{
		Object value = getValue( key ).orElse( null );
		if ( value == null )
			return Stream.empty();
		if ( value instanceof List )
			return ( ( List<String> ) value ).stream();
		return Stream.of( value ).map( Objs::castToString ).flatMap( s -> Strs.split( s, delimiter ) );
	}

	default Stream<String> getStringStream( @Nonnull TypeBase.TypeStringList type )
	{
		Supplier<Stream<String>> fork = Streams.fork( getStringStream( type.getPath() ), 2 );
		if ( fork.get().count() == 0 )
			return type.getDefault().stream();
		return fork.get();
	}

	default <V> V getValue( @Nonnull TypeBase.TypeWithDefault<V> type )
	{
		return getValue( type.getPath() ).map( obj -> ( V ) obj ).orElseGet( type.getDefaultSupplier() );
	}

	VoluntaryWithCause<?, ExceptionClass> getValue( @Nonnull String key );

	VoluntaryWithCause<?, ExceptionClass> getValue();

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

	default boolean isNumber()
	{
		return isNumber( getDefaultKey() );
	}

	default boolean isNumber( String key )
	{
		return getValue( key ).map( Maths::isNumber ).orElse( false );
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
		VoluntaryWithCause<?, ExceptionClass> result = getValue( key );
		return result.isPresent() && type.isAssignableFrom( result.get().getClass() );
	}
}
