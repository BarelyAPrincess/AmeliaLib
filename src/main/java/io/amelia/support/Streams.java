/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Streams
{
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
