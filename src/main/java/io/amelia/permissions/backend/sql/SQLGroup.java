/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permissions.backend.sql;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Stream;

import io.amelia.database.Database;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.foundation.Foundation;
import io.amelia.lang.DatabaseException;
import io.amelia.permissions.ChildPermission;
import io.amelia.permissions.PermissibleGroup;
import io.amelia.permissions.Permission;
import io.amelia.permissions.PermissionNamespace;
import io.amelia.permissions.Permissions;
import io.amelia.permissions.References;
import io.amelia.support.EnumColor;
import io.amelia.support.Streams;
import io.amelia.support.VoluntaryBoolean;

public class SQLGroup extends PermissibleGroup
{
	private final SQLBackend backend;

	public SQLGroup( SQLBackend backend, UUID uuid, String name )
	{
		super( uuid, name );
		this.backend = backend;
	}

	@Override
	public void reloadGroups()
	{
		Database db = backend.getSQL();

		clearGroups();
		try
		{
			ElegantQuerySelect rs = db.table( "permissions_groups" ).select().where( "parent" ).matches( uuid().toString() ).and().where( "type" ).matches( "1" ).executeWithException();
			// ResultSet rs = db.query( "SELECT * FROM `permissions_groups` WHERE `parent` = '" + uuid() + "' AND `type` = '1';" );

			if ( rs.count() > 0 )
				do
				{
					PermissibleGroup grp = Foundation.getPermissions().getGroup( UUID.fromString( rs.getString( "child" ) ) );
					addGroup( grp, References.format( rs.getString( "refs" ) ) );
				}
				while ( rs.next() );

			rs.close();
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void reloadPermissions()
	{
		Database db = backend.getSQL();

		clearPermissions();
		clearTimedPermissions();
		try
		{
			ElegantQuerySelect rs = db.table( "permissions_entity" ).select().where( "owner" ).matches( getStringId() ).and().where( "type" ).matches( "1" ).executeWithException();
			// ResultSet rs = db.query( "SELECT * FROM `permissions_entity` WHERE `owner` = ? AND `type` = '1';", uuid() );

			if ( rs.count() > 0 )
				do
				{
					PermissionNamespace ns = PermissionNamespace.of( rs.getString( "permission" ) );

					if ( !ns.containsOnlyValidChars() )
					{
						Permissions.L.warning( "We failed to add the permission %s to entity %s because it contained invalid characters, namespaces can only contain 0-9, a-z and _." );
						continue;
					}

					Stream<Permission> perms = ns.containsRegex() ? Foundation.getPermissions().getNodes( ns ) : Stream.of( ns.createPermission() );
					perms.forEach( perm -> addPermission( new ChildPermission( this, perm, VoluntaryBoolean.ofNullable( Boolean.valueOf( rs.getString( "value" ) ) ), getWeight() ), References.format( rs.getString( "refs" ) ) ) );
				}
				while ( rs.next() );

			rs.close();
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void remove()
	{
		Database db = backend.getSQL();
		try
		{
			// db.queryUpdate( String.format( "DELETE FROM `permissions_entity` WHERE `owner` = '%s' AND `type` = '1';", uuid() ) );
			// db.queryUpdate( String.format( "DELETE FROM `permissions_groups` WHERE `parent` = '%s' AND `type` = '1';", uuid() ) );

			db.table( "permissions_entity" ).delete().where( "owner" ).matches( getStringId() ).and().where( "type" ).matches( "1" ).executeWithException();
			db.table( "permissions_groups" ).delete().where( "parent" ).matches( getStringId() ).and().where( "type" ).matches( "1" ).executeWithException();
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void save()
	{
		if ( isVirtual() )
			return;

		if ( isDebug() )
			Permissions.L.info( EnumColor.YELLOW + "Group " + getStringId() + " saved to backend" );

		try
		{
			Database db = backend.getSQL();
			remove();

			Streams.forEachWithException( getChildPermissions( null ), child -> {
				Permission perm = child.getPermission();
				// db.queryUpdate( String.format( "INSERT INTO `permissions_entity` (`owner`,`type`,`refs`,`permission`,`value`) VALUES ('%s','1','%s','%s','%s');", uuid(), child.getReferences().join(), perm.getPermissionNamespace(),
				// child.getObject() ) );
				db.table( "permissions_entity" ).insert().value( "owner", uuid() ).value( "type", 1 ).value( "refs", child.getReferences().map( References::join ).orElse( "" ) ).value( "permission", perm.getNamespace() ).value( "value", child.getValue().getString() ).executeWithException();
			} );

			Map<PermissibleGroup, References> groups = getGroupEntries( null );
			for ( Entry<PermissibleGroup, References> entry : groups.entrySet() )
				db.table( "permissions_groups" ).insert().value( "child", entry.getKey().uuid().toString() ).value( "parent", uuid().toString() ).value( "type", 1 ).value( "refs", entry.getValue().join() ).executeWithException();
			// db.queryUpdate( String.format( "INSERT INTO `permissions_groups` (`child`, `parent`, `type`, `refs`) VALUES ('%s', '%s', '1', '%s');", entry.getKey().uuid(), uuid(), entry.getValue().join() ) );
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}
}
