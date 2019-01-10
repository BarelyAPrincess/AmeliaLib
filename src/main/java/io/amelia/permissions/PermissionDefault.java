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

import io.amelia.foundation.Foundation;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;

public enum PermissionDefault
{
	USER( PermissionNamespace.of( "sys.user" ) ),
	ADMIN( PermissionNamespace.of( "sys.admin" ) ),
	BANNED( PermissionNamespace.of( "sys.banned" ) ),
	DEFAULT( PermissionNamespace.of( "default" ) ),
	EVERYBODY( PermissionNamespace.empty() ),
	OP( PermissionNamespace.of( "sys.op" ) ),
	WHITELISTED( PermissionNamespace.of( "sys.whitelisted" ) );

	/**
	 * By calling each Permission node we forces it's creation if non-existent
	 */
	public static void initNodes()
	{
		USER.getNode();
		ADMIN.getNode();
		BANNED.getNode();
		DEFAULT.getNode();
		EVERYBODY.getNode();
		OP.getNode();
		WHITELISTED.getNode();
	}

	public static boolean isDefault( Permission perm )
	{
		for ( PermissionDefault pd : PermissionDefault.values() )
			if ( pd.getPermissionNamespace().equals( perm.getPermissionNamespace() ) )
				return true;

		return false;
	}

	private PermissionNamespace namespace;

	PermissionDefault( PermissionNamespace namespace )
	{
		this.namespace = namespace;
	}

	public String getLocalName()
	{
		return namespace.getLocalName();
	}

	public PermissionNamespace getPermissionNamespace()
	{
		return namespace;
	}

	public Permission getNode()
	{
		Permission result = Foundation.getPermissions().getNode( namespace ).orElse( null );

		if ( result == null )
		{
			result = Foundation.getPermissions().getPermission( getPermissionNamespace() ).orElse( null );

			if ( this == EVERYBODY )
				result.getPermissionMeta().setValueDefault( VoluntaryBoolean.of( true ) );

			switch ( this )
			{
				case DEFAULT:
					result.getPermissionMeta().setDescription( "Used as the default permission node if one does not exist. (DO NOT EDIT!)" );
					break;
				case EVERYBODY:
					result.getPermissionMeta().setDescription( "This node is used for the 'everyone' permission. (DO NOT EDIT!)" );
					break;
				case OP:
					result.getPermissionMeta().setDescription( "Indicates OP entities. (DO NOT EDIT!)" );
					break;
				case USER:
					result.getPermissionMeta().setDescription( "Indicates a general USER entity. (DO NOT EDIT!)" );
					break;
				case ADMIN:
					result.getPermissionMeta().setDescription( "Indicates ADMIN entities. (DO NOT EDIT!)" );
					break;
				case BANNED:
					result.getPermissionMeta().setDescription( "Indicates BANNED entities. (DO NOT EDIT!)" );
					break;
				case WHITELISTED:
					result.getPermissionMeta().setDescription( "Indicates WHITELISTED entities. (DO NOT EDIT!)" );
					break;
			}

			result.commitToBackend();
		}

		return result;
	}

	@Override
	public String toString()
	{
		return name() + " {namespace=" + namespace + "}";
	}
}
