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

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.amelia.foundation.ConfigRegistry;

public class Namespace extends NodeStack<Namespace> implements Comparable<Namespace>
{
	public static Namespace empty( String separator )
	{
		return new Namespace( new String[0], separator );
	}

	public static Namespace empty()
	{
		return new Namespace( new String[0], "." );
	}

	public static boolean isTld( String domain )
	{
		domain = Http.hostnameNormalize( domain );
		for ( String tld : ConfigRegistry.config.getStringList( ConfigRegistry.ConfigKeys.TLDS ) )
			if ( domain.matches( tld ) )
				return true;
		return false;
	}

	public static Namespace of( String namespace )
	{
		return of( namespace, null );
	}

	public static Namespace of( String namespace, String glue )
	{
		namespace = Objs.notNullOrDef( namespace, "" );
		glue = Objs.notEmptyOrDef( glue, "." );
		return new Namespace( Strs.split( namespace, Pattern.compile( glue, Pattern.LITERAL ) ).collect( Collectors.toList() ), glue );
	}

	public static Namespace of( String[] nodes, String glue )
	{
		return new Namespace( nodes, glue );
	}

	public static Namespace of( String[] nodes )
	{
		return new Namespace( nodes );
	}

	public static Namespace of( Collection<String> nodes, String glue )
	{
		return new Namespace( nodes, glue );
	}

	public static Namespace of( Collection<String> nodes )
	{
		return new Namespace( nodes );
	}

	public static Namespace ofRegex( String namespace, String regex )
	{
		namespace = Objs.notNullOrDef( namespace, "" );
		regex = Objs.notEmptyOrDef( regex, "\\." );
		return new Namespace( Strs.split( namespace, Pattern.compile( regex ) ).collect( Collectors.toList() ) );
	}

	public static Domain parseDomain( String namespace )
	{
		namespace = Http.hostnameNormalize( namespace );

		if ( Objs.isEmpty( namespace ) )
			return new Domain( new Namespace(), new Namespace() );

		Namespace ns = Namespace.of( namespace );
		int parentNodePos = -1;

		for ( int n = 0; n < ns.getNodeCount(); n++ )
		{
			String sns = ns.getSubNodes( n ).getString();
			if ( isTld( sns ) )
			{
				parentNodePos = n;
				break;
			}
		}

		return parentNodePos > 0 ? new Domain( ns.getSubNodes( parentNodePos ), ns.getSubNodes( 0, parentNodePos ) ) : new Domain( new Namespace(), ns );
	}

	private Namespace( String[] nodes, String glue )
	{
		super( Namespace::new, glue, nodes );
	}

	private Namespace( Collection<String> nodes, String glue )
	{
		super( Namespace::new, glue, nodes );
	}

	private Namespace( String glue )
	{
		super( Namespace::new, glue );
	}

	private Namespace( Namespace from, String[] nodes )
	{
		this( nodes );
	}

	private Namespace( String[] nodes )
	{
		this( nodes, "." );
	}

	private Namespace( Collection<String> nodes )
	{
		this( nodes, "." );
	}

	private Namespace()
	{
		this( "." );
	}

	public String getGlue()
	{
		return glue;
	}

	public Namespace setGlue( String glue )
	{
		this.glue = glue;
		return this;
	}

	public static class Domain
	{
		private final Namespace child;
		private final Namespace tld;

		private Domain( Namespace tld, Namespace child )
		{
			this.tld = tld;
			this.child = child;
		}

		public Namespace getChild()
		{
			return child;
		}

		public Namespace getChildDomain()
		{
			return child.getNodeCount() <= 1 ? new Namespace() : child.getSubNodes( 1 );
		}

		public Namespace getFullDomain()
		{
			return child.merge( tld );
		}

		public Namespace getRootDomain()
		{
			return Namespace.of( child.getStringLast() + "." + tld.getString() );
		}

		public Namespace getTld()
		{
			return tld;
		}
	}
}
