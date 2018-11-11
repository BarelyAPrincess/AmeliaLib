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

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NodePath extends NodeStack<NodePath>
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

	private boolean isAbsolute;

	public NodePath( String[] nodes, @Nonnull Separator separator )
	{
		super( NodePath::new, separator.getSeparator(), nodes );
	}

	public NodePath( Collection<String> nodes, @Nonnull Separator separator )
	{
		super( NodePath::new, separator.getSeparator(), nodes );
	}

	public NodePath( @Nonnull Separator separator )
	{
		super( NodePath::new, separator.getSeparator() );
	}

	private NodePath( NodePath from, String[] nodes )
	{
		this( nodes );
		isAbsolute = from.isAbsolute;
	}

	public NodePath( String[] nodes )
	{
		this( nodes, DEFAULT_SEPARATOR );
	}

	public NodePath( Collection<String> nodes )
	{
		this( nodes, DEFAULT_SEPARATOR );
	}

	public NodePath()
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

	public Namespace toNamespace()
	{
		return new Namespace( getNodes(), glue );
	}

	public enum Separator
	{
		FORWARDSLASH( "/" ),
		BACKSLASH( "\\" ),
		UNDERSCORE( "_" ),;

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
