/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.backend.file;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

import io.amelia.data.parcel.Parcel;
import io.amelia.foundation.Foundation;
import io.amelia.lang.ParcelableException;
import io.amelia.permissions.ChildPermission;
import io.amelia.permissions.PermissibleGroup;
import io.amelia.permissions.Permission;
import io.amelia.permissions.PermissionNamespace;
import io.amelia.permissions.Permissions;
import io.amelia.permissions.References;
import io.amelia.permissions.lang.PermissionBackendException;
import io.amelia.support.EnumColor;
import io.amelia.support.Streams;

public class FileGroup extends PermissibleGroup
{
	private final FileBackend backend;

	public FileGroup( FileBackend backend, UUID uuid, String name )
	{
		super( uuid, name );
		this.backend = backend;
	}

	@Override
	public void reloadGroups()
	{
		if ( isDebug() )
			Permissions.L.info( EnumColor.YELLOW + "Groups being loaded for group " + getStringId() );

		clearGroups();
		clearTimedGroups();

		Parcel groups = backend.getParcel().getChildOrCreate( "entities." + uuid() + ".groups" );
		groups.getChildren().forEach( group -> addGroup0( Foundation.getPermissions().getGroup( UUID.fromString( group.getName() ), true ), References.format( groups.getString().orElse( "" ) ) ) );
	}

	@Override
	public void reloadPermissions()
	{
		if ( isDebug() )
			Permissions.L.info( EnumColor.YELLOW + "Permissions being loaded for entity " + getStringId() );

		Parcel permissions = backend.getParcel().getChildOrCreate( "entities." + uuid() + ".permissions" );

		clearPermissions();
		clearTimedPermissions();

		permissions.getChildren().forEach( node -> {
			PermissionNamespace ns = PermissionNamespace.of( node.getName().replace( '_', '.' ) );

			Stream<Permission> perms = ns.containsRegex() ? Foundation.getPermissions().getNodes( ns ) : Stream.of( ns.createPermission() );

			perms.forEach( perm -> {
				addPermission( new ChildPermission( this, perm, node.getBoolean(), getWeight() ), References.format( node.getString( "refs" ).orElse( "" ) ) );
			} );
		} );
	}

	@Override
	public void remove()
	{
		try
		{
			backend.getParcel().getChildOrCreate( "groups" ).destroyChild( uuid().toString() );
		}
		catch ( ParcelableException.Error e )
		{
			throw new PermissionBackendException( e );
		}
	}

	@Override
	public void save()
	{
		try
		{
			if ( isVirtual() )
				return;

			if ( isDebug() )
				Permissions.L.info( EnumColor.YELLOW + "Group " + getStringId() + " being saved to backend" );

			Parcel root = backend.getParcel().getChildOrCreate( "entities." + uuid().toString() );

			Streams.forEachWithException( getChildPermissions( null ), child -> {
				Permission perm = child.getPermission();
				Parcel sub = root.getChildOrCreate( "permissions." + perm.getPermissionNamespace().getString( "_" ) );
				sub.setValue( "value", child.getValue().orElse( null ) );
				sub.setValue( "refs", child.getReferences().map( References::join ).orElse( null ) );
			} );

			for ( Entry<PermissibleGroup, References> entry : getGroupEntries( null ).entrySet() )
				root.setValue( "groups." + entry.getKey().uuid().toString(), entry.getValue().join() );
		}
		catch ( ParcelableException.Error e )
		{
			throw new PermissionBackendException( e );
		}
	}
}
