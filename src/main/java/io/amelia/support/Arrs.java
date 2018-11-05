/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import com.google.common.io.CharStreams;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manipulates arrays typically using Java 8 Streams
 */
@SuppressWarnings( "unchecked" )
public class Arrs
{
	public static <T> T[] append( @Nonnull T[] arr, T first )
	{
		return ( T[] ) Stream.concat( Arrays.stream( arr ), Stream.of( first ) ).toArray();
	}

	public static <T> T[] array( T... objs )
	{
		return objs;
	}

	// This is Arrays.binarySearch(), but doesn't do any argument validation.
	public static int binarySearch( int[] array, int size, int value )
	{
		int lo = 0;
		int hi = size - 1;

		while ( lo <= hi )
		{
			final int mid = ( lo + hi ) >>> 1;
			final int midVal = array[mid];

			if ( midVal < value )
				lo = mid + 1;
			else if ( midVal > value )
				hi = mid - 1;
			else
				return mid;  // value found
		}
		return ~lo;  // value not present
	}

	public static int binarySearch( long[] array, int size, long value )
	{
		int lo = 0;
		int hi = size - 1;

		while ( lo <= hi )
		{
			final int mid = ( lo + hi ) >>> 1;
			final long midVal = array[mid];

			if ( midVal < value )
				lo = mid + 1;
			else if ( midVal > value )
				hi = mid - 1;
			else
				return mid;  // value found
		}
		return ~lo;  // value not present
	}

	public static <T> boolean contains( T[] arr, T t )
	{
		return Arrays.stream( arr ).anyMatch( t::equals );
	}

	public static boolean contains( int[] arr, int t )
	{
		return IntStream.of( arr ).anyMatch( x -> x == t );
	}

	public static boolean contains( byte[] arr, byte t )
	{
		return byteStream( arr ).anyMatch( ( ( Byte ) t )::equals );
	}

	public static boolean contains( char[] arr, char t )
	{
		return charStream( arr ).anyMatch( ( ( Character ) t )::equals );
	}

	public static byte[] primitiveByteArray( Byte[] bytes )
	{
		byte[] newBytes = new byte[bytes.length];
		for ( int i = 0; i < bytes.length; i++ )
			newBytes[i] = bytes[i];
		return newBytes;
	}

	public static Stream<Byte> byteStream( byte[] bytes )
	{
		Byte[] newBytes = new Byte[bytes.length];
		for ( int i = 0; i < bytes.length; i++ )
			newBytes[i] = bytes[i];
		return Arrays.stream( newBytes );
	}

	public static Stream<Character> charStream( char[] chars )
	{
		return new ArrayList<Character>()
		{{
			for ( char c : chars )
				add( c );
		}}.stream();
	}

	public static int compareTo( long[] left, long[] right )
	{
		if ( left == right )
			return 0;

		if ( left == null )
			return -1;

		if ( right == null )
			return +1;

		if ( left.length > right.length )
			return +1;

		if ( right.length > left.length )
			return -1;

		for ( int i = 0; i < left.length; i++ )
			if ( left[i] > right[i] )
				return +1;
			else if ( right[i] > left[i] )
				return -1;

		return 0;
	}

	public static int compareTo( char[] left, char[] right )
	{
		if ( left == right )
			return 0;

		if ( left == null )
			return -1;

		if ( right == null )
			return +1;

		if ( left.length > right.length )
			return +1;

		if ( right.length > left.length )
			return -1;

		for ( int i = 0; i < left.length; i++ )
			if ( left[i] > right[i] )
				return +1;
			else if ( right[i] > left[i] )
				return -1;

		return 0;
	}

	public static int compareTo( byte[] left, byte[] right )
	{
		if ( left == right )
			return 0;

		if ( left == null )
			return -1;

		if ( right == null )
			return +1;

		if ( left.length > right.length )
			return +1;

		if ( right.length > left.length )
			return -1;

		for ( int i = 0; i < left.length; i++ )
			if ( left[i] > right[i] )
				return +1;
			else if ( right[i] > left[i] )
				return -1;

		return 0;
	}

	public static int compareToSum( long[] left, long[] right )
	{
		if ( left == right )
			return 0;

		if ( left == null )
			return -1;

		if ( right == null )
			return +1;

		BigInteger leftSum = BigInteger.ZERO;
		BigInteger rightSum = BigInteger.ZERO;

		for ( int i = 0; i < left.length; i++ )
		{
			leftSum = leftSum.add( BigInteger.valueOf( left[i] ) );
			rightSum = rightSum.add( BigInteger.valueOf( left[i] ) );
		}

		return leftSum.compareTo( rightSum );
	}

	public static byte[] concat( @Nonnull byte[]... arrays )
	{
		Objs.notNegativeOrZero( arrays.length );

		int length = 0;
		for ( byte[] array : arrays )
		{
			length += array.length;
		}

		final byte[] result = new byte[length];

		int offset = 0;
		for ( byte[] array : arrays )
		{
			System.arraycopy( array, 0, result, offset, array.length );
			offset += array.length;
		}

		return result;
	}

	public static <T> T[] concat( @Nonnull T[]... arr )
	{
		Objs.notNegativeOrZero( arr.length );
		return Arrays.stream( arr ).flatMap( Arrays::stream ).toArray( size -> Arrays.copyOf( arr[0], size ) );
	}

	public static <T> T[] limit( @Nonnull T[] arr, int limit )
	{
		return Arrays.stream( arr ).limit( limit ).toArray( size -> Arrays.copyOf( arr, size ) );
	}

	public static int[] pop( @Nonnull int[] arr )
	{
		return Arrays.copyOf( arr, arr.length - 1 );
	}

	public static byte[] pop( @Nonnull byte[] bytes )
	{
		return Arrays.copyOf( bytes, bytes.length - 1 );
	}

	public static <T> T[] pop( @Nonnull T[] arr )
	{
		return Arrays.copyOf( arr, arr.length - 1 );
	}

	public static <T> T[] prepend( @Nonnull T[] arr, T first )
	{
		return Stream.concat( Stream.of( first ), Arrays.stream( arr ) ).toArray( size -> Arrays.copyOf( arr, size ) );
	}

	public static int[] push( @Nonnull int[] bytes, int b )
	{
		int[] newBytes = Arrays.copyOf( bytes, bytes.length + 1 );
		newBytes[bytes.length] = b;
		return newBytes;
	}

	public static byte[] push( @Nonnull byte[] bytes, byte b )
	{
		byte[] newBytes = Arrays.copyOf( bytes, bytes.length + 1 );
		newBytes[bytes.length] = b;
		return newBytes;
	}

	public static <T> T[] push( @Nonnull T[] arr, @Nullable T obj )
	{
		T[] newArray = Arrays.copyOf( arr, arr.length + 1 );
		newArray[arr.length] = obj;
		return newArray;
	}

	public static <T> T[] skip( @Nonnull T[] arr, int skip )
	{
		return Arrays.stream( arr ).skip( skip ).toArray( size -> Arrays.copyOf( arr, size ) );
	}

	/**
	 * Copies a collection of {@code Character} instances into a new array of primitive {@code char}
	 * values.
	 *
	 * <p>Elements are copied from the argument collection as if by {@code
	 * collection.toArray()}. Calling this method is as thread-safe as calling that method.
	 *
	 * @param collection a collection of {@code Character} objects
	 *
	 * @return an array containing the same values as {@code collection}, in the same order, converted
	 * to primitives
	 *
	 * @throws NullPointerException if {@code collection} or any of its elements is null
	 */
	public static char[] toCharArray( Collection<Character> collection )
	{
		Object[] boxedArray = collection.toArray();
		int len = boxedArray.length;
		char[] array = new char[len];
		for ( int i = 0; i < len; i++ )
			array[i] = ( Character ) Objs.notNull( boxedArray[i] );
		return array;
	}

	public static Long[] toLongArray( Object obj )
	{
		if ( !obj.getClass().isArray() )
			throw new ArithmeticException( "Argument is not an array. Class " + obj.getClass().getSimpleName() + " detected." );

		List<Long> list = new ArrayList<>();

		if ( obj instanceof long[] )
			return Arrays.stream( ( long[] ) obj ).boxed().toArray( Long[]::new );
		else if ( obj instanceof double[] )
			return Arrays.stream( ( double[] ) obj ).boxed().map( Double::doubleToLongBits ).toArray( Long[]::new );
		else if ( obj instanceof int[] )
			return Arrays.stream( ( int[] ) obj ).boxed().map( Integer::longValue ).toArray( Long[]::new );
		else if ( obj instanceof byte[] )
			for ( int i = 0; i < ( ( byte[] ) obj ).length; i++ )
				list.add( Byte.toUnsignedLong( ( ( byte[] ) obj )[i] ) );
		else if ( obj instanceof char[] )
			for ( int i = 0; i < ( ( char[] ) obj ).length; i++ )
				list.add( ( long ) Character.codePointAt( ( char[] ) obj, i ) );
		else if ( obj instanceof short[] )
			for ( int i = 0; i < ( ( short[] ) obj ).length; i++ )
				list.add( Short.toUnsignedLong( ( ( short[] ) obj )[i] ) );
		else if ( obj instanceof float[] )
			for ( int i = 0; i < ( ( float[] ) obj ).length; i++ )
				list.add( ( long ) ( ( float[] ) obj )[i] );
		else if ( obj instanceof boolean[] ) // A boolean, does that even make sense? I mean it is a primitive.
			for ( int i = 0; i < ( ( boolean[] ) obj ).length; i++ )
				list.add( ( ( boolean[] ) obj )[i] ? 1L : 0L );
		else
		{
			try
			{
				return Arrays.stream( ( Object[] ) obj ).map( Objs::castToLongWithException ).toArray( Long[]::new );
			}
			catch ( ClassCastException e )
			{
				throw new ArithmeticException( "Argument can not be cast to a long array." );
			}
		}

		return list.toArray( new Long[0] );
	}

	private Arrs()
	{
		// Static Access
	}
}
