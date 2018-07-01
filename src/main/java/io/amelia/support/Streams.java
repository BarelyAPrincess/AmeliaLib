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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Streams
{
	public static final Object NOTHING = new Object();

	public static <T> Stream<T> recursive( final T first, final Function<T, Stream<T>> method )
	{
		return recursive( first, method, false );
	}

	public static <T> Stream<T> recursive( final T first, final Function<T, Stream<T>> method, boolean includeFirst )
	{
		Objs.notNull( method );
		return Stream.concat( includeFirst ? Stream.of( first ) : Stream.empty(), method.apply( first ).flatMap( element -> recursive( element, method, true ) ) );
	}

	public static <T> Stream<T> transverse( final T first, final UnaryOperator<T> method )
	{
		Objs.notNull( method );
		final Iterator<T> iterator = new Iterator<T>()
		{
			@SuppressWarnings( "unchecked" )
			T t = ( T ) NOTHING;

			@Override
			public boolean hasNext()
			{
				return t != null;
			}

			@Override
			public T next()
			{
				return t = ( t == NOTHING ) ? first : method.apply( t );
			}
		};
		return StreamSupport.stream( Spliterators.spliteratorUnknownSize( iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE ), false );
	}

	public static <T, E extends Exception> void forEachWithException( Stream<T> stream, ConsumerWithException<T, E> consumer ) throws E
	{
		AtomicReference<E> cachedException = new AtomicReference<>();
		try
		{
			stream.forEach( t -> {
				try
				{
					consumer.accept( t );
				}
				catch ( Exception exception )
				{
					try
					{
						cachedException.set( ( E ) exception );
						throw new RuntimeException();
					}
					catch ( ClassCastException classCastException )
					{
						throw new RuntimeException( exception );
					}
				}
			} );
		}
		catch ( RuntimeException runtimeException )
		{
			if ( cachedException.get() == null )
				throw runtimeException;
			throw cachedException.get();
		}
	}

	private Streams()
	{
		// Static Access
	}
}
