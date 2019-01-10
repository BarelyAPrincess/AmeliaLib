/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.amelia.foundation.Foundation;
import io.amelia.support.NodeStack;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;

/**
 * Extends the base {@link NodeStack} and adds permission specific methods
 */
public class PermissionNamespace extends NodeStack<PermissionNamespace>
{
	public static PermissionNamespace empty( String separator )
	{
		return new PermissionNamespace( new String[0], separator );
	}

	public static PermissionNamespace empty()
	{
		return new PermissionNamespace( new String[0], "." );
	}

	public static PermissionNamespace of( String namespace )
	{
		return of( namespace, null );
	}

	public static PermissionNamespace of( String namespace, String glue )
	{
		namespace = Objs.notNullOrDef( namespace, "" ).toLowerCase();

		if ( Objs.isEmpty( namespace ) || "-1".equals( namespace ) || "everybody".equals( namespace ) || "everyone".equals( namespace ) )
			return PermissionDefault.EVERYBODY.getPermissionNamespace();
		if ( "0".equals( namespace ) || "op".equals( namespace ) || "root".equals( namespace ) )
			return PermissionDefault.OP.getPermissionNamespace();
		if ( "admin".equals( namespace ) )
			return PermissionDefault.ADMIN.getPermissionNamespace();

		glue = Objs.notEmptyOrDef( glue, "." );
		return new PermissionNamespace( Strs.split( namespace, Pattern.compile( glue, Pattern.LITERAL ) ).collect( Collectors.toList() ), glue );
	}

	public static PermissionNamespace ofRegex( String namespace, String regex )
	{
		namespace = Objs.notNullOrDef( namespace, "" );
		regex = Objs.notEmptyOrDef( regex, "\\." );
		return new PermissionNamespace( Strs.split( namespace, Pattern.compile( regex ) ).collect( Collectors.toList() ) );
	}

	public static PermissionNamespace transform( NodeStack ns )
	{
		return new PermissionNamespace( ns.getNames() );
	}

	public PermissionNamespace( String[] nodes, String glue )
	{
		super( PermissionNamespace::new, glue, nodes );
	}

	public PermissionNamespace( Collection<String> nodes, String glue )
	{
		super( PermissionNamespace::new, glue, nodes );
	}

	public PermissionNamespace( String glue )
	{
		super( PermissionNamespace::new, glue );
	}

	private PermissionNamespace( PermissionNamespace from, String[] nodes )
	{
		this( nodes );
	}

	public PermissionNamespace( String[] nodes )
	{
		this( nodes, "." );
	}

	public PermissionNamespace( Collection<String> nodes )
	{
		this( nodes, "." );
	}

	public PermissionNamespace()
	{
		this( "." );
	}

	@Override
	protected PermissionNamespace create( String[] nodes )
	{
		return new PermissionNamespace( nodes );
	}

	public Permission createPermission()
	{
		return Foundation.getPermissions().createNode( getString() );
	}

	public Permission createPermission( VoluntaryBoolean valueDefault )
	{
		return Foundation.getPermissions().createNode( getString(), valueDefault );
	}

	public boolean equals( Permission perm )
	{
		return equals( perm.getNamespace() );
	}

	public Voluntary<Permission> getPermission()
	{
		return Foundation.getPermissions().getNode( this );
	}

	/**
	 * Attempts to regex match each node of the namespaces.
	 * XXX Make sure a regex namespace can be created for this to work.
	 */
	public boolean matches( PermissionNamespace ns )
	{
		if ( getNodeCount() != ns.getNodeCount() )
			return false;

		for ( int i = 0; i < nodes.length; i++ )
			if ( !nodes[i].matches( ns.nodes[i] ) )
				return false;
		return true;
	}
}
