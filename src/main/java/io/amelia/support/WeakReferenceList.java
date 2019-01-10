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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Wraps an {@link ArrayList<WeakReference>>} and provides methods that auto remove dereferenced values.
 *
 * @param <Type> The Value Type
 */
public class WeakReferenceList<Type> implements List<Type>
{
	private static boolean notValid( WeakReference<?> ref )
	{
		return ref == null || ref.get() == null;
	}

	private final List<WeakReference<Type>> list = new CopyOnWriteArrayList<>();

	@Override
	public boolean add( Type value )
	{
		return list.add( new WeakReference<>( value ) );
	}

	@Override
	public void add( int index, Type value )
	{
		list.add( index, new WeakReference<>( value ) );
	}

	@Override
	public boolean addAll( @Nonnull Collection<? extends Type> values )
	{
		return list.addAll( values.stream().map( value -> new WeakReference<>( ( Type ) value ) ).collect( Collectors.toList() ) );
	}

	@Override
	public boolean addAll( int index, @Nonnull Collection<? extends Type> values )
	{
		return list.addAll( index, values.stream().map( value -> new WeakReference<>( ( Type ) value ) ).collect( Collectors.toList() ) );
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	@Override
	public boolean contains( Object obj )
	{
		return stream().anyMatch( obj::equals );
	}

	@Override
	public boolean containsAll( @Nonnull Collection<?> collection )
	{
		return stream().allMatch( collection::contains );
	}

	@Override
	public boolean equals( Object o )
	{
		return this == o;
	}

	private void filter()
	{
		list.stream().filter( WeakReferenceList::notValid ).forEach( list::remove );
	}

	@Override
	public Type get( int index )
	{
		filter();
		return list.get( index ).get();
	}

	@Override
	public int hashCode()
	{
		return list.hashCode();
	}

	@Override
	public int indexOf( Object obj )
	{
		return list().indexOf( obj );
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Nonnull
	@Override
	public Iterator<Type> iterator()
	{
		return list().iterator();
	}

	@Override
	public int lastIndexOf( Object obj )
	{
		return list().lastIndexOf( obj );
	}

	public List<Type> list()
	{
		return stream().collect( Collectors.toList() );
	}

	@Nonnull
	@Override
	public ListIterator<Type> listIterator()
	{
		return list().listIterator();
	}

	@Nonnull
	@Override
	public ListIterator<Type> listIterator( int index )
	{
		return list().listIterator( index );
	}

	@Override
	public Type remove( int index )
	{
		filter();
		return list.remove( index ).get();
	}

	@Override
	public boolean remove( Object obj )
	{
		list.stream().filter( obj::equals ).forEach( list::remove );
		return true;
	}

	@Override
	public boolean removeAll( @Nonnull Collection<?> collection )
	{
		list.stream().filter( collection::contains ).forEach( list::remove );
		return true;
	}

	@Override
	public boolean retainAll( @Nonnull Collection<?> collection )
	{
		list.stream().filter( obj -> !collection.contains( obj ) ).forEach( list::remove );
		return true;
	}

	@Override
	public Type set( int index, Type element )
	{
		filter();
		return list.set( index, new WeakReference<>( element ) ).get();
	}

	@Override
	public int size()
	{
		filter();
		return list.size();
	}

	@Override
	public Stream<Type> stream()
	{
		return list.stream().map( WeakReference::get ).filter( Objs::isNotNull );
	}

	@Nonnull
	@Override
	public List<Type> subList( int fromIndex, int toIndex )
	{
		return list().subList( fromIndex, toIndex );
	}

	@Nonnull
	@Override
	public Object[] toArray()
	{
		return list().toArray();
	}

	@Nonnull
	@Override
	public <V> V[] toArray( @Nonnull V[] array )
	{
		return list().toArray( array );
	}
}
