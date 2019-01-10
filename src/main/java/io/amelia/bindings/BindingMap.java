/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.bindings;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;

@SuppressWarnings( "unchecked" )
public final class BindingMap extends ContainerWithValue<BindingMap, BindingReference, BindingsException.Error>
{
	@Nonnull
	public static BindingMap empty()
	{
		try
		{
			return new BindingMap();
		}
		catch ( BindingsException.Error error )
		{
			// This should never happen!
			throw new RuntimeException( error );
		}
	}

	WeakReference<WritableBinding> privatizedOwner = null;

	private BindingMap() throws BindingsException.Error
	{
		super( BindingMap::new, "" );
	}

	protected BindingMap( String key ) throws BindingsException.Error
	{
		super( BindingMap::new, key );
	}

	protected BindingMap( BindingMap parent, String key ) throws BindingsException.Error
	{
		super( BindingMap::new, parent, key );
	}

	public Stream<BindingReference> findValues( Class<?> valueClass )
	{
		return getAllChildren().filter( child -> child.hasValue() && valueClass.isAssignableFrom( child.getValue().get().getObjClass() ) ).flatMap( map -> map.value.getInstances() );
	}

	public <S> Stream<S> getValues( Class<S> expectedClass )
	{
		BindingReference ref = getValue().orElse( null );
		if ( ref == null || !expectedClass.isAssignableFrom( ref.getObjClass() ) )
			return Stream.empty();
		return ref.getInstances();
	}

	@Override
	protected BindingsException.Error getException( @Nonnull String message, Exception exception )
	{
		// TODO Include node in exception
		return new BindingsException.Error( message );
	}

	public boolean isPrivatized()
	{
		return ( privatizedOwner != null && privatizedOwner.get() != null || parent != null && parent.isPrivatized() );
	}

	public void privatize( WritableBinding writableBinding ) throws BindingsException.Denied
	{
		if ( isPrivatized() )
			throw new BindingsException.Denied( "Namespace \"" + getNamespace().getString() + "\" has already been privatized." );
		// Indented to unprivatize children and notify the potential owners.
		unprivatize();
		privatizedOwner = new WeakReference<>( writableBinding );
	}

	private void unprivatize()
	{
		getChildren().forEach( BindingMap::unprivatize );
		if ( privatizedOwner != null && privatizedOwner.get() != null )
			privatizedOwner.get().destroy();
	}

	/**
	 * Called from the WritableBinding
	 */
	void unprivatize( WritableBinding writableBinding )
	{
		if ( privatizedOwner != null && privatizedOwner.get() != null && privatizedOwner.get() == writableBinding )
			privatizedOwner = null;
	}
}
