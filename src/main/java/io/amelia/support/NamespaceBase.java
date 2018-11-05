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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;

/**
 * Advanced class for handling namespaces with virtually any separator character.
 *
 * @param <T> Subclass of this class
 */
public abstract class NamespaceBase<T extends NamespaceBase> implements Cloneable, Comparable<T>
{
	// TODO Implement a flag setup that does things such as forces nodes to lowercase, normalizes, or converts to ASCII

	public static final Pattern RANGE_EXPRESSION = Pattern.compile( "(0-9+)-(0-9+)" );

	private static boolean containsRegex( String namespace )
	{
		return namespace.contains( "*" ) || namespace.matches( ".*[0-9]+-[0-9]+.*" );
	}

	private final NonnullFunction<String[], T> creator;
	protected String glue;
	protected String[] nodes;

	protected NamespaceBase( NonnullFunction<String[], T> creator, String glue, String[] nodes )
	{
		this.creator = creator;
		this.glue = glue;
		this.nodes = nodes; // Strs.toLowerCase()?
	}

	protected NamespaceBase( NonnullFunction<String[], T> creator, String glue, List<String> nodes )
	{
		this.creator = creator;
		this.glue = glue;
		this.nodes = nodes.toArray( new String[0] );// Strs.toLowerCase()?
	}

	protected NamespaceBase( NonnullFunction<String[], T> creator, String glue )
	{
		this.creator = creator;
		this.glue = glue;
		this.nodes = new String[0];
	}

	@SuppressWarnings( "unchecked" )
	public T append( @Nonnull String node )
	{
		if ( Strs.isEmpty( node ) )
			return ( T ) this;
		if ( node.contains( glue ) )
			throw new IllegalArgumentException( "Appended string MUST NOT contain the glue character." );
		this.nodes = Arrs.concat( this.nodes, new String[] {node} );
		return ( T ) this;
	}

	public T appendAndCreate( @Nonnull String node )
	{
		if ( Strs.isEmpty( node ) )
			return clone();
		if ( node.contains( glue ) )
			throw new IllegalArgumentException( "Appended string MUST NOT contain the glue character." );
		return creator.apply( Arrs.concat( this.nodes, new String[] {node} ) );
	}

	@Override
	public T clone()
	{
		return creator.apply( nodes );
	}

	public int compareTo( @Nonnull String other, String glue )
	{
		return Maths.normalizeCompare( matchPercentage( other, glue ), 100 );
	}

	public int compareTo( @Nonnull String other )
	{
		return Maths.normalizeCompare( matchPercentage( other ), 100 );
	}

	@Override
	public int compareTo( @Nonnull T other )
	{
		return Maths.normalizeCompare( matchPercentage( other ), 100 );
	}

	/**
	 * Checks is namespace only contains valid characters.
	 *
	 * @return True if namespace contains only valid characters
	 */
	public boolean containsOnlyValidChars()
	{
		for ( String n : nodes )
			if ( !n.matches( "[a-z0-9_]*" ) )
				return false;
		return true;
	}

	public boolean containsRegex()
	{
		for ( String s : nodes )
			if ( s.contains( "*" ) || s.matches( ".*[0-9]+-[0-9]+.*" ) )
				return true;
		return false;
	}

	private T create( String... nodes )
	{
		T node = creator.apply( nodes );
		node.glue = glue;
		return node;
	}

	public T dropFirst()
	{
		return subNodes( 1 );
	}

	public boolean endsWith( String other )
	{
		return getString().endsWith( other );
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( !( obj instanceof NamespaceBase ) )
			return false;

		NamespaceBase ns = ( NamespaceBase ) obj;

		if ( nodes.length != ns.nodes.length )
			return false;

		for ( int i = 0; i < nodes.length; i++ )
			if ( !nodes[i].equals( ns.nodes[i] ) )
				return false;

		return true;
	}

	public boolean equals( String namespace )
	{
		/*
		 * We are not going to try and match a permission if it contains regex.
		 * The other way around should be true and likely means someone got their strings backwards.
		 */
		if ( containsRegex( namespace ) )
			throw ApplicationException.runtime( "The namespace \"" + namespace + "\" contains wildcard/regex. This is usually a bug or the check was backwards." );

		return prepareRegexp().matcher( namespace ).matches();
	}

	/**
	 * Filters out invalid characters from namespace.
	 *
	 * @return The fixed {@link T}
	 */
	public T fixInvalidChars()
	{
		String[] result = new String[nodes.length];
		for ( int i = 0; i < nodes.length; i++ )
			result[i] = nodes[i].replaceAll( "[^a-z0-9_]", "" );
		return creator.apply( result );
	}

	public T getFirst()
	{
		return create( getStringFirst() );
	}

	public T getLast()
	{
		return create( getStringLast() );
	}

	public String getLocalName()
	{
		return nodes[nodes.length - 1];
	}

	public T getNode( int inx )
	{
		return create( getStringNode( inx ) );
	}

	public int getNodeCount()
	{
		return nodes.length;
	}

	public String[] getNodes()
	{
		return nodes;
	}

	public T getParent()
	{
		return create( getStringParent() );
	}

	public T getParent( int depth )
	{
		return create( getStringParent( depth ) );
	}

	public String getRootName()
	{
		return nodes[0];
	}

	public String getString()
	{
		return getString( false );
	}

	/**
	 * Converts Namespace to a String
	 *
	 * @param escape Shall we escape separator characters in node names
	 *
	 * @return The converted String
	 */
	public String getString( boolean escape )
	{
		if ( escape )
			return Arrays.stream( nodes ).filter( Strs::isNotEmpty ).map( n -> n.replace( glue, "\\" + glue ) ).collect( Collectors.joining( glue ) );
		return Arrays.stream( nodes ).filter( Strs::isNotEmpty ).collect( Collectors.joining( glue ) );
	}

	public String getStringFirst()
	{
		return getStringNode( 0 );
	}

	public String getStringLast()
	{
		return getStringNode( getNodeCount() - 1 );
	}

	public String getStringNode( int inx )
	{
		try
		{
			return nodes[inx];
		}
		catch ( IndexOutOfBoundsException e )
		{
			return null;
		}
	}

	public String getStringNodeWithException( int inx )
	{
		return nodes[inx];
	}

	public String getStringParent()
	{
		return getStringParent( 1 );
	}

	public String getStringParent( int depth )
	{
		if ( nodes.length <= depth )
			return "";

		return Strs.join( Arrays.copyOf( nodes, nodes.length - depth ), glue );
	}

	public boolean isEmpty()
	{
		return nodes.length == 0;
	}

	public int matchPercentage( @Nonnull String namespace, @Nonnull String glue )
	{
		return matchPercentage( splitString( namespace, glue ) );
	}

	/**
	 * Calculates the matching percentage of this namespace and the provided one.
	 *
	 * 0 = Not At All
	 * 1-99 = Partial Match
	 * 100 = Equals
	 * 100+ = Partial Match (Other starts with this namespace)
	 */
	int matchPercentage( @Nonnull String[] other )
	{
		int total = 0;
		int perNode = 100 / nodes.length; // Points per matching node.

		for ( int i = 0; i < Math.min( nodes.length, other.length ); i++ )
			if ( nodes[i].equals( other[i] ) )
				total += perNode;
			else
				return total;

		if ( other.length > nodes.length )
			total += 10 * ( other.length - nodes.length );

		return total;
	}

	public int matchPercentage( @Nonnull String namespace )
	{
		return matchPercentage( namespace, this.glue );
	}

	public int matchPercentage( @Nonnull T namespace )
	{
		return matchPercentage( namespace.nodes );
	}

	public T merge( Namespace ns )
	{
		return creator.apply( Stream.of( nodes, ns.nodes ).flatMap( Stream::of ).toArray( String[]::new ) );
	}

	/**
	 * Normalizes each node to ASCII and to lowercase using Locale US.
	 *
	 * @return The new normalized {@link T}
	 */
	public T normalizeAscii()
	{
		String[] result = new String[nodes.length];
		for ( int i = 0; i < nodes.length; i++ )
			result[i] = Strs.toAscii( nodes[i] ).toLowerCase( Locale.US );
		return creator.apply( result );
	}

	/**
	 * Normalizes each node to Unicode and to lowercase using Locale US.
	 *
	 * @return The new normalized {@link T}
	 */
	public T normalizeUnicode()
	{
		String[] result = new String[nodes.length];
		for ( int i = 0; i < nodes.length; i++ )
			result[i] = Strs.toUnicode( nodes[i] ).toLowerCase( Locale.US );
		return creator.apply( result );
	}

	/**
	 * Pops the last node
	 */
	public T pop()
	{
		return subNodes( 0, getNodeCount() - 1 );
	}

	/**
	 * Prepares a namespace for parsing via RegEx
	 *
	 * @return The fully RegEx ready string
	 */
	public Pattern prepareRegexp()
	{
		String regexpOrig = Strs.join( nodes, "\\." );
		String regexp = regexpOrig.replace( "*", "(.*)" );

		try
		{
			Matcher rangeMatcher = RANGE_EXPRESSION.matcher( regexp );
			while ( rangeMatcher.find() )
			{
				StringBuilder range = new StringBuilder();
				int from = Integer.parseInt( rangeMatcher.group( 1 ) );
				int to = Integer.parseInt( rangeMatcher.group( 2 ) );

				range.append( "(" );

				for ( int i = Math.min( from, to ); i <= Math.max( from, to ); i++ )
				{
					range.append( i );
					if ( i < Math.max( from, to ) )
						range.append( "|" );
				}

				range.append( ")" );

				regexp = regexp.replace( rangeMatcher.group( 0 ), range.toString() );
			}
		}
		catch ( Throwable e )
		{
			// Ignore
		}

		try
		{
			return Pattern.compile( regexp, Pattern.CASE_INSENSITIVE );
		}
		catch ( PatternSyntaxException e )
		{
			return Pattern.compile( Pattern.quote( regexpOrig.replace( "*", "(.*)" ) ), Pattern.CASE_INSENSITIVE );
		}
	}

	public T prepend( String... nodes )
	{
		return create( prependString( nodes ) );
	}

	@SuppressWarnings( "unchecked" )
	public String[] prependString( String... nodes )
	{
		if ( nodes.length == 0 )
			throw new IllegalArgumentException( "Nodes are empty" );
		if ( nodes.length == 1 )
			nodes = splitString( nodes[0] );
		return Arrs.concat( nodes, this.nodes );
	}

	public T replace( String literal, String replacement )
	{
		return create( replaceString( literal, replacement ) );
	}

	@SuppressWarnings( "unchecked" )
	public String[] replaceString( String literal, String replacement )
	{
		return Arrays.stream( nodes ).map( s -> s.replace( literal, replacement ) ).collect( Collectors.toList() ).toArray( new String[0] );
	}

	public T reverseOrder()
	{
		List<String> tmpNodes = Arrays.asList( nodes );
		Collections.reverse( tmpNodes );
		return create( tmpNodes.toArray( new String[0] ) );
	}

	private String[] splitString( @Nonnull String str, String separator )
	{
		separator = Objs.notEmptyOrDef( separator, glue );
		return Strs.split( str, separator ).filter( Strs::isNotEmpty ).toArray( String[]::new );
	}

	private String[] splitString( @Nonnull String str )
	{
		return splitString( str, null );
	}

	public boolean startsWith( @Nonnull T namespace )
	{
		return startsWith( namespace, true );
	}

	/**
	 * Computes if this namespace starts with the provided namespace.
	 * <p>
	 * ex: (Left being this namespace, right being the provided namespace)
	 * <pre>
	 *   True: "com.google.exampleSite.home" == "com.google.exampleSite"
	 *   False: "com.google.exampleSite.home" == "com.google.example"
	 * </pre>
	 *
	 * @param namespace        The namespace this namespace might start with.
	 * @param matchAtSeparator Should we only match each node or can the last node partially match? Setting this to FALSE would result in both examples above being TRUE.
	 *
	 * @return Does this namespace start with the provided namespace. True if so, false otherwise.
	 */
	public boolean startsWith( @Nonnull T namespace, boolean matchAtSeparator )
	{
		Objs.notNull( namespace );

		if ( namespace.getNodeCount() == 0 )
			return true;

		if ( namespace.getNodeCount() > getNodeCount() )
			return false;

		for ( int i = 0; i < namespace.getNodeCount(); i++ )
			if ( !namespace.nodes[i].equals( nodes[i] ) )
				return !matchAtSeparator && i + 1 == namespace.getNodeCount() && nodes[i].startsWith( namespace.nodes[i] );

		return true;
	}

	public boolean startsWith( @Nonnull String namespace )
	{
		return startsWith( namespace, null );
	}

	public boolean startsWith( @Nonnull String namespace, String separator )
	{
		return startsWith( creator.apply( splitString( namespace, separator ) ), true );
	}

	public boolean startsWith( @Nonnull String namespace, boolean matchAtSeparator )
	{
		return startsWith( namespace, null, matchAtSeparator );
	}

	public boolean startsWith( @Nonnull String namespace, String separator, boolean matchAtSeparator )
	{
		return startsWith( creator.apply( splitString( namespace, separator ) ), matchAtSeparator );
	}

	public String[] subArray( int start, int end )
	{
		if ( start < 0 )
			throw new IllegalArgumentException( "Start can't be less than 0" );
		if ( start > nodes.length )
			throw new IllegalArgumentException( "Start can't be more than length " + nodes.length );
		if ( end > nodes.length )
			throw new IllegalArgumentException( "End can't be more than node count" );

		return Arrays.copyOfRange( nodes, start, end );
	}

	public String[] subArray( int start )
	{
		return subArray( start, getNodeCount() );
	}

	public T subNodes( int start )
	{
		return subNodes( start, getNodeCount() );
	}

	public T subNodes( int start, int end )
	{
		return create( subArray( start, end ) );
	}

	public String subString( int start )
	{
		return subString( start, getNodeCount() );
	}

	public String subString( int start, int end )
	{
		return Strs.join( subArray( start, end ), glue );
	}

	@Override
	public String toString()
	{
		return getString();
	}
}
