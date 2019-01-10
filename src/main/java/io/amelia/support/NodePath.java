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

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodePath extends NodeStack<NodePath> implements ImplPath<NodePath>
{
	public static final Separator DEFAULT_SEPARATOR = Separator.FORWARDSLASH;

	public static NodePath empty()
	{
		return empty( DEFAULT_SEPARATOR );
	}

	public static NodePath empty( Separator separator )
	{
		return new NodePath( new String[0], separator );
	}

	public static NodePath of( String path )
	{
		return of( path, DEFAULT_SEPARATOR );
	}

	public static NodePath of( @Nullable String path, @Nonnull Separator separator )
	{
		path = Objs.notNullOrDef( path, "" );
		return new NodePath( Strs.split( path, Pattern.compile( separator.getSeparator(), Pattern.LITERAL ) ).collect( Collectors.toList() ), separator ).setAbsolute( path.startsWith( separator.getSeparator() ) );
	}

	public static NodePath of( @Nonnull Collection<String> nodes, @Nonnull Separator separator )
	{
		return new NodePath( nodes );
	}

	public static NodePath of( @Nonnull String[] nodes, @Nonnull Separator separator )
	{
		return new NodePath( nodes );
	}

	public static NodePath of( @Nonnull Path path, @Nonnull Separator separator )
	{
		return of( IO.getNames( path ) );
	}

	public static NodePath of( @Nonnull Collection<String> nodes )
	{
		return new NodePath( nodes );
	}

	public static NodePath of( @Nonnull String[] nodes )
	{
		return new NodePath( nodes );
	}

	public static NodePath of( @Nonnull Path path )
	{
		return of( IO.getNames( path ) );
	}

	private boolean isAbsolute;

	private NodePath( String[] nodes, @Nonnull Separator separator )
	{
		super( NodePath::new, separator.getSeparator(), nodes );
	}

	private NodePath( Collection<String> nodes, @Nonnull Separator separator )
	{
		super( NodePath::new, separator.getSeparator(), nodes );
	}

	private NodePath( @Nonnull Separator separator )
	{
		super( NodePath::new, separator.getSeparator() );
	}

	private NodePath( NodePath from, String[] nodes )
	{
		this( nodes );
		isAbsolute = from.isAbsolute;
	}

	private NodePath( String[] nodes )
	{
		this( nodes, DEFAULT_SEPARATOR );
	}

	private NodePath( Collection<String> nodes )
	{
		this( nodes, DEFAULT_SEPARATOR );
	}

	private NodePath()
	{
		this( DEFAULT_SEPARATOR );
	}

	public NodePath append( @Nonnull NodePath node )
	{
		if ( node.isEmpty() )
			return this;
		this.nodes = Arrs.concat( this.nodes, node.nodes );
		return this;
	}

	public NodePath appendAndCreate( @Nonnull NodePath node )
	{
		if ( node.isEmpty() )
			return clone();
		return create( Arrs.concat( this.nodes, node.nodes ) );
	}

	@Override
	public int compareTo( Path other )
	{
		return super.compareTo( other.toString(), File.pathSeparator );
	}

	@Override
	public NodePath create( String... nodes )
	{
		return super.create( nodes );
	}

	@Override
	public NodePath getDefaultPath()
	{
		return NodePath.empty().setAbsolute( true );
	}

	@Override
	public int getNameCount()
	{
		return getNodeCount();
	}

	public String getSeparator()
	{
		return glue;
	}

	@Override
	public String getString( boolean escape )
	{
		return ( isAbsolute ? glue : "" ) + super.getString( escape );
	}

	public boolean isAbsolute()
	{
		return isAbsolute;
	}

	public NodePath setAbsolute( boolean absolute )
	{
		isAbsolute = absolute;
		return this;
	}

	public NodePath setSeparator( Separator separator )
	{
		glue = separator.getSeparator();
		return this;
	}

	@Override
	public NodePath subpath( int beginIndex, int endIndex )
	{
		return super.getSubNodes( beginIndex, endIndex );
	}

	@Override
	public File toFile()
	{
		return new File( getString( File.separator ) );
	}

	public Namespace toNamespace()
	{
		return Namespace.of( getNames(), glue );
	}

	public enum Separator
	{
		FORWARDSLASH( "/" ),
		BACKSLASH( "\\" ),
		UNDERSCORE( "_" ),
		;

		private final String separator;

		Separator( String separator )
		{
			this.separator = separator;
		}

		public String getSeparator()
		{
			return separator;
		}
	}
}
