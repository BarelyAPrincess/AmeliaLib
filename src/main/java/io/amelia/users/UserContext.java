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

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.bindings.Bindings;
import io.amelia.data.KeyValueGetterTrait;
import io.amelia.data.KeyValueSetterTrait;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.data.TypeBase;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelCarrier;
import io.amelia.data.parcel.ParcelReceiver;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.ParcelException;
import io.amelia.lang.ParcelableException;
import io.amelia.lang.UserException;
import io.amelia.permission.PermissibleEntity;
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
public class UserContext implements UserSubject, Comparable<UserContext>, KeyValueTypesTrait<ParcelableException.Error>, KeyValueSetterTrait<Object, ParcelableException.Error>, KeyValueGetterTrait<Object, ParcelableException.Error>, ParcelReceiver
{
	private final WeakReferenceList<UserEntity> entities = new WeakReferenceList<>();
	private final boolean isUnloadable;
	private final Parcel parcel;
	private boolean unloaded = false;
	private final UserCreator userCreator;
	private final UUID uuid;

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
	public Set<String> getKeys()
	{
		return parcel.getKeys();
	}

	@Override
	public PermissibleEntity getPermissible()
	{
		return Bindings.resolveClassOrFail( PermissibleEntity.class, uuid );
	}

	@Override
	public Voluntary getValue( String key, Function<Object, Object> computeFunction )
	{
		return parcel.getValue( key, computeFunction );
	}

	@Override
	public Voluntary getValue( String key, Supplier<Object> supplier )
	{
		return parcel.getValue( key, supplier );
	}

	@Override
	public Voluntary getValue( @Nonnull String key )
	{
		return parcel.getValue( key );
	}

	@Override
	public Voluntary getValue()
	{
		return parcel.getValue();
	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{
		Streams.forEachWithException( getEntities(), userEntity -> userEntity.handleParcel( parcelCarrier ) );
	}

	@Override
	public boolean hasValue( String key )
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
		return Parcels.parseFormatString( parcel, ConfigRegistry.config.getValue( DefaultUsers.ConfigKeys.DISPLAY_NAME_FORMAT ) ).orElse( "{error}" );
	}

	public void save() throws UserException.Error
	{
		userCreator.save( this );
	}

	@Override
	public void setValue( String key, Object value ) throws ParcelableException.Error
	{
		parcel.setValue( key, value );
	}

	@Override
	public void setValue( TypeBase type, Object value ) throws ParcelableException.Error
	{
		parcel.setValue( type, value );
	}

	@Override
	public void setValueIfAbsent( TypeBase.TypeWithDefault type ) throws ParcelableException.Error
	{
		parcel.setValue( type );
	}

	@Override
	public void setValueIfAbsent( String key, Object value ) throws ParcelableException.Error
	{
		parcel.setValueIfAbsent( key, value );
	}

	public void unload() throws UserException.Error
	{
		if ( !isUnloadable )
			throw new UserException.Error( this, uuid() + " can't be unloaded." );
		userCreator.getUsers().users.remove( this );
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
