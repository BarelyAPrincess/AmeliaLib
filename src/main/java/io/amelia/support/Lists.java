/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Lists
{
	public static <V> V add( @Nonnull List<V> list, @Nonnull V obj )
	{
		Objs.notNull( list );
		Objs.notNull( obj );

		list.add( obj );
		return obj;
	}

	public static List<String> collect( @Nonnull Iterator<String> iterator )
	{
		List<String> result = new ArrayList<>();
		while ( iterator.hasNext() )
			result.add( iterator.next() );
		return result;
	}

	public static <E> E compute( Collection<E> collection, Predicate<E> predicate )
	{
		for ( E element : collection )
			if ( predicate.test( element ) )
				return element;
		return null;
	}

	@SuppressWarnings( "unchecked" )
	public static <T> List<T> copy( List<?> list )
	{
		List<T> newList = copyEmpty( list );
		for ( Object o : list )
			newList.add( ( T ) o );
		return newList;
	}

	public static <V> List<V> copyEmpty( List<?> list )
	{
		if ( list instanceof CopyOnWriteArrayList )
			return new CopyOnWriteArrayList<>();
		if ( list instanceof LinkedList )
			return new LinkedList<>();
		if ( list instanceof Vector )
			return new Vector<>();
		return new ArrayList<>();
	}

	@Nullable
	public static <V> V find( @Nonnull List<V> list, @Nonnull Predicate<V> predicate )
	{
		for ( V value : list )
			if ( predicate.test( value ) )
				return value;

		return null;
	}

	/**
	 * Cycles through list of elements until lambda function returns non-null.
	 * If the list doesn't contain the returned value, the old value is replaced.
	 *
	 * @param list     The original list
	 * @param function The matching function
	 * @param <V>      Contained list type
	 *
	 * @return The matching value
	 */
	public static <V> V findAndReplace( List<V> list, Function<? super V, ? extends V> function )
	{
		Objs.notNull( function );

		for ( V oldValue : list )
		{
			V newValue = function.apply( oldValue );
			if ( newValue != null )
			{
				if ( !list.contains( newValue ) )
				{
					list.remove( oldValue );
					list.add( newValue );
				}
				return newValue;
			}
		}

		return null;
	}

	public static <V> V findOrNew( @Nonnull List<V> list, @Nonnull Predicate<V> predicate, @Nonnull Supplier<V> valueSupplier )
	{
		V find = find( list, predicate );
		if ( find == null )
		{
			find = valueSupplier.get();
			list.add( find );
		}
		return find;
	}

	@SuppressWarnings( "unchecked" )
	public static <Type> Optional<Type> first( @Nonnull Collection<Type> list )
	{
		return Optional.ofNullable( list.size() == 0 ? null : ( Type ) list.toArray()[0] );
	}

	public static boolean incremented( Set<String> values )
	{
		Set<Integer> numbers = new TreeSet<>();

		int lowest = -1;

		for ( String s : values )
			try
			{
				int n = Integer.parseInt( s );
				if ( lowest < 0 || n < lowest )
					lowest = n;
				numbers.add( n );
			}
			catch ( NumberFormatException e )
			{
				return false; // String is not a number, auto disqualified
			}

		for ( int i = lowest; i < numbers.size(); i++ )
		{
			if ( !numbers.contains( i ) )
			{
				return false;
			}
		}

		return true;
	}

	public static boolean isOfType( List values, Class<?> aClass )
	{
		for ( Object obj : values )
			if ( !aClass.isAssignableFrom( obj.getClass() ) )
				return false;
		return true;
	}

	public static String joinQuery( Map<String, String> map )
	{
		return map.entrySet().stream().map( e -> e.getKey() + "=" + e.getValue() ).collect( Collectors.joining( "&" ) );
	}

	@SuppressWarnings( "unchecked" )
	public static <Type> Optional<Type> last( Collection<Type> list )
	{
		return Optional.ofNullable( list.size() == 0 ? null : ( Type ) list.toArray()[list.size() - 1] );
	}

	public static <T> List<T> newArrayList( Iterable<T> elements )
	{
		return StreamSupport.stream( elements.spliterator(), true ).collect( Collectors.toList() );
	}

	@SafeVarargs
	public static <T> List<T> newArrayList( T... elements )
	{
		return new ArrayList<>( Arrays.asList( elements ) );
	}

	@SafeVarargs
	public static <T> Set<T> newHashSet( T... elements )
	{
		return new HashSet<>( Arrays.asList( elements ) );
	}

	public static <T> boolean removeIf( Set<T> list, Predicate<T> filter )
	{
		Objs.notNull( list );
		Objs.notNull( filter );

		boolean removed = false;
		final Iterator<T> each = list.iterator();
		while ( each.hasNext() )
		{
			T next = each.next();
			if ( filter.test( next ) )
			{
				each.remove();
				removed = true;
			}
		}
		return removed;
	}

	public static <T> boolean removeIf( List<T> list, Predicate<T> filter )
	{
		Objs.notNull( list );
		Objs.notNull( filter );

		boolean removed = false;
		final Iterator<T> each = list.iterator();
		while ( each.hasNext() )
		{
			T next = each.next();
			if ( filter.test( next ) )
			{
				each.remove();
				removed = true;
			}
		}
		return removed;
	}

	public static <T> List<T> subList( @Nonnull List<T> list, int start, int length )
	{
		Objs.notNull( list );
		Objs.notNegative( start );
		Objs.notNegative( length );

		return list.stream().skip( start ).limit( length ).collect( Collectors.toList() );
	}

	public static <T, R> List<R> walk( List<T> list, Function<T, R> function )
	{
		if ( list == null )
			return null;
		return list.stream().map( function ).filter( Objects::nonNull ).collect( Collectors.toList() );
	}

	private Lists()
	{
		// Static Helper
	}
}
