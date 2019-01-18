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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.Singular;
import io.amelia.events.Events;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ConfigException;
import io.amelia.looper.LooperTask;
import io.amelia.permissions.backend.file.FileBackend;
import io.amelia.permissions.backend.memory.MemoryBackend;
import io.amelia.permissions.backend.sql.SQLBackend;
import io.amelia.permissions.event.PermissibleEvent;
import io.amelia.permissions.event.PermissibleSystemEvent;
import io.amelia.permissions.lang.PermissionBackendException;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryBoolean;

@Singular
public abstract class Permissions
{
	public static final Kernel.Logger L = Kernel.getLogger( Permissions.class );

	protected boolean allowOps = true;
	protected boolean hasWhitelist = false;
	protected boolean isDebugEnabled = false;

	public Permissions()
	{
		addBackend( new MemoryBackend( this, false ) );
		addBackend( new SQLBackend( this, false ) );
		addBackend( new FileBackend( this, true ) );
	}

	protected abstract void addBackend( PermissionBackend backend );

	public boolean allowOps()
	{
		return allowOps;
	}

	protected void callEvent( PermissibleSystemEvent.Action action )
	{
		callEvent( new PermissibleSystemEvent( action ) );
	}

	protected void callEvent( PermissibleEvent event )
	{
		Events.getInstance().callEvent( event );
	}

	public abstract PermissibleState checkPermissibleState( UUID uuid );

	public abstract PermissionResult checkPermission( UUID uuid, PermissionNamespace permission, String... refs );

	public abstract PermissionResult checkPermission( Permissible entity, PermissionNamespace namespace );

	public abstract Permission createNode( String namespace );

	public abstract Permission createNode( String namespace, VoluntaryBoolean valueDefault );

	public abstract Permission createNode( PermissionNamespace namespace );

	public abstract Permission createNode( PermissionNamespace namespace, VoluntaryBoolean valueDefault );

	public abstract void end();

	public abstract PermissionBackend getBackend();

	public abstract Stream<PermissionBackend> getBackends();

	public abstract PermissibleGroup getDefaultGroup();

	public abstract PermissibleGroup getDefaultGroup( References refs );

	protected abstract PermissibleGroup getDefaultGroup( References refs, PermissibleGroup fallback );

	public abstract Stream<PermissibleEntity> getEntities();

	public abstract Stream<PermissibleEntity> getEntitiesWithPermission( Permission perm );

	public abstract Stream<PermissibleEntity> getEntitiesWithPermission( @Nonnull PermissionNamespace namespace );

	public abstract PermissibleGroup getGroup( @Nonnull UUID uuid );

	public abstract PermissibleGroup getGroup( @Nonnull UUID uuid, boolean create );

	public abstract Stream<PermissibleGroup> getGroups();

	public abstract Stream<PermissibleGroup> getGroups( String query );

	public abstract RegExpMatcher getMatcher();

	public abstract Voluntary<Permission> getNode( PermissionNamespace namespace );

	protected abstract Voluntary<Permission> getNodeByLocalName( String name );

	public abstract Stream<Permission> getNodes( PermissionNamespace ns );

	public abstract Stream<Permission> getNodes( String ns );

	public abstract PermissibleEntity getPermissibleEntity( @Nonnull UUID uuid );

	public abstract PermissibleEntity getPermissibleEntity( @Nonnull UUID uuid, boolean create );

	public abstract Voluntary<Permission> getPermission( PermissionNamespace namespace );

	public abstract Voluntary<Permission> getPermission( String path );

	public abstract Set<String> getRefInheritance( String ref );

	public abstract Set<String> getReferences();

	protected abstract Permission getRootNode( String name );

	public abstract Stream<Permission> getRootNodes();

	public abstract Stream<Permission> getRootNodes( boolean ignoreSysNode );

	public boolean hasWhitelist()
	{
		return hasWhitelist;
	}

	public boolean isDebugEnabled()
	{
		return isDebugEnabled;
	}

	public abstract void loadData() throws PermissionBackendException;

	public abstract boolean refactorNamespace( String newNamespace, boolean appendLocalName );

	protected abstract void registerTask( LooperTask task, int delay );

	public abstract void reload() throws PermissionBackendException;

	public abstract void reset() throws PermissionBackendException;

	public abstract void resetEntity( Permissible entity );

	public abstract void resetGroup( String groupName );

	public abstract void saveData();

	public void setAllowOp( boolean allowOp ) throws ConfigException.Error
	{
		callEvent( PermissibleSystemEvent.Action.ALLOWOP_TOGGLE );
		this.allowOps = allowOp;
	}

	public abstract void setBackend( String backendName ) throws PermissionBackendException;

	public void setDebugEnabled( boolean debugEnabled ) throws ConfigException.Error
	{
		callEvent( PermissibleSystemEvent.Action.DEBUGMODE_TOGGLE );
		this.isDebugEnabled = debugEnabled;
	}

	public abstract void setDefaultGroup( PermissibleGroup group, References refs );

	public abstract void setDefaultGroup( PermissibleGroup group );

	public void setHasWhitelist( boolean hasWhitelist ) throws ConfigException.Error
	{
		callEvent( PermissibleSystemEvent.Action.WHITELIST_TOGGLE );
		this.hasWhitelist = hasWhitelist;
	}

	public abstract void setRefInheritance( String ref, Collection<String> heir );
}
