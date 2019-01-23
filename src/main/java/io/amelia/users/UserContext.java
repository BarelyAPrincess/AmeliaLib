/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.users;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.KeyValueGetterTrait;
import io.amelia.data.KeyValueSetterTrait;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.data.TypeBase;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.foundation.EntitySubject;
import io.amelia.foundation.Foundation;
import io.amelia.lang.ParcelException;
import io.amelia.lang.ParcelableException;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.support.Namespace;
import io.amelia.support.Parcels;
import io.amelia.support.Streams;
import io.amelia.support.Voluntary;
import io.amelia.support.WeakReferenceList;

/**
 * Provides the starting point for all users and synchronizes them with their specified creator.
 * We aim for memory usage and references to be kept at a minimum.
 *
 * UserCreator (The Backend) -> UserContext (The User Details) -> UserMeta (The User Processed) -> UserInstance (The User Logged In and can have multiple entities)
 */
public class UserContext implements EntitySubject, Comparable<UserContext>, KeyValueTypesTrait<ParcelableException.Error>, KeyValueSetterTrait<Object, ParcelableException.Error>, KeyValueGetterTrait<Object, ParcelableException.Error>, ParcelReceiver
{
	private final WeakReferenceList<UserEntity> entities = new WeakReferenceList<>();
	private final boolean isUnloadable;
	private final Parcel parcel;
	private final UserCreator userCreator;
	private final UUID uuid;

	/**
	 * Weak references the {@link PermissibleEntity} over at the Permission Manager.<br>
	 * We use the {@link WeakReference} so it can be garbage collected when unused,<br>
	 * we reload it from the Permission Manager once needed again.
	 */
	private WeakReference<PermissibleEntity> permissibleEntity = null;
	private boolean unloaded = false;

	public UserContext( UserOffline userOffline )
	{
		this.userCreator = userOffline.getUserCreator();
		this.uuid = userOffline.uuid();
		this.isUnloadable = true;
		this.parcel = userOffline.parcel;
	}

	public UserContext( UserCreator userCreator, UUID uuid, boolean isUnloadable )
	{
		this.userCreator = userCreator;
		this.uuid = uuid;
		this.isUnloadable = isUnloadable;
		this.parcel = Parcel.empty();
	}

	@Override
	public int compareTo( @Nonnull UserContext other )
	{
		return uuid().compareTo( other.uuid() );
	}

	@Override
	public UserContext getContext()
	{
		return this;
	}

	public UserCreator getCreator()
	{
		return userCreator;
	}

	public Stream<UserEntity> getEntities()
	{
		return entities.stream();
	}

	@Override
	public UserEntity getEntity()
	{
		UserEntity instance = new UserEntity( this );
		entities.add( instance );
		return instance;
	}

	@Override
	public Set<Namespace> getKeys()
	{
		return parcel.getKeys();
	}

	@Override
	public PermissibleEntity getPermissibleEntity()
	{
		if ( permissibleEntity == null || permissibleEntity.get() == null )
			permissibleEntity = new WeakReference<>( Foundation.getPermissions().getPermissibleEntity( uuid ) );

		return permissibleEntity.get();
	}

	@Override
	public Voluntary<Object> getValue( @Nonnull Namespace key )
	{
		return parcel.getValue( key );
	}

	@Override
	public Voluntary<Object> getValue()
	{
		return parcel.getValue();
	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{
		Streams.forEachWithException( getEntities(), userEntity -> userEntity.handleParcel( parcelCarrier ) );
	}

	@Override
	public boolean hasValue( Namespace key )
	{
		return parcel.hasValue( key );
	}

	public boolean isUnloadable()
	{
		return isUnloadable;
	}

	@Override
	public String name()
	{
		return Parcels.parseFormatString( parcel, Foundation.getUsers().getDisplayNameFormat() ).orElse( "{error}" );
	}

	public void save() throws UserException.Error
	{
		userCreator.save( this );
	}

	@Override
	public void setValue( Namespace key, Object value ) throws ParcelableException.Error
	{
		parcel.setValue( key, value );
	}

	@Override
	public void setValue( TypeBase type, Object value ) throws ParcelableException.Error
	{
		parcel.setValue( type, value );
	}

	@Override
	public void setValueIfAbsent( Namespace key, Supplier<?> value ) throws ParcelableException.Error
	{
		parcel.setValueIfAbsent( key, value );
	}

	@Override
	public void setValueIfAbsent( TypeBase.TypeWithDefault type ) throws ParcelableException.Error
	{
		parcel.setValue( type );
	}

	public void unload() throws UserException.Error
	{
		if ( !isUnloadable )
			throw new UserException.Error( this, uuid() + " can't be unloaded." );
		Foundation.getUsers().removeUserContext( this );
		unloaded = true;
	}

	@Nonnull
	@Override
	public UUID uuid()
	{
		return uuid;
	}

	public void validate() throws UserException.Error
	{
		if ( unloaded )
			throw new UserException.Error( this, uuid() + " has already been unloaded!" );
	}
}
