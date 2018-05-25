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
