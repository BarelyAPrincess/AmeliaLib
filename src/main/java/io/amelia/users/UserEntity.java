/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import com.google.common.base.Joiner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.data.parcel.ParcelSender;
import io.amelia.foundation.EntitySubject;
import io.amelia.lang.ParcelException;
import io.amelia.lang.ParcelableException;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.support.VoluntaryWithCause;
import io.amelia.users.auth.UserCredentials;

public class UserEntity implements EntitySubject, ParcelSender, ParcelReceiver
{
	/**
	 * Tracks permissibles that are referencing this account
	 */
	private final Set<UserAttachment> permissibles = Collections.newSetFromMap( new WeakHashMap<>() );
	private final UserContext userContext;
	private UserCredentials lastUsedCredentials = null;

	UserEntity( @Nonnull UserContext userContext )
	{
		this.userContext = userContext;
	}

	int countAttachments()
	{
		return permissibles.size();
	}

	public Collection<UserAttachment> getAttachments()
	{
		return Collections.unmodifiableSet( permissibles );
	}

	@Override
	public UserContext getContext()
	{
		return userContext;
	}

	@Override
	public UserEntity getEntity()
	{
		return this;
	}

	public Set<String> getIpAddresses()
	{
		Set<String> ips = new HashSet<>();
		for ( UserAttachment perm : getAttachments() )
			ips.add( perm.getIpAddress() );
		return ips;
	}

	public UserCredentials getLastUsedCredentials()
	{
		return lastUsedCredentials;
	}

	@Override
	public PermissibleEntity getPermissible()
	{
		return getContext().getPermissible();
	}

	@Nullable
	@Override
	public ParcelReceiver getReplyTo()
	{
		return this;
	}

	/**
	 * Get a string from the Metadata with a default value
	 *
	 * @param key Metadata key.
	 *
	 * @return String
	 */
	public VoluntaryWithCause<String, ParcelableException.Error> getString( String key )
	{
		return userContext.getString( key );
	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{
		// TODO
	}

	@Override
	public String name()
	{
		return userContext.name();
	}

	public void registerAttachment( UserAttachment attachment )
	{
		if ( !permissibles.contains( attachment ) )
			permissibles.add( attachment );
	}

	public void setLastUsedCredentials( UserCredentials lastUsedCredentials )
	{
		this.lastUsedCredentials = lastUsedCredentials;
	}

	@Override
	public String toString()
	{
		return "User{" + userContext.toString() + ",Attachments{" + Joiner.on( "," ).join( getAttachments() ) + "}}";
	}

	public void unregisterAttachment( UserAttachment attachment )
	{
		permissibles.remove( attachment );
	}

	@Nonnull
	@Override
	public UUID uuid()
	{
		return userContext.uuid();
	}
}
