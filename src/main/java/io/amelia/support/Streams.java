/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.tasks.Ticks;

public class Streams
{
	public static final Object NOTHING = new Object();

	public static <T, E extends Exception> void forEachWithException( Stream<T> stream, ConsumerWithException<T, E> consumer ) throws E
	{
		try
		{
			stream.forEach( t -> {
				try
				{
					consumer.accept( t );
				}
				catch ( Exception exception )
				{
					throw new SteamHaltException( exception );
				}
			} );
		}
		catch ( SteamHaltException exception )
		{
			try
			{
				throw ( E ) exception.getCause();
			}
			catch ( ClassCastException classCastException )
			{
				throw new RuntimeException( "Lambda threw an exception", exception.getCause() );
			}
		}
	}

	@Nonnull
	public static <T> Supplier<Stream<T>> fork( @Nonnull Stream<T> stream )
	{
		return fork( stream, 2 );
	}

	/**
	 * Takes an inlet stream and forks it into a near infinite number of distinct outlet streams.
	 * Unlike the conventional method of using a lambda to fork a stream,
	 * this method makes a perfect mirror copy of the provided stream.
	 * Each call to the returned supplier will result in a new stream attached to the provided stream.
	 *
	 * @param inletStream      The inlet stream we read from.
	 * @param estimatedStreams The estimated number of streams that will be requested from the resulting supplier.
	 *                         This method will block from reading the inlet stream until the estimated number of
	 *                         streams are requested or five-seconds have past.
	 *
	 * @return Returns a supplier that will spawn a new stream attached to the inlet stream.
	 *
	 * TODO WORK IN PROGRESS!!!
	 */
	@Nonnull
	public static <T> Supplier<Stream<T>> fork( @Nonnull Stream<T> inletStream, @Nonnegative int estimatedStreams )
	{
		List<BlockingQueue<T>> pendingQueues = new ArrayList<>();
		CompletableFuture<Void> future = CompletableFuture.runAsync( () -> {
			int timeoutCount = 0;
			while ( pendingQueues.size() < estimatedStreams && timeoutCount < Ticks.SECOND_5 )
			{
				try
				{
					Thread.sleep( 50 );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace();
					// Ignore
				}
				timeoutCount++;
			}
			if ( !pendingQueues.isEmpty() )
				inletStream.parallel().forEach( obj -> pendingQueues.forEach( queue -> queue.add( obj ) ) );
		} );

		Supplier<Spliterator<T>> supplier = () -> new Spliterator<T>()
		{
			BlockingQueue<T> queue = new LinkedBlockingQueue<>();

			{
				pendingQueues.add( queue );
			}

			@Override
			public int characteristics()
			{
				return 0;
			}

			@Override
			public long estimateSize()
			{
				return 0;
			}

			@Override
			public boolean tryAdvance( Consumer<? super T> action )
			{
				// We attempt to block until either an object is returned or the future indicates it finished.
				T obj = null;
				while ( obj == null )
				{
					if ( future.isDone() )
						return false;
					try
					{
						obj = queue.poll( 1L, TimeUnit.SECONDS );
					}
					catch ( InterruptedException e )
					{
						e.printStackTrace();
						// Ignore
					}
				}
				action.accept( obj );
				return true;
			}

			@Override
			public Spliterator<T> trySplit()
			{
				return null;
			}
		};

		return () -> StreamSupport.stream( supplier, 0, false );
	}

	public static <T> Stream<T> recursive( @Nonnull final T first, @Nonnull final Function<T, Stream<T>> method, boolean includeFirst )
	{
		return Stream.concat( includeFirst ? Stream.of( first ) : Stream.empty(), method.apply( first ).flatMap( element -> recursive( element, method, true ) ) );
	}

	public static <T> Stream<T> recursive( @Nonnull final T first, @Nonnull final Function<T, Stream<T>> method )
	{
		return recursive( first, method, false );
	}

	public static <T> Stream<T> transverse( @Nonnull final T first, @Nonnull final UnaryOperator<T> method )
	{
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

	private Streams()
	{
		// Static Access
	}

	private static class SteamHaltException extends RuntimeException
	{
		SteamHaltException( Exception exception )
		{
			super( exception );
		}
	}
}
