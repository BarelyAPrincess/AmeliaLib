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

import com.sun.istack.internal.NotNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AsciiLower implements CharSequence, Comparable<CharSequence>
{
	/* Common private utility method used to bounds check the byte array
	 * and requested offset & length values used by the String(byte[],..)
	 * constructors.
	 */
	private static void checkBounds( byte[] bytes, int offset, int length )
	{
		if ( length < 0 )
			throw new StringIndexOutOfBoundsException( length );
		if ( offset < 0 )
			throw new StringIndexOutOfBoundsException( offset );
		if ( offset > bytes.length - length )
			throw new StringIndexOutOfBoundsException( offset + length );
	}

	/**
	 * The value is used for character storage.
	 */
	private final char value[];
	/**
	 * Cache the hash code for the string
	 */
	private int hash; // Default to 0

	/**
	 * Initializes a newly created {@code String} object so that it represents
	 * an empty character sequence.  Note that use of this constructor is
	 * unnecessary since Strings are immutable.
	 */
	public AsciiLower()
	{
		this.value = new char[0];
	}

	/**
	 * Initializes a newly created {@code String} object so that it represents
	 * the same sequence of characters as the argument; in other words, the
	 * newly created string is a copy of the argument string. Unless an
	 * explicit copy of {@code original} is needed, use of this constructor is
	 * unnecessary since Strings are immutable.
	 *
	 * @param original A {@code String}
	 */
	public AsciiLower( CharSequence original )
	{
		this.value = original.toString().toLowerCase().toCharArray();
	}

	/**
	 * Allocates a new {@code String} so that it represents the sequence of
	 * characters currently contained in the character array argument. The
	 * contents of the character array are copied; subsequent modification of
	 * the character array does not affect the newly created string.
	 *
	 * @param value The initial value of the string
	 */
	public AsciiLower( char value[] )
	{
		value = Strs.toLowerCase( value );
		this.value = Arrays.copyOf( value, value.length );
	}

	/**
	 * Allocates a new {@code String} that contains characters from a subarray
	 * of the character array argument. The {@code offset} argument is the
	 * index of the first character of the subarray and the {@code count}
	 * argument specifies the length of the subarray. The contents of the
	 * subarray are copied; subsequent modification of the character array does
	 * not affect the newly created string.
	 *
	 * @param value  Array that is the source of characters
	 * @param offset The initial offset
	 * @param count  The length
	 *
	 * @throws IndexOutOfBoundsException If the {@code offset} and {@code count} arguments index
	 *                                   characters outside the bounds of the {@code value} array
	 */
	public AsciiLower( char value[], int offset, int count )
	{
		if ( offset < 0 )
			throw new StringIndexOutOfBoundsException( offset );
		if ( count <= 0 )
		{
			if ( count < 0 )
				throw new StringIndexOutOfBoundsException( count );
			if ( offset <= value.length )
			{
				this.value = new char[0];
				return;
			}
		}
		// Note: offset or count might be near -1>>>1.
		if ( offset > value.length - count )
			throw new StringIndexOutOfBoundsException( offset + count );
		this.value = Strs.toLowerCase( Arrays.copyOfRange( value, offset, offset + count ) );
	}

	/**
	 * Constructs a new {@code String} by decoding the specified subarray of
	 * bytes using the specified charset.  The length of the new {@code String}
	 * is a function of the charset, and hence may not be equal to the length
	 * of the subarray.
	 *
	 * <p> The behavior of this constructor when the given bytes are not valid
	 * in the given charset is unspecified.  The {@link
	 * java.nio.charset.CharsetDecoder} class should be used when more control
	 * over the decoding process is required.
	 *
	 * @param bytes       The bytes to be decoded into characters
	 * @param offset      The index of the first byte to decode
	 * @param length      The number of bytes to decode
	 * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
	 *                    charset}
	 *
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @throws IndexOutOfBoundsException    If the {@code offset} and {@code length} arguments index
	 *                                      characters outside the bounds of the {@code bytes} array
	 * @since JDK1.1
	 */
	public AsciiLower( byte bytes[], int offset, int length, String charsetName ) throws UnsupportedEncodingException
	{
		if ( charsetName == null )
			throw new NullPointerException( "charsetName" );
		checkBounds( bytes, offset, length );
		this.value = Strs.toLowerCase( Strs.StringCoding.decode( charsetName, bytes, offset, length ) );
	}

	/**
	 * Constructs a new {@code String} by decoding the specified subarray of
	 * bytes using the specified {@linkplain java.nio.charset.Charset charset}.
	 * The length of the new {@code String} is a function of the charset, and
	 * hence may not be equal to the length of the subarray.
	 *
	 * <p> This method always replaces malformed-input and unmappable-character
	 * sequences with this charset's default replacement string.  The {@link
	 * java.nio.charset.CharsetDecoder} class should be used when more control
	 * over the decoding process is required.
	 *
	 * @param bytes   The bytes to be decoded into characters
	 * @param offset  The index of the first byte to decode
	 * @param length  The number of bytes to decode
	 * @param charset The {@linkplain java.nio.charset.Charset charset} to be used to
	 *                decode the {@code bytes}
	 *
	 * @throws IndexOutOfBoundsException If the {@code offset} and {@code length} arguments index
	 *                                   characters outside the bounds of the {@code bytes} array
	 * @since 1.6
	 */
	public AsciiLower( byte bytes[], int offset, int length, Charset charset )
	{
		if ( charset == null )
			throw new NullPointerException( "charset" );
		checkBounds( bytes, offset, length );
		this.value = Strs.toLowerCase( Strs.StringCoding.decode( charset, bytes, offset, length ) );
	}

	/**
	 * Constructs a new {@code String} by decoding the specified array of bytes
	 * using the specified {@linkplain java.nio.charset.Charset charset}.  The
	 * length of the new {@code String} is a function of the charset, and hence
	 * may not be equal to the length of the byte array.
	 *
	 * <p> The behavior of this constructor when the given bytes are not valid
	 * in the given charset is unspecified.  The {@link
	 * java.nio.charset.CharsetDecoder} class should be used when more control
	 * over the decoding process is required.
	 *
	 * @param bytes       The bytes to be decoded into characters
	 * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
	 *                    charset}
	 *
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 * @since JDK1.1
	 */
	public AsciiLower( byte bytes[], String charsetName ) throws UnsupportedEncodingException
	{
		this( bytes, 0, bytes.length, charsetName );
	}

	/**
	 * Constructs a new {@code String} by decoding the specified array of
	 * bytes using the specified {@linkplain java.nio.charset.Charset charset}.
	 * The length of the new {@code String} is a function of the charset, and
	 * hence may not be equal to the length of the byte array.
	 *
	 * <p> This method always replaces malformed-input and unmappable-character
	 * sequences with this charset's default replacement string.  The {@link
	 * java.nio.charset.CharsetDecoder} class should be used when more control
	 * over the decoding process is required.
	 *
	 * @param bytes   The bytes to be decoded into characters
	 * @param charset The {@linkplain java.nio.charset.Charset charset} to be used to
	 *                decode the {@code bytes}
	 *
	 * @since 1.6
	 */
	public AsciiLower( byte bytes[], Charset charset )
	{
		this( bytes, 0, bytes.length, charset );
	}

	/**
	 * Constructs a new {@code AsciiKey} by decoding the specified subarray of
	 * bytes using the platform's default charset.  The length of the new
	 * {@code String} is a function of the charset, and hence may not be equal
	 * to the length of the subarray.
	 *
	 * <p> The behavior of this constructor when the given bytes are not valid
	 * in the default charset is unspecified.  The {@link
	 * java.nio.charset.CharsetDecoder} class should be used when more control
	 * over the decoding process is required.
	 *
	 * @param bytes  The bytes to be decoded into characters
	 * @param offset The index of the first byte to decode
	 * @param length The number of bytes to decode
	 *
	 * @throws IndexOutOfBoundsException If the {@code offset} and the {@code length} arguments index
	 *                                   characters outside the bounds of the {@code bytes} array
	 */
	public AsciiLower( byte bytes[], int offset, int length )
	{
		checkBounds( bytes, offset, length );
		this.value = Strs.toLowerCase( Strs.StringCoding.decode( bytes, offset, length ) );
	}

	/**
	 * Constructs a new {@code String} by decoding the specified array of bytes
	 * using the platform's default charset.  The length of the new {@code
	 * String} is a function of the charset, and hence may not be equal to the
	 * length of the byte array.
	 *
	 * <p> The behavior of this constructor when the given bytes are not valid
	 * in the default charset is unspecified.  The {@link
	 * java.nio.charset.CharsetDecoder} class should be used when more control
	 * over the decoding process is required.
	 *
	 * @param bytes The bytes to be decoded into characters
	 */
	public AsciiLower( byte bytes[] )
	{
		this( bytes, 0, bytes.length );
	}

	/*
	 * Package private constructor which shares value array for speed.
	 * this constructor is always expected to be called with share==true.
	 * a separate constructor is needed because we already have a public
	 * String(char[]) constructor that makes a copy of the given char[].
	 */
	AsciiLower( char[] value, boolean share )
	{
		// assert share : "unshared not supported";
		this.value = value;
	}

	/**
	 * Returns the {@code char} value at the
	 * specified index. An index ranges from {@code 0} to
	 * {@code length() - 1}. The first {@code char} value of the sequence
	 * is at index {@code 0}, the next at index {@code 1},
	 * and so on, as for array indexing.
	 *
	 * <p>If the {@code char} value specified by the index is a
	 * <a href="Character.html#unicode">surrogate</a>, the surrogate
	 * value is returned.
	 *
	 * @param index the index of the {@code char} value.
	 *
	 * @return the {@code char} value at the specified index of this string.
	 * The first {@code char} value is at index {@code 0}.
	 *
	 * @throws IndexOutOfBoundsException if the {@code index}
	 *                                   argument is negative or not less than the length of this
	 *                                   string.
	 */
	public char charAt( int index )
	{
		if ( ( index < 0 ) || ( index >= value.length ) )
			throw new StringIndexOutOfBoundsException( index );
		return value[index];
	}

	/**
	 * Compares the specified string to this string using the ASCII values of the characters. Returns 0 if the strings
	 * contain the same characters in the same order. Returns a negative integer if the first non-equal character in
	 * this string has an ASCII value which is less than the ASCII value of the character at the same position in the
	 * specified string, or if this string is a prefix of the specified string. Returns a positive integer if the first
	 * non-equal character in this string has a ASCII value which is greater than the ASCII value of the character at
	 * the same position in the specified string, or if the specified string is a prefix of this string.
	 *
	 * @param charSequence the string to compare.
	 *
	 * @return 0 if the strings are equal, a negative integer if this string is before the specified string, or a
	 * positive integer if this string is after the specified string.
	 *
	 * @throws NullPointerException if {@code string} is {@code null}.
	 */
	@Override
	public int compareTo( @NotNull CharSequence charSequence )
	{
		if ( this == charSequence )
			return 0;

		int result;
		int length1 = length();
		int length2 = charSequence.length();
		int minLength = Math.min( length1, length2 );
		for ( int i = 0; i < minLength; i++ )
		{
			result = value[i] - charSequence.charAt( i );
			if ( result != 0 )
				return result;
		}

		return length1 - length2;
	}

	/**
	 * Concatenates the specified string to the end of this string.
	 * <p>
	 * If the length of the argument string is {@code 0}, then this
	 * {@code String} object is returned. Otherwise, a
	 * {@code String} object is returned that represents a character
	 * sequence that is the concatenation of the character sequence
	 * represented by this {@code String} object and the character
	 * sequence represented by the argument string.<p>
	 * Examples:
	 * <blockquote><pre>
	 * "cares".concat("s") returns "caress"
	 * "to".concat("get").concat("her") returns "together"
	 * </pre></blockquote>
	 *
	 * @param str the {@code AsciiKey} that is concatenated to the end
	 *            of this {@code AsciiKey}.
	 *
	 * @return a string that represents the concatenation of this object's
	 * characters followed by the string argument's characters.
	 */
	public AsciiLower concat( AsciiLower str )
	{
		int otherLen = str.length();
		if ( otherLen == 0 )
		{
			return this;
		}
		int len = value.length;
		char buf[] = Arrays.copyOf( value, len + otherLen );
		str.getChars( buf, len );
		return new AsciiLower( buf, true );
	}

	/**
	 * Returns true if and only if this string contains the specified
	 * sequence of char values.
	 *
	 * @param s the sequence to search for
	 *
	 * @return true if this string contains {@code s}, false otherwise
	 *
	 * @since 1.5
	 */
	public boolean contains( CharSequence s )
	{
		return indexOf( s.toString() ) > -1;
	}

	/**
	 * Tests if this string ends with the specified suffix.
	 *
	 * @param suffix the suffix.
	 *
	 * @return {@code true} if the character sequence represented by the
	 * argument is a suffix of the character sequence represented by
	 * this object; {@code false} otherwise. Note that the
	 * result will be {@code true} if the argument is the
	 * empty string or is equal to this {@code String} object
	 * as determined by the {@link #equals(Object)} method.
	 */
	public boolean endsWith( CharSequence suffix )
	{
		return startsWith( suffix, value.length - suffix.length() );
	}

	/**
	 * Compares this string to the specified object.  The result is {@code
	 * true} if and only if the argument is not {@code null} and is a {@code
	 * String} object that represents the same sequence of characters as this
	 * object.
	 *
	 * @param obj The object to compare this {@code String} against
	 *
	 * @return {@code true} if the given object represents a {@code String}
	 * equivalent to this string, {@code false} otherwise
	 *
	 * @see #compareTo(CharSequence)
	 * @see #equalsIgnoreCase(CharSequence)
	 */
	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj instanceof CharSequence )
			return compareTo( ( CharSequence ) obj ) == 0;
		return false;
	}

	/**
	 * Compares this {@code String} to another {@code String}, ignoring case
	 * considerations.  Two strings are considered equal ignoring case if they
	 * are of the same length and corresponding characters in the two strings
	 * are equal ignoring case.
	 *
	 * <p> Two characters {@code c1} and {@code c2} are considered the same
	 * ignoring case if at least one of the following is true:
	 * <ul>
	 * <li> The two characters are the same (as compared by the
	 * {@code ==} operator)
	 * <li> Applying the method {@link
	 * java.lang.Character#toUpperCase(char)} to each character
	 * produces the same result
	 * <li> Applying the method {@link
	 * java.lang.Character#toLowerCase(char)} to each character
	 * produces the same result
	 * </ul>
	 *
	 * @param charSequence The {@code String} to compare this {@code String} against
	 *
	 * @return {@code true} if the argument is not {@code null} and it
	 * represents an equivalent {@code String} ignoring case; {@code
	 * false} otherwise
	 *
	 * @see #equals(Object)
	 */
	public boolean equalsIgnoreCase( CharSequence charSequence )
	{
		if ( this == charSequence )
			return true;
		return toString().equalsIgnoreCase( charSequence.toString() );
	}

	/**
	 * Encodes this {@code String} into a sequence of bytes using the named
	 * charset, storing the result into a new byte array.
	 *
	 * <p> The behavior of this method when this string cannot be encoded in
	 * the given charset is unspecified.  The {@link
	 * java.nio.charset.CharsetEncoder} class should be used when more control
	 * over the encoding process is required.
	 *
	 * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
	 *                    charset}
	 *
	 * @return The resultant byte array
	 *
	 * @throws UnsupportedEncodingException If the named charset is not supported
	 */
	public byte[] getBytes( String charsetName ) throws UnsupportedEncodingException
	{
		return toString().getBytes( charsetName );
	}

	/**
	 * Encodes this {@code String} into a sequence of bytes using the given
	 * {@linkplain java.nio.charset.Charset charset}, storing the result into a
	 * new byte array.
	 *
	 * <p> This method always replaces malformed-input and unmappable-character
	 * sequences with this charset's default replacement byte array.  The
	 * {@link java.nio.charset.CharsetEncoder} class should be used when more
	 * control over the encoding process is required.
	 *
	 * @param charset The {@linkplain java.nio.charset.Charset} to be used to encode
	 *                the {@code String}
	 *
	 * @return The resultant byte array
	 */
	public byte[] getBytes( Charset charset )
	{
		return toString().getBytes( charset );
	}

	/**
	 * Encodes this {@code String} into a sequence of bytes using the
	 * platform's default charset, storing the result into a new byte array.
	 *
	 * <p> The behavior of this method when this string cannot be encoded in
	 * the default charset is unspecified.  The {@link
	 * java.nio.charset.CharsetEncoder} class should be used when more control
	 * over the encoding process is required.
	 *
	 * @return The resultant byte array
	 */
	public byte[] getBytes()
	{
		return toString().getBytes();
	}

	/**
	 * Copy characters from this string into dst starting at dstBegin.
	 * This method doesn't perform any range checking.
	 */
	void getChars( char dst[], int dstBegin )
	{
		System.arraycopy( value, 0, dst, dstBegin, value.length );
	}

	/**
	 * Copies characters from this string into the destination character
	 * array.
	 * <p>
	 * The first character to be copied is at index {@code srcBegin};
	 * the last character to be copied is at index {@code srcEnd-1}
	 * (thus the total number of characters to be copied is
	 * {@code srcEnd-srcBegin}). The characters are copied into the
	 * subarray of {@code dst} starting at index {@code dstBegin}
	 * and ending at index:
	 * <blockquote><pre>
	 *     dstBegin + (srcEnd-srcBegin) - 1
	 * </pre></blockquote>
	 *
	 * @param srcBegin index of the first character in the string
	 *                 to copy.
	 * @param srcEnd   index after the last character in the string
	 *                 to copy.
	 * @param dst      the destination array.
	 * @param dstBegin the start offset in the destination array.
	 *
	 * @throws IndexOutOfBoundsException If any of the following
	 *                                   is true:
	 *                                   <ul><li>{@code srcBegin} is negative.
	 *                                   <li>{@code srcBegin} is greater than {@code srcEnd}
	 *                                   <li>{@code srcEnd} is greater than the length of this
	 *                                   string
	 *                                   <li>{@code dstBegin} is negative
	 *                                   <li>{@code dstBegin+(srcEnd-srcBegin)} is larger than
	 *                                   {@code dst.length}</ul>
	 */
	public void getChars( int srcBegin, int srcEnd, char dst[], int dstBegin )
	{
		if ( srcBegin < 0 )
			throw new StringIndexOutOfBoundsException( srcBegin );
		if ( srcEnd > value.length )
			throw new StringIndexOutOfBoundsException( srcEnd );
		if ( srcBegin > srcEnd )
			throw new StringIndexOutOfBoundsException( srcEnd - srcBegin );
		System.arraycopy( value, srcBegin, dst, dstBegin, srcEnd - srcBegin );
	}

	/**
	 * Returns a hash code for this string. The hash code for a
	 * {@code String} object is computed as
	 * <blockquote><pre>
	 * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
	 * </pre></blockquote>
	 * using {@code int} arithmetic, where {@code s[i]} is the
	 * <i>i</i>th character of the string, {@code n} is the length of
	 * the string, and {@code ^} indicates exponentiation.
	 * (The hash value of the empty string is zero.)
	 *
	 * @return a hash code value for this object.
	 */
	public int hashCode()
	{
		int h = hash;
		if ( h == 0 && value.length > 0 )
		{
			char val[] = value;

			for ( int i = 0; i < value.length; i++ )
				h = 31 * h + val[i];
			hash = h;
		}
		return h;
	}

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring.
	 *
	 * <p>The returned index is the smallest value <i>k</i> for which:
	 * <blockquote><pre>
	 * this.startsWith(str, <i>k</i>)
	 * </pre></blockquote>
	 * If no such value of <i>k</i> exists, then {@code -1} is returned.
	 *
	 * @param str the substring to search for.
	 *
	 * @return the index of the first occurrence of the specified substring,
	 * or {@code -1} if there is no such occurrence.
	 */
	public int indexOf( CharSequence str )
	{
		return indexOf( str, 0 );
	}

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring, starting at the specified index.
	 *
	 * <p>The returned index is the smallest value <i>k</i> for which:
	 * <blockquote><pre>
	 * <i>k</i> &gt;= fromIndex {@code &&} this.startsWith(str, <i>k</i>)
	 * </pre></blockquote>
	 * If no such value of <i>k</i> exists, then {@code -1} is returned.
	 *
	 * @param str       the substring to search for.
	 * @param fromIndex the index from which to start the search.
	 *
	 * @return the index of the first occurrence of the specified substring,
	 * starting at the specified index,
	 * or {@code -1} if there is no such occurrence.
	 */
	public int indexOf( CharSequence str, int fromIndex )
	{
		return toString().lastIndexOf( str.toString(), fromIndex );
	}

	/**
	 * Returns {@code true} if, and only if, {@link #length()} is {@code 0}.
	 *
	 * @return {@code true} if {@link #length()} is {@code 0}, otherwise
	 * {@code false}
	 *
	 * @since 1.6
	 */
	public boolean isEmpty()
	{
		return value.length == 0;
	}

	/**
	 * Returns the index within this string of the last occurrence of the
	 * specified substring.  The last occurrence of the empty string ""
	 * is considered to occur at the index value {@code this.length()}.
	 *
	 * <p>The returned index is the largest value <i>k</i> for which:
	 * <blockquote><pre>
	 * this.startsWith(str, <i>k</i>)
	 * </pre></blockquote>
	 * If no such value of <i>k</i> exists, then {@code -1} is returned.
	 *
	 * @param str the substring to search for.
	 *
	 * @return the index of the last occurrence of the specified substring,
	 * or {@code -1} if there is no such occurrence.
	 */
	public int lastIndexOf( CharSequence str )
	{
		return lastIndexOf( str, value.length );
	}

	/**
	 * Returns the index within this string of the last occurrence of the
	 * specified substring, searching backward starting at the specified index.
	 *
	 * <p>The returned index is the largest value <i>k</i> for which:
	 * <blockquote><pre>
	 * <i>k</i> {@code <=} fromIndex {@code &&} this.startsWith(str, <i>k</i>)
	 * </pre></blockquote>
	 * If no such value of <i>k</i> exists, then {@code -1} is returned.
	 *
	 * @param str       the substring to search for.
	 * @param fromIndex the index to start the search from.
	 *
	 * @return the index of the last occurrence of the specified substring,
	 * searching backward from the specified index,
	 * or {@code -1} if there is no such occurrence.
	 */
	public int lastIndexOf( CharSequence str, int fromIndex )
	{
		return toString().lastIndexOf( str.toString(), fromIndex );
	}

	/**
	 * Returns the length of this string.
	 * The length is equal to the number of <a href="Character.html#unicode">Unicode
	 * code units</a> in the string.
	 *
	 * @return the length of the sequence of characters represented by this
	 * object.
	 */
	public int length()
	{
		return value.length;
	}

	/**
	 * Tells whether or not this string matches the given <a
	 * href="../util/regex/Pattern.html#sum">regular expression</a>.
	 *
	 * <p> An invocation of this method of the form
	 * <i>str</i>{@code .matches(}<i>regex</i>{@code )} yields exactly the
	 * same result as the expression
	 *
	 * <blockquote>
	 * {@link java.util.regex.Pattern}.{@link java.util.regex.Pattern#matches(String, CharSequence)
	 * matches(<i>regex</i>, <i>str</i>)}
	 * </blockquote>
	 *
	 * @param regex the regular expression to which this string is to be matched
	 *
	 * @return {@code true} if, and only if, this string matches the
	 * given regular expression
	 *
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 * @spec JSR-51
	 * @see java.util.regex.Pattern
	 * @since 1.4
	 */
	public boolean matches( CharSequence regex )
	{
		return Pattern.matches( regex.toString(), this );
	}

	/**
	 * Returns a string resulting from replacing all occurrences of
	 * {@code oldChar} in this string with {@code newChar}.
	 * <p>
	 * If the character {@code oldChar} does not occur in the
	 * character sequence represented by this {@code String} object,
	 * then a reference to this {@code String} object is returned.
	 * Otherwise, a {@code String} object is returned that
	 * represents a character sequence identical to the character sequence
	 * represented by this {@code String} object, except that every
	 * occurrence of {@code oldChar} is replaced by an occurrence
	 * of {@code newChar}.
	 * <p>
	 * Examples:
	 * <blockquote><pre>
	 * "mesquite in your cellar".replace('e', 'o')
	 *         returns "mosquito in your collar"
	 * "the war of baronets".replace('r', 'y')
	 *         returns "the way of bayonets"
	 * "sparring with a purple porpoise".replace('p', 't')
	 *         returns "starring with a turtle tortoise"
	 * "JonL".replace('q', 'x') returns "JonL" (no change)
	 * </pre></blockquote>
	 *
	 * @param oldChar the old character.
	 * @param newChar the new character.
	 *
	 * @return a string derived from this string by replacing every
	 * occurrence of {@code oldChar} with {@code newChar}.
	 */
	public AsciiLower replace( char oldChar, char newChar )
	{
		if ( oldChar != newChar )
		{
			int len = value.length;
			int i = -1;
			char[] val = value; /* avoid getfield opcode */

			while ( ++i < len )
			{
				if ( val[i] == oldChar )
				{
					break;
				}
			}
			if ( i < len )
			{
				char buf[] = new char[len];
				for ( int j = 0; j < i; j++ )
				{
					buf[j] = val[j];
				}
				while ( i < len )
				{
					char c = val[i];
					buf[i] = ( c == oldChar ) ? newChar : c;
					i++;
				}
				return new AsciiLower( buf, true );
			}
		}
		return this;
	}

	/**
	 * Replaces each substring of this string that matches the literal target
	 * sequence with the specified literal replacement sequence. The
	 * replacement proceeds from the beginning of the string to the end, for
	 * example, replacing "aa" with "b" in the string "aaa" will result in
	 * "ba" rather than "ab".
	 *
	 * @param target      The sequence of char values to be replaced
	 * @param replacement The replacement sequence of char values
	 *
	 * @return The resulting string
	 */
	public AsciiLower replace( CharSequence target, CharSequence replacement )
	{
		return new AsciiLower( Pattern.compile( target.toString(), Pattern.LITERAL ).matcher( this ).replaceAll( Matcher.quoteReplacement( replacement.toString() ) ) );
	}

	/**
	 * Replaces each substring of this string that matches the given <a
	 * href="../util/regex/Pattern.html#sum">regular expression</a> with the
	 * given replacement.
	 *
	 * <p> An invocation of this method of the form
	 * <i>str</i>{@code .replaceAll(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
	 * yields exactly the same result as the expression
	 *
	 * <blockquote>
	 * <code>
	 * {@link java.util.regex.Pattern}.{@link
	 * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
	 * java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link
	 * java.util.regex.Matcher#replaceAll replaceAll}(<i>repl</i>)
	 * </code>
	 * </blockquote>
	 *
	 * <p>
	 * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
	 * replacement string may cause the results to be different than if it were
	 * being treated as a literal replacement string; see
	 * {@link java.util.regex.Matcher#replaceAll Matcher.replaceAll}.
	 * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
	 * meaning of these characters, if desired.
	 *
	 * @param regex       the regular expression to which this string is to be matched
	 * @param replacement the string to be substituted for each match
	 *
	 * @return The resulting {@code String}
	 *
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 * @see java.util.regex.Pattern
	 */
	public AsciiLower replaceAll( CharSequence regex, CharSequence replacement )
	{
		return new AsciiLower( Pattern.compile( regex.toString() ).matcher( this ).replaceAll( replacement.toString() ) );
	}

	/**
	 * Replaces the first substring of this string that matches the given <a
	 * href="../util/regex/Pattern.html#sum">regular expression</a> with the
	 * given replacement.
	 *
	 * <p> An invocation of this method of the form
	 * <i>str</i>{@code .replaceFirst(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
	 * yields exactly the same result as the expression
	 *
	 * <blockquote>
	 * <code>
	 * {@link java.util.regex.Pattern}.{@link
	 * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
	 * java.util.regex.Pattern#matcher(java.lang.CharSequence) matcher}(<i>str</i>).{@link
	 * java.util.regex.Matcher#replaceFirst replaceFirst}(<i>repl</i>)
	 * </code>
	 * </blockquote>
	 *
	 * <p>
	 * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
	 * replacement string may cause the results to be different than if it were
	 * being treated as a literal replacement string; see
	 * {@link java.util.regex.Matcher#replaceFirst}.
	 * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
	 * meaning of these characters, if desired.
	 *
	 * @param regex       the regular expression to which this string is to be matched
	 * @param replacement the string to be substituted for the first match
	 *
	 * @return The resulting {@code String}
	 *
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 * @see java.util.regex.Pattern
	 */
	public AsciiLower replaceFirst( CharSequence regex, CharSequence replacement )
	{
		return new AsciiLower( Pattern.compile( regex.toString() ).matcher( this ).replaceFirst( replacement.toString() ) );
	}

	/**
	 * Splits this string around matches of the given
	 * <a href="../util/regex/Pattern.html#sum">regular expression</a>.
	 *
	 * <p> The array returned by this method contains each substring of this
	 * string that is terminated by another substring that matches the given
	 * expression or is terminated by the end of the string.  The substrings in
	 * the array are in the order in which they occur in this string.  If the
	 * expression does not match any part of the input then the resulting array
	 * has just one element, namely this string.
	 *
	 * <p> When there is a positive-width match at the beginning of this
	 * string then an empty leading substring is included at the beginning
	 * of the resulting array. A zero-width match at the beginning however
	 * never produces such empty leading substring.
	 *
	 * <p> The {@code limit} parameter controls the number of times the
	 * pattern is applied and therefore affects the length of the resulting
	 * array.  If the limit <i>n</i> is greater than zero then the pattern
	 * will be applied at most <i>n</i>&nbsp;-&nbsp;1 times, the array's
	 * length will be no greater than <i>n</i>, and the array's last entry
	 * will contain all input beyond the last matched delimiter.  If <i>n</i>
	 * is non-positive then the pattern will be applied as many times as
	 * possible and the array can have any length.  If <i>n</i> is zero then
	 * the pattern will be applied as many times as possible, the array can
	 * have any length, and trailing empty strings will be discarded.
	 *
	 * <p> The string {@code "boo:and:foo"}, for example, yields the
	 * following results with these parameters:
	 *
	 * <blockquote><table cellpadding=1 cellspacing=0 summary="Split example showing regex, limit, and result">
	 * <tr>
	 * <th>Regex</th>
	 * <th>Limit</th>
	 * <th>Result</th>
	 * </tr>
	 * <tr><td align=center>:</td>
	 * <td align=center>2</td>
	 * <td>{@code { "boo", "and:foo" }}</td></tr>
	 * <tr><td align=center>:</td>
	 * <td align=center>5</td>
	 * <td>{@code { "boo", "and", "foo" }}</td></tr>
	 * <tr><td align=center>:</td>
	 * <td align=center>-2</td>
	 * <td>{@code { "boo", "and", "foo" }}</td></tr>
	 * <tr><td align=center>o</td>
	 * <td align=center>5</td>
	 * <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
	 * <tr><td align=center>o</td>
	 * <td align=center>-2</td>
	 * <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
	 * <tr><td align=center>o</td>
	 * <td align=center>0</td>
	 * <td>{@code { "b", "", ":and:f" }}</td></tr>
	 * </table></blockquote>
	 *
	 * <p> An invocation of this method of the form
	 * <i>str.</i>{@code split(}<i>regex</i>{@code ,}&nbsp;<i>n</i>{@code )}
	 * yields the same result as the expression
	 *
	 * <blockquote>
	 * <code>
	 * {@link java.util.regex.Pattern}.{@link
	 * java.util.regex.Pattern#compile compile}(<i>regex</i>).{@link
	 * java.util.regex.Pattern#split(java.lang.CharSequence, int) split}(<i>str</i>,&nbsp;<i>n</i>)
	 * </code>
	 * </blockquote>
	 *
	 * @param regex the delimiting regular expression
	 * @param limit the result threshold, as described above
	 *
	 * @return the array of strings computed by splitting this string
	 * around matches of the given regular expression
	 *
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 * @see java.util.regex.Pattern
	 */
	public AsciiLower[] split( CharSequence regex, int limit )
	{
		return Arrays.stream( toString().split( regex.toString(), limit ) ).map( AsciiLower::new ).toArray( AsciiLower[]::new );
	}

	/**
	 * Splits this string around matches of the given <a
	 * href="../util/regex/Pattern.html#sum">regular expression</a>.
	 *
	 * <p> This method works as if by invoking the two-argument {@link
	 * #split(CharSequence, int) split} method with the given expression and a limit
	 * argument of zero.  Trailing empty strings are therefore not included in
	 * the resulting array.
	 *
	 * <p> The string {@code "boo:and:foo"}, for example, yields the following
	 * results with these expressions:
	 *
	 * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex and result">
	 * <tr>
	 * <th>Regex</th>
	 * <th>Result</th>
	 * </tr>
	 * <tr><td align=center>:</td>
	 * <td>{@code { "boo", "and", "foo" }}</td></tr>
	 * <tr><td align=center>o</td>
	 * <td>{@code { "b", "", ":and:f" }}</td></tr>
	 * </table></blockquote>
	 *
	 * @param regex the delimiting regular expression
	 *
	 * @return the array of strings computed by splitting this string
	 * around matches of the given regular expression
	 *
	 * @throws PatternSyntaxException if the regular expression's syntax is invalid
	 * @spec JSR-51
	 * @see java.util.regex.Pattern
	 * @since 1.4
	 */
	public AsciiLower[] split( CharSequence regex )
	{
		return split( regex, 0 );
	}

	/**
	 * Tests if the substring of this string beginning at the
	 * specified index starts with the specified prefix.
	 *
	 * @param prefix  the prefix.
	 * @param tOffset where to begin looking in this string.
	 *
	 * @return {@code true} if the character sequence represented by the
	 * argument is a prefix of the substring of this object starting
	 * at index {@code toffset}; {@code false} otherwise.
	 * The result is {@code false} if {@code toffset} is
	 * negative or greater than the length of this
	 * {@code String} object; otherwise the result is the same
	 * as the result of the expression
	 * <pre>
	 * this.substring(toffset).startsWith(prefix)
	 * </pre>
	 */
	public boolean startsWith( CharSequence prefix, int tOffset )
	{
		int prefixArray[] = prefix.chars().toArray();
		int prefixOffset = 0;
		int prefixCount = prefix.length();
		// Note: toffset might be near -1>>>1.
		if ( ( tOffset < 0 ) || ( tOffset > value.length - prefixCount ) )
		{
			return false;
		}
		while ( --prefixCount >= 0 )
		{
			if ( value[tOffset++] != prefixArray[prefixOffset++] )
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests if this string starts with the specified prefix.
	 *
	 * @param prefix the prefix.
	 *
	 * @return {@code true} if the character sequence represented by the
	 * argument is a prefix of the character sequence represented by
	 * this string; {@code false} otherwise.
	 * Note also that {@code true} will be returned if the
	 * argument is an empty string or is equal to this
	 * {@code String} object as determined by the
	 * {@link #equals(Object)} method.
	 */
	public boolean startsWith( CharSequence prefix )
	{
		return startsWith( prefix, 0 );
	}

	@Override
	public CharSequence subSequence( int start, int end )
	{
		return substring( start, end );
	}

	/**
	 * Returns a string that is a substring of this string. The
	 * substring begins with the character at the specified index and
	 * extends to the end of this string. <p>
	 * Examples:
	 * <blockquote><pre>
	 * "unhappy".substring(2) returns "happy"
	 * "Harbison".substring(3) returns "bison"
	 * "emptiness".substring(9) returns "" (an empty string)
	 * </pre></blockquote>
	 *
	 * @param beginIndex the beginning index, inclusive.
	 *
	 * @return the specified substring.
	 *
	 * @throws IndexOutOfBoundsException if
	 *                                   {@code beginIndex} is negative or larger than the
	 *                                   length of this {@code String} object.
	 */
	public AsciiLower substring( int beginIndex )
	{
		if ( beginIndex < 0 )
		{
			throw new StringIndexOutOfBoundsException( beginIndex );
		}
		int subLen = value.length - beginIndex;
		if ( subLen < 0 )
		{
			throw new StringIndexOutOfBoundsException( subLen );
		}
		return ( beginIndex == 0 ) ? this : new AsciiLower( value, beginIndex, subLen );
	}

	/**
	 * Returns a string that is a substring of this string. The
	 * substring begins at the specified {@code beginIndex} and
	 * extends to the character at index {@code endIndex - 1}.
	 * Thus the length of the substring is {@code endIndex-beginIndex}.
	 * <p>
	 * Examples:
	 * <blockquote><pre>
	 * "hamburger".substring(4, 8) returns "urge"
	 * "smiles".substring(1, 5) returns "mile"
	 * </pre></blockquote>
	 *
	 * @param beginIndex the beginning index, inclusive.
	 * @param endIndex   the ending index, exclusive.
	 *
	 * @return the specified substring.
	 *
	 * @throws IndexOutOfBoundsException if the
	 *                                   {@code beginIndex} is negative, or
	 *                                   {@code endIndex} is larger than the length of
	 *                                   this {@code String} object, or
	 *                                   {@code beginIndex} is larger than
	 *                                   {@code endIndex}.
	 */
	public AsciiLower substring( int beginIndex, int endIndex )
	{
		if ( beginIndex < 0 )
		{
			throw new StringIndexOutOfBoundsException( beginIndex );
		}
		if ( endIndex > value.length )
		{
			throw new StringIndexOutOfBoundsException( endIndex );
		}
		int subLen = endIndex - beginIndex;
		if ( subLen < 0 )
		{
			throw new StringIndexOutOfBoundsException( subLen );
		}
		return ( ( beginIndex == 0 ) && ( endIndex == value.length ) ) ? this : new AsciiLower( value, beginIndex, subLen );
	}

	/**
	 * Converts this string to a new character array.
	 *
	 * @return a newly allocated character array whose length is the length
	 * of this string and whose contents are initialized to contain
	 * the character sequence represented by this string.
	 */
	public char[] toCharArray()
	{
		// Cannot use Arrays.copyOf because of class initialization order issues
		char result[] = new char[value.length];
		System.arraycopy( value, 0, result, 0, value.length );
		return result;
	}

	/**
	 * This object (which is already a string!) is itself returned.
	 *
	 * @return the string itself.
	 */
	public String toString()
	{
		return new String( value );
	}

	/**
	 * Returns a string whose value is this string, with any leading and trailing
	 * whitespace removed.
	 * <p>
	 * If this {@code String} object represents an empty character
	 * sequence, or the first and last characters of character sequence
	 * represented by this {@code String} object both have codes
	 * greater than {@code '\u005Cu0020'} (the space character), then a
	 * reference to this {@code String} object is returned.
	 * <p>
	 * Otherwise, if there is no character with a code greater than
	 * {@code '\u005Cu0020'} in the string, then a
	 * {@code String} object representing an empty string is
	 * returned.
	 * <p>
	 * Otherwise, let <i>k</i> be the index of the first character in the
	 * string whose code is greater than {@code '\u005Cu0020'}, and let
	 * <i>m</i> be the index of the last character in the string whose code
	 * is greater than {@code '\u005Cu0020'}. A {@code String}
	 * object is returned, representing the substring of this string that
	 * begins with the character at index <i>k</i> and ends with the
	 * character at index <i>m</i>-that is, the result of
	 * {@code this.substring(k, m + 1)}.
	 * <p>
	 * This method may be used to trim whitespace (as defined above) from
	 * the beginning and end of a string.
	 *
	 * @return A string whose value is this string, with any leading and trailing white
	 * space removed, or this string if it has no leading or
	 * trailing white space.
	 */
	public AsciiLower trim()
	{
		int len = value.length;
		int st = 0;
		char[] val = value;    /* avoid getfield opcode */

		while ( ( st < len ) && ( val[st] <= ' ' ) )
		{
			st++;
		}
		while ( ( st < len ) && ( val[len - 1] <= ' ' ) )
		{
			len--;
		}
		return ( ( st > 0 ) || ( len < value.length ) ) ? substring( st, len ) : this;
	}
}
