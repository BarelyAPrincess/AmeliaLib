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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.ContainerBase;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Foundation;
import io.amelia.lang.ParcelableException;
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
import io.amelia.support.IO;
import io.amelia.support.Streams;

/**
 * Provides the File Permission Backend
 */
public class FileBackend extends PermissionBackend
{
	// TODO Temporary
	public static final UUID DEFUALT_UUID = UUID.randomUUID();

	public Parcel parcel;
	public Path permissionsFile;

	public FileBackend( Permissions permissions, boolean isDefault )
	{
		super( permissions, "file", isDefault );
	}

	@Override
	public void commit()
	{
		if ( permissionsFile != null )
		{
			try
			{
				IO.writeStringToPath( ParcelLoader.encodeYaml( parcel ), permissionsFile );
			}
			catch ( IOException e )
			{
				Permissions.L.severe( "Error saving permissions file to " + permissionsFile.toString(), e );
			}
		}
	}

	@Override
	public PermissibleGroup getDefaultGroup( References refs )
	{
		Parcel groups = parcel.getChild( "groups" );

		if ( groups == null )
			throw new RuntimeException( "No groups defined. Check your permissions file." );

		for ( String ref : refs )
			groups.getChildren().filter( section -> section.getBoolean( "refs." + ref + ".default" ).orElse( false ) ).map( section -> Foundation.getPermissions().getGroup( UUID.fromString( section.getString().orElse( null ) ) ) ).findFirst();

		if ( refs.isEmpty() )
			throw new RuntimeException( "Default user group is not defined. Please select one using the \"default: true\" property" );

		return null;
	}

	@Override
	public PermissibleEntity getEntity( UUID uuid )
	{
		return new FileEntity( this, uuid, "" );
	}

	@Override
	public Stream<String> getEntityNames()
	{
		return getEntityNames( 0 );
	}

	@Override
	public Stream<String> getEntityNames( int type )
	{
		Parcel section = parcel.getChild( type == 1 ? "groups" : "entities" );
		if ( section == null )
			return Stream.empty();
		return section.getChildren().map( ContainerBase::getName );
	}

	@Override
	public PermissibleGroup getGroup( UUID uuid )
	{
		return new FileGroup( this, uuid, "" );
	}

	@Override
	public Stream<String> getGroupNames()
	{
		return getEntityNames( 1 );
	}

	public Parcel getParcel()
	{
		return parcel;
	}

	/**
	 * This method is called when the permissions config file does not exist
	 * and needs to be created, this also adds the defaults.
	 */
	private void initNewConfiguration() throws PermissionBackendException
	{
		if ( Files.notExists( permissionsFile ) )
			try
			{
				Files.createFile( permissionsFile );

				setDefaultGroup( DEFUALT_UUID, References.format( "" ) );

				List<String> defaultPermissions = new LinkedList<>();
				defaultPermissions.add( "io.amelia.*" );

				parcel.setValue( "groups.default.permissions", defaultPermissions );

				commit();
			}
			catch ( IOException | ParcelableException.Error e )
			{
				throw new PermissionBackendException( e );
			}
	}

	@Override
	public void initialize() throws PermissionBackendException
	{
		try
		{
			permissionsFile = ConfigRegistry.config.getStringAsPath( "permissions.file" ).orElse( Paths.get( "permissions.yaml" ) );
			parcel = ParcelLoader.decodeYaml( permissionsFile );
			Permissions.L.info( "Permissions file successfully loaded." );
		}
		catch ( FileNotFoundException e )
		{
			if ( parcel == null )
			{
				parcel = Parcel.empty();
				initNewConfiguration();
			}
		}
		catch ( IOException | ParcelableException.Error e )
		{
			throw new PermissionBackendException( "Error loading permissions file!", e );
		}
	}

	@Override
	public void loadEntities() throws PermissionBackendException
	{
		Parcel section = parcel.getChild( "entities" );
		if ( section == null )
			return;

		section.getChildren().forEach( entity -> {
			Foundation.getPermissions().getPermissibleEntity( UUID.fromString( entity.getName() ), true );
		} );
	}

	@Override
	public void loadGroups() throws PermissionBackendException
	{
		Parcel section = parcel.getChild( "groups" );
		if ( section == null )
			return;

		section.getChildren().forEach( group -> {
			Foundation.getPermissions().getGroup( UUID.fromString( group.getName() ), true );
		} );
	}

	@Override
	public void loadPermissions() throws PermissionBackendException
	{
		Parcel section = parcel.getChild( "permissions" );
		if ( section == null )
			return;

		section.getChildren().forEach( node -> {
			PermissionNamespace ns = PermissionNamespace.of( node.getName().replace( "_", "." ) );

			if ( !ns.containsOnlyValidChars() )
			{
				String origNamespace = ns.getString();
				ns.fixInvalidChars();
				node.setName( ns.getString( "_" ) );
				Permissions.L.warning( "The permission '%s' contains invalid characters. The namespaces can only contain the characters a-z, 0-9, and _, this will be fixed automatically. The new name is: %s", origNamespace, ns.getString() );
			}

			Permission perm = Permission.of( ns );
			PermissionMeta meta = perm.getPermissionMeta();
			meta.setValue( node.getBoolean( "value" ) );
			meta.setValueDefault( node.getBoolean( "default" ) );
			meta.setDescription( node.getString( "description" ).orElse( null ) );
		} );
	}

	@Override
	public void nodeCommit( Permission perm )
	{
		if ( PermissionDefault.isDefault( perm ) )
			return;

		if ( perm.hasChildren() && !perm.getPermissionMeta().hasDescription() )
			return;

		Parcel permission = parcel.getChildOrCreate( "permissions." + perm.getPermissionNamespace().getString( "_" ) );

		PermissionMeta meta = perm.getPermissionMeta();

		try
		{
			// permission.setValue( "default", perm.getType() == PermissionType.DEFAULT ? null : model.getValueDefault() );
			permission.setValue( "value", meta.getValue().orElse( null ) );
			permission.setValue( "description", meta.getDescription().orElse( null ) );
		}
		catch ( ParcelableException.Error e )
		{
			e.printStackTrace();
		}

		commit();
	}

	@Override
	public void nodeDestroy( Permission perm )
	{
		try
		{
			parcel.getChildOrCreate( "permissions" ).destroyChild( perm.getPermissionNamespace().getString() );
		}
		catch ( ParcelableException.Error e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void nodeReload( Permission perm )
	{

	}

	@Override
	public void reloadBackend() throws PermissionBackendException
	{
		try
		{
			parcel = ParcelLoader.decodeYaml( permissionsFile );
		}
		catch ( ParcelableException.Error | IOException e )
		{
			throw new PermissionBackendException( e );
		}
	}

	@Override
	public void setDefaultGroup( @Nonnull UUID uuid, @Nullable References references )
	{
		Parcel groups = parcel.getChildOrCreate( "groups" );

		String defaultGroupProperty = references == null ? "default" : "refs" + references.join() + "default";

		final AtomicBoolean success = new AtomicBoolean( false );

		try
		{
			Streams.forEachWithException( groups.getChildren(), groupSection -> {
				groupSection.setValue( defaultGroupProperty, false );

				if ( UUID.fromString( groupSection.getName() ).equals( uuid ) )
				{
					groupSection.setValue( defaultGroupProperty, true );
					success.set( true );
				}
				else
					groupSection.setValue( defaultGroupProperty, null );
			} );
		}
		catch ( ParcelableException.Error error )
		{
			error.printStackTrace();
		}

		if ( !success.get() )
		{
			PermissibleGroup pGroup = Foundation.getPermissions().getGroup( uuid );
			pGroup.setDefault( true );
			pGroup.save();
		}

		commit();
	}
}
