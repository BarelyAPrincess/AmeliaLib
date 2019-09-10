/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Used to track a local value using a key that is weak referenced.
 *
 * @param <T>
 */
public class LocalObject<T>
{
	private final Supplier<T> supplier;
	private final List<LocalObjectReference> references = new ArrayList<>();

	public LocalObject( Supplier<T> supplier )
	{
		this.supplier = supplier;
	}

	public LocalObject()
	{
		this( null );
	}

	private void clean()
	{
		synchronized ( references )
		{
			for ( LocalObjectReference ref : references )
				if ( ref.key.get() == null )
					references.remove( ref );
		}
	}

	public void clear()
	{
		references.clear();
	}

	@SuppressWarnings( "unchecked" )
	public T getState( @Nonnull Object key )
	{
		synchronized ( references )
		{
			clean();

			for ( LocalObjectReference ref : references )
				if ( ref.key.get() == key )
					return ( T ) ref.value;

			if ( supplier != null )
			{
				T value = supplier.get();
				references.add( new LocalObjectReference( key, value ) );
				return value;
			}

			return null;
		}
	}

	public void setState( @Nonnull Object key, @Nonnull T value )
	{
		synchronized ( references )
		{
			clean();

			for ( LocalObjectReference ref : references )
				if ( ref.key.get() == key )
				{
					ref.value = value;
					return;
				}

			references.add( new LocalObjectReference( key, value ) );
		}
	}

	private class LocalObjectReference
	{
		WeakReference<Object> key;
		T value;

		LocalObjectReference( Object key, T value )
		{
			this.key = new WeakReference<>( key );
			this.value = value;
		}
	}
}
