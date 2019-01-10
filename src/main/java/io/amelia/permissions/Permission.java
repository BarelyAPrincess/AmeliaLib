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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.ContainerWithValue;
import io.amelia.foundation.Foundation;
import io.amelia.permissions.lang.PermissionException;
import io.amelia.support.EnumColor;
import io.amelia.support.NodeStack;
import io.amelia.support.Objs;
import io.amelia.support.Strs;

/**
 * Permission class for each permission node
 */
public final class Permission extends ContainerWithValue<Permission, PermissionMeta, PermissionException> implements Comparable<Permission>
{
	@Nonnull
	public static Permission empty()
	{
		try
		{
			return new Permission( null, "" );
		}
		catch ( PermissionException e )
		{
			// This should never happen!
			throw new RuntimeException( e );
		}
	}

	public static Permission of( @Nonnull String namespace )
	{
		return of( PermissionNamespace.of( Objs.notNullOrDef( namespace, "" ) ) );
	}

	public static Permission of( @Nonnull NodeStack namespace )
	{
		Permission current = empty();
		for ( String child : namespace.getNames() )
			current = current.getChildOrCreate( child );
		return current;
	}

	private Permission() throws PermissionException
	{
		super( Permission::new, "" );
	}

	private Permission( @Nonnull String key ) throws PermissionException
	{
		super( Permission::new, key );
	}

	private Permission( @Nullable Permission parent, @Nonnull String key ) throws PermissionException
	{
		super( Permission::new, parent, key );
	}

	private Permission( @Nullable Permission parent, @Nonnull String key, @Nullable PermissionMeta value ) throws PermissionException
	{
		super( Permission::new, parent, key, value );
	}

	public void commitToBackend()
	{
		Foundation.getPermissions().getBackend().nodeCommit( this );
	}

	@Override
	public int compareTo( Permission perm )
	{
		if ( getNamespace().equals( perm.getNamespace() ) )
			return 0;

		PermissionNamespace ns1 = getPermissionNamespace();
		PermissionNamespace ns2 = perm.getPermissionNamespace();

		int ln = Math.min( ns1.getNodeCount(), ns2.getNodeCount() );

		for ( int i = 0; i < ln; i++ )
			if ( !ns1.getNode( i ).equals( ns2.getNode( i ) ) )
				return ns1.getNode( i ).compareTo( ns2.getNode( i ) );

		return ns1.getNodeCount() > ns2.getNodeCount() ? -1 : 1;
	}

	public String dumpPermissionStack()
	{
		return dumpPermissionStack0( 0 );
	}

	private String dumpPermissionStack0( int depth )
	{
		StringBuilder output = new StringBuilder();

		String spacing = depth > 0 ? Strs.repeat( "      ", depth - 1 ) + "|---> " : "";

		output.append( String.format( "%s%s%s=%s", EnumColor.YELLOW, spacing, getName(), getPermissionMeta() ) );
		getChildren().forEach( p -> output.append( p.dumpPermissionStack0( depth + 1 ) ) );

		return output.toString();
	}

	@Override
	protected PermissionException getException( @Nonnull String message, @Nullable Exception exception )
	{
		return null;
	}

	public PermissionMeta getPermissionMeta()
	{
		return getValue( () -> new PermissionMeta( getPermissionNamespace() ) ).get();
	}

	public PermissionNamespace getPermissionNamespace()
	{
		return PermissionNamespace.transform( super.getNamespace() );
	}

	@Override
	public String toString()
	{
		return String.format( "Permission{name=%s,parent=%s,getUserContext=%s}", getName(), getParent(), getPermissionMeta() );
	}
}
