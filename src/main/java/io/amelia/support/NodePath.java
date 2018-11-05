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

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.lang.ApplicationException;

public class NodePath extends NamespaceBase<NodePath>
{
	public static NodePath empty()
	{
		return empty( Separator.FORWARDSLASH );
	}

	public static NodePath empty( Separator separator )
	{
		return new NodePath( new String[0], separator.getSeparator() );
	}

	public static NodePath parseString( String path )
	{
		return parseString( path, Separator.FORWARDSLASH );
	}

	public static NodePath parseString( @Nullable String path, @Nonnull Separator separator )
	{
		path = Objs.notNullOrDef( path, "" );
		return new NodePath( Strs.split( path, Pattern.compile( separator.getSeparator(), Pattern.LITERAL ) ).collect( Collectors.toList() ), separator.getSeparator() );
	}

	public NodePath( String[] nodes, String glue )
	{
		super( NodePath::new, glue, nodes );
	}

	public NodePath( List<String> nodes, String glue )
	{
		super( NodePath::new, glue, nodes );
	}

	public NodePath( String glue )
	{
		super( NodePath::new, glue );
	}

	public NodePath( String[] nodes )
	{
		super( NodePath::new, ".", nodes );
	}

	public NodePath( List<String> nodes )
	{
		super( NodePath::new, ".", nodes );
	}

	public NodePath()
	{
		super( NodePath::new, "." );
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
