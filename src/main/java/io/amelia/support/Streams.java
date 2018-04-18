package io.amelia.support;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Streams
{
	public static <T, E extends Exception> void forEachWithException( Stream<T> stream, ConsumerWithException<T, E> consumer ) throws E
	{
		for ( T t : stream.collect( Collectors.toList() ) )
			consumer.accept( t );
	}

	private Streams()
	{
		// Static Access
	}
}
