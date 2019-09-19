/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ParcelableException;
import io.amelia.support.IO;
import io.amelia.support.Maths;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.Streams;
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
public interface KeyValueTypesTrait<ExceptionClass extends ApplicationException.Error>
{
	default VoluntaryBoolean getBoolean()
	{
		return VoluntaryBoolean.ofNullable( getValue( getDefaultKey() ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default Boolean getBoolean( @Nonnull TypeBase.TypeBoolean type )
	{
		return getBoolean( type.getStringPath() ).orElse( type.getDefault() );
	}

	default VoluntaryBoolean getBoolean( @Nonnull String key )
	{
		return getBoolean( Namespace.of( key ) );
	}

	default VoluntaryBoolean getBoolean( @Nonnull Namespace key )
	{
		return VoluntaryBoolean.ofNullable( getValue( key ).map( Objs::castToBoolean ).orElse( null ) );
	}

	default Voluntary<Color> getColor()
	{
		return getColor( getDefaultKey() );
	}

	default Color getColor( @Nonnull TypeBase.TypeColor type )
	{
		return getColor( type.getStringPath() ).orElse( type.getDefault() );
	}

	default Voluntary<Color> getColor( @Nonnull String key )
	{
		return getColor( Namespace.of( key ) );
	}

	default Voluntary<Color> getColor( @Nonnull Namespace key )
	{
		return getValue( key ).filter( v -> v instanceof Color ).map( v -> ( Color ) v );
	}

	@Nonnull
	default Namespace getDefaultKey()
	{
		// TODO Should we check for empty keys and replace them with this default? e.g., key.isEmpty ? key = getDefaultKey();
		return Namespace.empty();
	}

	default OptionalDouble getDouble()
	{
		return getDouble( getDefaultKey() );
	}

	default Double getDouble( @Nonnull TypeBase.TypeDouble type )
	{
		return getDouble( type.getStringPath() ).orElse( type.getDefault() );
	}

	default OptionalDouble getDouble( @Nonnull String key )
	{
		return getDouble( Namespace.of( key ) );
	}

	default OptionalDouble getDouble( @Nonnull Namespace key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToDouble ), OptionalDouble::of, OptionalDouble::empty );
	}

	default <T extends Enum<T>> Voluntary<T> getEnum( @Nonnull Class<T> enumClass )
	{
		return getEnum( getDefaultKey(), enumClass );
	}

	default <T extends Enum<T>> T getEnum( @Nonnull TypeBase.TypeEnum<T> type )
	{
		return getEnum( type.getStringPath(), type.getEnumClass() ).orElse( type.getDefault() );
	}

	default <T extends Enum<T>> Voluntary<T> getEnum( @Nonnull String key, @Nonnull Class<T> enumClass )
	{
		return getEnum( Namespace.of( key ), enumClass );
	}

	default <T extends Enum<T>> Voluntary<T> getEnum( @Nonnull Namespace key, @Nonnull Class<T> enumClass )
	{
		return getString( key ).map( e -> Enum.valueOf( enumClass, e ) );
	}

	default OptionalInt getInteger()
	{
		return getInteger( getDefaultKey() );
	}

	default Integer getInteger( @Nonnull TypeBase.TypeInteger type )
	{
		return getInteger( type.getStringPath() ).orElse( type.getDefault() );
	}

	default OptionalInt getInteger( @Nonnull String key )
	{
		return getInteger( Namespace.of( key ) );
	}

	default OptionalInt getInteger( @Nonnull Namespace key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToInt ), OptionalInt::of, OptionalInt::empty );
	}

	default <T> Voluntary<List<T>> getList()
	{
		return getList( getDefaultKey() );
	}

	default <T> void getList( @Nonnull List<T> list )
	{
		getList( getDefaultKey(), list );
	}

	default <T> Voluntary<List<T>> getList( @Nonnull Class<T> expectedObjectClass )
	{
		return getList( getDefaultKey(), expectedObjectClass );
	}

	default <T> void getList( @Nonnull String key, @Nonnull List<T> list )
	{
		getList( Namespace.of( key ), list );
	}

	default <T> Voluntary<List<T>> getList( @Nonnull String key )
	{
		return getList( Namespace.of( key ) );
	}

	default <T> Voluntary<List<T>> getList( @Nonnull String key, @Nonnull Class<T> expectedObjectClass )
	{
		return getList( Namespace.of( key ) );
	}

	default <T> void getList( @Nonnull Namespace key, @Nonnull List<T> list )
	{
		getValue( key ).filter( v -> v instanceof List ).ifPresent( v -> list.addAll( ( List<T> ) v ) );
	}

	default <T> Voluntary<List<T>> getList( @Nonnull Namespace key )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> ( List<T> ) v );
	}

	default <T> Voluntary<List<T>> getList( @Nonnull Namespace key, @Nonnull Class<T> expectedObjectClass )
	{
		return getValue( key ).filter( v -> v instanceof List ).map( v -> Objs.castList( ( List<?> ) v, expectedObjectClass ) );
	}

	default VoluntaryLong getLong()
	{
		return getLong( getDefaultKey() );
	}

	default Long getLong( @Nonnull TypeBase.TypeLong type )
	{
		return getLong( type.getStringPath() ).orElse( type.getDefault() );
	}

	default VoluntaryLong getLong( @Nonnull String key )
	{
		return getLong( Namespace.of( key ) );
	}

	default VoluntaryLong getLong( @Nonnull Namespace key )
	{
		return Objs.ifPresent( getValue( key ).map( Objs::castToLong ), VoluntaryLong::of, VoluntaryLong::empty );
	}

	default Voluntary<String> getString()
	{
		return getString( getDefaultKey() );
	}

	default String getString( @Nonnull TypeBase.TypeString type )
	{
		return getString( type.getStringPath() ).orElse( type.getDefault() );
	}

	default Voluntary<String> getString( @Nonnull String key )
	{
		return getString( Namespace.of( key ) );
	}

	default Voluntary<String> getString( @Nonnull Namespace key )
	{
		return getValue( key ).map( Objs::castToString );
	}

	default <T> Voluntary<Class<T>> getStringAsClass()
	{
		return getStringAsClass( getDefaultKey() );
	}

	default <T> Voluntary<Class<T>> getStringAsClass( @Nonnull String key )
	{
		return getStringAsClass( Namespace.of( key ) );
	}

	default <T> Voluntary<Class<T>> getStringAsClass( @Nonnull String key, @Nullable Class<T> expectedClass )
	{
		return getStringAsClass( Namespace.of( key ) );
	}

	default <T> Voluntary<Class<T>> getStringAsClass( @Nonnull Namespace key )
	{
		return getStringAsClass( key, null );
	}

	default <T> Voluntary<Class<T>> getStringAsClass( @Nonnull Namespace key, @Nullable Class<T> expectedClass )
	{
		return getString( key ).map( str -> ( Class<T> ) Objs.getClassByName( str ) ).filter( cls -> expectedClass != null && expectedClass.isAssignableFrom( cls ) );
	}

	default Voluntary<File> getStringAsFile( @Nonnull File rel )
	{
		return getStringAsFile( getDefaultKey(), rel );
	}

	default Voluntary<File> getStringAsFile()
	{
		return getStringAsFile( getDefaultKey() );
	}

	default File getStringAsFile( @Nonnull TypeBase.TypeFile type )
	{
		return getStringAsFile( type.getStringPath() ).orElse( type.getDefault() );
	}

	default Voluntary<File> getStringAsFile( @Nonnull String key, @Nonnull File rel )
	{
		return getStringAsFile( Namespace.of( key ) );
	}

	default Voluntary<File> getStringAsFile( @Nonnull String key )
	{
		return getStringAsFile( Namespace.of( key ) );
	}

	default Voluntary<File> getStringAsFile( @Nonnull Namespace key, @Nonnull File rel )
	{
		return getString( key ).map( s -> IO.buildFile( rel, s ) );
	}

	default Voluntary<File> getStringAsFile( @Nonnull Namespace key )
	{
		return getString( key ).map( IO::buildFile );
	}

	default Voluntary<Path> getStringAsPath( @Nonnull Path rel )
	{
		return getStringAsPath( getDefaultKey(), rel );
	}

	default Voluntary<Path> getStringAsPath()
	{
		return getStringAsPath( getDefaultKey() );
	}

	default Path getStringAsPath( @Nonnull TypeBase.TypePath type )
	{
		return getStringAsPath( type.getStringPath() ).orElse( type.getDefault() );
	}

	default Voluntary<Path> getStringAsPath( @Nonnull String key, @Nonnull Path rel )
	{
		return getStringAsPath( Namespace.of( key ) );
	}

	default Voluntary<Path> getStringAsPath( @Nonnull String key )
	{
		return getStringAsPath( Namespace.of( key ) );
	}

	default Voluntary<Path> getStringAsPath( @Nonnull Namespace key, @Nonnull Path rel )
	{
		return getString( key ).map( s -> IO.buildPath( rel, s ) );
	}

	default Voluntary<Path> getStringAsPath( @Nonnull Namespace key )
	{
		return getString( key ).map( IO::buildPath );
	}

	default Voluntary<List<String>> getStringList()
	{
		return getStringList( getDefaultKey(), "|" );
	}

	default List<String> getStringList( @Nonnull TypeBase.TypeStringList type )
	{
		return getStringList( type.getStringPath() ).orElse( type.getDefault() );
	}

	default Voluntary<List<String>> getStringList( @Nonnull String key )
	{
		return getStringList( Namespace.of( key ) );
	}

	default Voluntary<List<String>> getStringList( @Nonnull String key, @Nonnull String delimiter )
	{
		return getStringList( Namespace.of( key ) );
	}

	default Voluntary<List<String>> getStringList( @Nonnull Namespace key )
	{
		return getStringList( key, "|" );
	}

	default Voluntary<List<String>> getStringList( @Nonnull Namespace key, @Nonnull String delimiter )
	{
		return Voluntary.of( getStringStream( key, delimiter ).collect( Collectors.toList() ) );
	}

	default Stream<String> getStringStream()
	{
		return getStringStream( getDefaultKey(), "|" );
	}

	default Stream<String> getStringStream( @Nonnull TypeBase.TypeStringList type )
	{
		Supplier<Stream<String>> fork = Streams.fork( getStringStream( type.getStringPath() ), 2 );
		if ( fork.get().count() == 0 )
			return type.getDefault().stream();
		return fork.get();
	}

	default Stream<String> getStringStream( @Nonnull String key )
	{
		return getStringStream( Namespace.of( key ) );
	}

	default Stream<String> getStringStream( @Nonnull String key, @Nonnull String delimiter )
	{
		return getStringStream( Namespace.of( key ) );
	}

	default Stream<String> getStringStream( @Nonnull Namespace key )
	{
		return getStringStream( key, "|" );
	}

	default Stream<String> getStringStream( @Nonnull Namespace key, @Nonnull String delimiter )
	{
		Object value = getValue( key ).orElse( null );
		if ( value == null )
			return Stream.empty();
		if ( value instanceof List )
			return ( ( List<String> ) value ).stream();
		return Stream.of( value ).map( Objs::castToString ).flatMap( s -> Strs.split( s, delimiter ) );
	}

	default <V> V getValue( @Nonnull TypeBase.TypeWithDefault<V> type )
	{
		return getValue( type.getPath() ).map( obj -> ( V ) obj ).orElseGet( type.getDefaultSupplier() );
	}

	Voluntary<?> getValue( @Nonnull Namespace key );

	Voluntary<?> getValue();

	default boolean isColor()
	{
		return isColor( getDefaultKey() );
	}

	default boolean isColor( @Nonnull String key )
	{
		return isColor( Namespace.of( key ) );
	}

	default boolean isColor( @Nonnull Namespace key )
	{
		return getValue( key ).map( v -> v instanceof Color ).orElse( false );
	}

	default boolean isEmpty()
	{
		return isEmpty( getDefaultKey() );
	}

	default boolean isEmpty( @Nonnull String key )
	{
		return isEmpty( Namespace.of( key ) );
	}

	default boolean isEmpty( @Nonnull Namespace key )
	{
		return getValue( key ).map( Objs::isEmpty ).orElse( true );
	}

	default boolean isList()
	{
		return isList( getDefaultKey() );
	}

	default boolean isList( @Nonnull String key )
	{
		return isList( Namespace.of( key ) );
	}

	default boolean isList( @Nonnull Namespace key )
	{
		return getValue( key ).map( o -> o instanceof List ).orElse( false );
	}

	default boolean isLong()
	{
		return isLong( getDefaultKey() );
	}

	default boolean isLong( @Nonnull String key )
	{
		return isLong( Namespace.of( key ) );
	}

	default boolean isLong( @Nonnull Namespace key )
	{
		return getValue( key ).map( o -> o instanceof Long ).orElse( false );
	}

	default boolean isNull()
	{
		return isNull( getDefaultKey() );
	}

	default boolean isNull( @Nonnull String key )
	{
		return isNull( Namespace.of( key ) );
	}

	default boolean isNull( @Nonnull Namespace key )
	{
		return getValue( key ).map( Objs::isNull ).orElse( true );
	}

	default boolean isNumber()
	{
		return isNumber( getDefaultKey() );
	}

	default boolean isNumber( String key )
	{
		return isNumber( Namespace.of( key ) );
	}

	default boolean isNumber( Namespace key )
	{
		return getValue( key ).map( Maths::isNumber ).orElse( false );
	}

	default boolean isSet()
	{
		return !isNull();
	}

	default boolean isSet( @Nonnull String key )
	{
		return isSet( Namespace.of( key ) );
	}

	default boolean isSet( @Nonnull Namespace key )
	{
		return !isNull( key );
	}

	default boolean isTrue()
	{
		return isTrue( getDefaultKey() );
	}

	default boolean isTrue( @Nonnull TypeBase.TypeBoolean type )
	{
		return isTrue( type.getStringPath(), type.getDefault() );
	}

	default boolean isTrue( boolean def )
	{
		return getValue( getDefaultKey() ).map( Objs::isTrue ).orElse( def );
	}

	default boolean isTrue( @Nonnull String key )
	{
		return isTrue( Namespace.of( key ) );
	}

	default boolean isTrue( @Nonnull String key, boolean def )
	{
		return isTrue( Namespace.of( key ) );
	}

	default boolean isTrue( @Nonnull Namespace key )
	{
		return isTrue( key, false );
	}

	default boolean isTrue( @Nonnull Namespace key, boolean def )
	{
		return getValue( key ).map( Objs::isTrue ).orElse( def );
	}

	default boolean isType( @Nonnull String key, @Nonnull Class<?> type )
	{
		return isType( Namespace.of( key ), type );
	}

	default boolean isType( @Nonnull Namespace key, @Nonnull Class<?> type )
	{
		Voluntary<?> result = getValue( key );
		return result.isPresent() && type.isAssignableFrom( result.get().getClass() );
	}
}
