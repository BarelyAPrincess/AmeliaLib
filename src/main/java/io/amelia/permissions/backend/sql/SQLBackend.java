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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import io.amelia.database.Database;
import io.amelia.database.DatabaseManager;
import io.amelia.database.elegant.ElegantQuerySelect;
import io.amelia.lang.DatabaseException;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.permissions.PermissibleGroup;
import io.amelia.permissions.Permission;
import io.amelia.permissions.PermissionBackend;
import io.amelia.permissions.PermissionDefault;
import io.amelia.permissions.PermissionMeta;
import io.amelia.permissions.PermissionNamespace;
import io.amelia.permissions.Permissions;
import io.amelia.permissions.References;
import io.amelia.permissions.lang.PermissionBackendException;
import io.amelia.permissions.lang.PermissionValueException;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.VoluntaryBoolean;

/**
 * Provides the SQL Permission Backend
 */
public class SQLBackend extends PermissionBackend
{
	public SQLBackend( Permissions permissions, boolean isDefault )
	{
		super( permissions, "sql", isDefault );
	}

	@Override
	public void commit()
	{
		// Nothing to do here!
	}

	@Override
	public PermissibleGroup getDefaultGroup( References refs )
	{
		try
		{
			Map<References, String> defaults = new HashMap<>();

			ElegantQuerySelect result = getSQL().table( "permissions_groups" ).select().where( "parent" ).matches( "default" ).and().where( "type" ).matches( 1 ).executeWithException();
			// ResultSet result = getSQL().query( "SELECT * FROM `permissions_groups` WHERE `parent` = 'default' AND `type` = '1';" );

			if ( result.count() < 1 )
				throw new RuntimeException( "There is no default group set. New entities will not have any groups." );

			do
			{
				References ref = References.format( result.getString( "ref" ) );
				if ( ref.isEmpty() )
					defaults.put( References.format( "" ), result.getString( "child" ) );
				else
					defaults.put( ref, result.getString( "child" ) );
			}
			while ( result.next() );

			if ( defaults.isEmpty() )
				throw new RuntimeException( "There is no default group set. New entities will not have any groups." );

			return getGroup( UUID.fromString( refs == null || refs.isEmpty() ? defaults.get( "" ) : defaults.get( refs ) ) );
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public PermissibleEntity getEntity( UUID uuid )
	{
		return new SQLEntity( this, uuid, "" );
	}

	@Override
	public Stream<String> getEntityNames()
	{
		return getEntityNames( 0 );
	}

	@Override
	public Stream<String> getEntityNames( int type )
	{
		try
		{
			ElegantQuerySelect select = getSQL().table( "permissions_entity" ).select().where( "type" ).matches( type ).executeWithException();
			// ResultSet result = getSQL().query( "SELECT * FROM `permissions_entity` WHERE `type` = " + type + ";" );

			return select.set().castMapValue( String.class ).stream().map( row -> row.get( "owner" ) );
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public PermissibleGroup getGroup( UUID uuid )
	{
		return new SQLGroup( this, uuid, "" );
	}

	@Override
	public Stream<String> getGroupNames()
	{
		return getEntityNames( 1 );
	}

	public Database getSQL()
	{
		return DatabaseManager.getDefault().getDatabase();
	}

	@Override
	public void initialize() throws PermissionBackendException
	{
		Database db = getSQL();

		if ( db == null )
			throw new PermissionBackendException( "SQL connection is not configured, see config.yml" );

		Set<String> missingTables = new HashSet<>();

		try
		{
			if ( !db.table( "permissions" ).exists() )
				missingTables.add( "permissions" );

			if ( !db.table( "permissions_entity" ).exists() )
				missingTables.add( "permissions_entity" );

			if ( !db.table( "permissions_groups" ).exists() )
				missingTables.add( "permissions_groups" );
		}
		catch ( DatabaseException e )
		{
			// Ignore
		}

		if ( !missingTables.isEmpty() )
			throw new PermissionBackendException( "SQL connection is configured but your missing tables: " + Strs.join( missingTables, "," ) + ", check the SQL Backend getting started guide for help." );

		// TODO Create these tables.

		Permissions.L.info( "Successfully initialized SQL Backend!" );
	}

	@Override
	public void loadEntities()
	{
		// TODO
	}

	@Override
	public void loadGroups()
	{
		// TODO
	}

	@Override
	public void loadPermissions()
	{
		try
		{
			ElegantQuerySelect result = getSQL().table( "permissions" ).select().executeWithException();
			// ResultSet result = getSQL().query( "SELECT * FROM `permissions`" );

			if ( result.next() )
				do
				{
					PermissionNamespace ns = PermissionNamespace.of( result.getString( "permission" ) );

					if ( !ns.containsOnlyValidChars() )
					{
						Permissions.L.warning( String.format( "The permission '%s' contains invalid characters, namespaces can only contain the characters a-z, 0-9, and _, this will be fixed automatically.", ns ) );
						ns.fixInvalidChars();
						this.updateDBValue( ns, "permission", ns.getString() );
					}

					Permission perm = Permission.of( ns );
					PermissionMeta meta = perm.getPermissionMeta();

					if ( result.getObject( "value" ) != null )
						meta.setValue( VoluntaryBoolean.ofNullable( Boolean.valueOf( result.getString( "value" ) ) ) );

					if ( result.getObject( "default" ) != null )
						meta.setValueDefault( VoluntaryBoolean.ofNullable( Boolean.valueOf( result.getString( "default" ) ) ) );

					meta.setDescription( result.getString( "description" ) );
				}
				while ( result.next() );

			result.close();
		}
		catch ( DatabaseException e )
		{
			/*
			 * TODO Do something if columns don't exist.
			 * Caused by: java.sql.SQLException: Column 'permission' not found.
			 */
			throw new RuntimeException( e );
		}
	}

	@Override
	public void nodeCommit( Permission perm )
	{
		try
		{
			Database db = getSQL();

			PermissionMeta meta = perm.getPermissionMeta();

			ElegantQuerySelect select = db.table( "permissions" ).select().where( "permission" ).matches( perm.getNamespace() ).executeWithException();
			// ResultSet rs = db.query( "SELECT * FROM `permissions` WHERE `permission` = '" + perm.getPermissionNamespace() + "';" );

			if ( select.count() < 1 )
			{
				if ( !PermissionDefault.isDefault( perm ) && ( !perm.hasChildren() || meta.hasDescription() ) )
					db.table( "permissions" ).insert().values( new String[] {"permission", "value", "default", "description"}, new Object[] {perm.getPermissionNamespace(), meta.getValue().getString(), meta.getValueDefault().getString(), meta.getDescription()} ).execute();
				// db.queryUpdate( "INSERT INTO `permissions` (`permission`, `value`, `default`, `description`) VALUES (?, ?, ?);", );
			}
			else
			{
				if ( select.count() > 1 )
					Permissions.L.warning( String.format( "We found more then one permission node with the namespace '%s', please fix this, or you might experience unexpected behavior.", perm.getNamespace() ) );

				if ( db.table( "permissions" ).delete().where( "permission" ).matches( perm.getNamespace() ).limit( 1 ).executeWithException().count() < 0 )
					// !db.delete( "permissions", String.format( "`permission` = '%s'", perm.getPermissionNamespace() ), 1 ) )
					Permissions.L.warning( "The SQLBackend failed to remove the permission node '" + perm.getPermissionNamespace() + "' from the database." );
				else
				{
					PermissionNamespace ns = perm.getPermissionNamespace();

					updateDBValue( ns, "value", meta.getValue().getString() );
					updateDBValue( ns, "default", meta.getValueDefault().getString() );

					if ( meta.hasDescription() )
						updateDBValue( ns, "description", meta.getDescription() );
				}
			}
		}
		catch ( DatabaseException | PermissionValueException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void nodeDestroy( Permission perm )
	{
		Database db = getSQL();
		// db.delete( "permissions", String.format( "`permission` = '%s'", perm.getPermissionNamespace() ) );
		try
		{
			db.table( "permissions" ).delete().where( "permission" ).matches( perm.getNamespace() ).limit( 1 ).executeWithException();
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void nodeReload( Permission perm )
	{
		Database db = getSQL();

		try
		{
			ElegantQuerySelect select = db.table( "permissions" ).select().where( "permission" ).matches( perm.getNamespace() ).executeWithException();
			// ResultSet rs = db.query( "SELECT * FROM `permissions` WHERE `permission` = '" + perm.getPermissionNamespace() + "';" );

			if ( select.count() > 0 )
			{
				// TODO RELOAD!
			}
		}
		catch ( DatabaseException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void reloadBackend() throws PermissionBackendException
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setDefaultGroup( UUID uuid, References ref )
	{
		try
		{
			Map<String, String> defaults = new HashMap<>();
			Set<String> children = new HashSet<>();

			ElegantQuerySelect result = getSQL().table( "permissions_groups" ).select().where( "parent" ).matches( "default" ).and().where( "type" ).matches( "1" ).executeWithException();
			// ResultSet result = getSQL().query( "SELECT * FROM `permissions_groups` WHERE `parent` = 'default' AND `type` = '1';" );

			// throw new RuntimeException( "There is no default group set. New entities will not have any groups." );
			if ( result.next() )
				do
				{
					String refs = result.getString( "ref" );
					if ( refs == null || refs.isEmpty() )
						defaults.put( "", result.getString( "child" ) );
					else
						for ( String r : refs.split( "|" ) )
							defaults.put( r.toLowerCase(), result.getString( "child" ) );
				}
				while ( result.next() );

			// Update defaults
			for ( String s : ref )
				defaults.put( s.toLowerCase(), uuid.toString() );

			// Remove duplicate children
			for ( Entry<String, String> e : defaults.entrySet() )
				if ( !children.contains( e.getKey() ) )
					children.add( e.getKey() );

			// Delete old records
			// getSQL().queryUpdate( "DELETE FROM `permissions_groups` WHERE `parent` = 'default' AND `type` = '1';" );
			getSQL().table( "permissions_groups" ).delete().where( "parent" ).matches( "default" ).and().where( "type" ).matches( "1" ).execute();

			// Save changes
			for ( String c : children )
			{
				String refs = "";
				for ( Entry<String, String> e : defaults.entrySet() )
					if ( e.getKey() == c )
						refs += "|" + e.getValue();

				if ( refs.length() > 0 )
					refs = refs.substring( 1 );

				getSQL().table( "permissions_group" ).insert().values( new String[] {"child", "parent", "type", "ref"}, new String[] {c, "default", "1", refs} ).execute();
				// getSQL().queryUpdate( "INSERT INTO `permissions_group` (`child`, `parent`, `type`, `ref`) VALUES ('" + c + "', 'default', '1', '" + refs + "');" );
			}
		}
		catch ( DatabaseException e )
		{
			throw new RuntimeException( e );
		}
	}

	private int updateDBValue( PermissionNamespace ns, String key, Object val ) throws DatabaseException, PermissionValueException
	{
		try
		{
			return updateDBValue( ns, key, Objs.castToStringWithException( val ) );
		}
		catch ( ClassCastException e )
		{
			throw new PermissionValueException( "We could not cast the Object %s for key %s.", val.getClass().getName(), key );
		}
	}

	private int updateDBValue( PermissionNamespace ns, String key, String val ) throws DatabaseException
	{
		Database db = getSQL();

		if ( key == null )
			return 0;

		if ( val == null )
			val = "";

		return db.table( "permissions" ).update().value( key, val ).where( "permission" ).matches( ns.getString() ).executeWithException().count();
		// return db.queryUpdate( "UPDATE `permissions` SET `" + key + "` = ? WHERE `permission` = ?;", val, ns.getPermissionNamespace() );
	}
}
