/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.bindings;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.lang.ApplicationException;
import io.amelia.support.Voluntary;

@SuppressWarnings( "unchecked" )
public final class BindingMap extends ContainerWithValue<BindingMap, BindingMap.BaseBinding, BindingException.Error>
{
	WeakReference<WritableBinding> owner = null;

	@Nonnull
	public static BindingMap empty()
	{
		try
		{
			return new BindingMap();
		}
		catch ( BindingException.Error error )
		{
			// This should never happen!
			throw new RuntimeException( error );
		}
	}

	private BindingMap() throws BindingException.Error
	{
		super( BindingMap::new, "" );
	}

	protected BindingMap( String key ) throws BindingException.Error
	{
		super( BindingMap::new, key );
	}

	protected BindingMap( BindingMap parent, String key ) throws BindingException.Error
	{
		super( BindingMap::new, parent, key );
	}

	public Stream<BaseBinding> findValues( Class<?> valueClass )
	{
		return getAllChildren().filter( child -> child.hasValue() && valueClass.isAssignableFrom( child.getValue().get().getObjClass() ) ).map( map -> map.value );
	}

	public <T> Voluntary<T, BindingException.Error> getValue( String key, Class<T> cls )
	{
		return getValue( key ).filter( obj -> cls.isAssignableFrom( obj.getClass() ) ).map( obj -> ( T ) obj );
	}

	public boolean isPrivatized()
	{
		return ( owner != null && owner.get() != null || parent != null && parent.isPrivatized() );
	}

	public void privatize( WritableBinding writableBinding ) throws BindingException.Denied
	{
		if ( isPrivatized() )
			throw new BindingException.Denied( "Namespace \"" + getNamespace().getString() + "\" has already been privatized." );
		// Indented to unprivatize children and notify the potentual owners.
		unprivatize();
		owner = new WeakReference<>( writableBinding );
	}

	public <S> void set( @Nonnull Class<S> objClass, @Nonnull Supplier<S> objSupplier ) throws BindingException.Error
	{
		setValue( new BaseBinding( objClass, objSupplier ) );
	}

	public void set( Object obj ) throws BindingException.Error
	{
		if ( obj == null )
			setValue( null );
		else
			setValue( new BaseBinding( obj ) );
	}

	@Override
	protected BindingException.Error getException( String message )
	{
		// TODO Include node in exception
		return new BindingException.Error( message );
	}

	private void unprivatize()
	{
		getChildren().forEach( BindingMap::unprivatize );
		if ( owner != null && owner.get() != null )
			owner.get().destroy();
	}

	/**
	 * Called from the WritableBinding
	 */
	void unprivatize( WritableBinding writableBinding )
	{
		if ( owner != null && owner.get() != null && owner.get() == writableBinding )
			owner = null;
	}

	class BaseBinding<S>
	{
		final Class<S> objClass;
		S objInstance = null;
		Supplier<S> objSupplier = null;

		BaseBinding( @Nonnull S objInstance )
		{
			this.objClass = ( Class<S> ) objInstance.getClass();
			this.objInstance = objInstance;
		}

		BaseBinding( @Nonnull Class<S> objClass, @Nonnull Supplier<S> objSupplier )
		{
			this.objClass = objClass;
			this.objSupplier = objSupplier;
		}

		public S getInstance()
		{
			if ( objInstance == null )
				objInstance = objSupplier.get();
			return objInstance;
		}

		public Class<S> getObjClass()
		{
			return objClass;
		}
	}
}
