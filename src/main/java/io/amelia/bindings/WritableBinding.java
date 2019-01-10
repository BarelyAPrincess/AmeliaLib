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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.support.Objs;
import io.amelia.support.Strs;

public class WritableBinding extends ReadableBinding
{
	private boolean destroyed;

	public WritableBinding( String namespace )
	{
		super( namespace );
	}

	public void destroy()
	{
		Bindings.getChildOrCreate( ourNamespace ).privatize( this );
		this.destroyed = true;
	}

	@Override
	public WritableBinding getSubNamespace( String namespace )
	{
		return Bindings.getNamespace( ourNamespace + "." + namespace ).writable();
	}

	public boolean isDestroyed()
	{
		return destroyed;
	}

	public boolean isPrivatized()
	{
		return !isDestroyed() && Bindings.getChildOrCreate( ourNamespace ).isPrivatized();
	}

	/**
	 * Attempts to privatize this writing binding, so only this reference can be allowed to make changes to the namespace.
	 * <p>
	 * This is done by making a weak reference of this binding from within the namespace. As long at this instance isn't
	 * destroyed by the JVM GC, it will remain private. However, remember that this will have no effect on writable instances
	 * requested prior to the construction of this method nor will it affect read access.
	 * <p>
	 * Also remember the only way to make the namespace public once again is to either dereference this instance or call the destroy method.
	 * It's also note worth that if a parent namespace is privatized, it will take precedence and destroy this WritableBinding.
	 */
	public void privatize() throws BindingsException.Denied
	{
		if ( ourNamespace.startsWith( "io.amelia" ) )
			throw new BindingsException.Denied( "Namespace \"io.amelia\" can't privatized as it's reserved for internal use." );
		if ( Strs.countMatches( ourNamespace, '.' ) < 2 )
			throw new BindingsException.Denied( "Namespaces with less than 3 nodes can't be privatized." );

		Bindings.getChildOrCreate( ourNamespace ).privatize( this );
	}

	public <T extends FacadeBinding> void registerFacadeBinding( @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier ) throws BindingsException.Error
	{
		registerFacadeBinding( facadeService, facadeSupplier, FacadePriority.NORMAL );
	}

	public <T extends FacadeBinding> void registerFacadeBinding( @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier, @Nonnull FacadePriority facadePriority ) throws BindingsException.Error
	{
		registerFacadeBinding( null, facadeService, facadeSupplier, facadePriority );
	}

	public <T extends FacadeBinding> void registerFacadeBinding( @Nullable String namespace, @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier ) throws BindingsException.Error
	{
		registerFacadeBinding( namespace, facadeService, facadeSupplier, FacadePriority.NORMAL );
	}

	public <T extends FacadeBinding> FacadeRegistration.Entry<T> registerFacadeBinding( @Nullable String namespace, @Nonnull Class<T> facadeService, @Nonnull Supplier<T> facadeSupplier, @Nonnull FacadePriority facadePriority ) throws BindingsException.Error
	{
		return Bindings.Lock.callWithWriteLock( ( namespace0, facadeService0, facadeSupplier0, facadePriority0 ) -> {
			namespace0 = Bindings.normalizeNamespace( namespace0 );

			String fullNamespace = Objs.isEmpty( namespace0 ) ? ourNamespace + ".facade" : ourNamespace + "." + namespace0;

			FacadeRegistration<T> facadeServiceList = getObject( fullNamespace, FacadeRegistration.class ).findAny().orElse( null );

			if ( facadeServiceList == null )
			{
				facadeServiceList = new FacadeRegistration<>( facadeService0 );
				set( fullNamespace, Collections.singletonList( facadeServiceList ) );
			}
			else if ( !facadeService0.isAssignableFrom( facadeServiceList.getBindingClass() ) )
				throw new BindingsException.Error( "The facade registered at namespace \"" + namespace0 + "\" does not match the facade already registered." );

			if ( facadeServiceList.isPriorityRegistered( facadePriority0 ) )
				throw new BindingsException.Error( "There is already a facade registered for priority level " + facadePriority0.name() + "at namespace \"" + namespace0 + "\"" );

			FacadeRegistration.Entry registration = new FacadeRegistration.Entry<T>( facadeSupplier0, facadePriority0 );
			facadeServiceList.add( registration );
			return registration;
		}, namespace, facadeService, facadeSupplier, facadePriority );
	}

	public <S> void set( String namespace, List<S> obj ) throws BindingsException.Error
	{
		namespace = Bindings.normalizeNamespace( ourNamespace, namespace );
		BindingMap map = Bindings.getChildOrCreate( namespace );
		map.setValue( new BindingReference( obj ) );
		map.trimChildren();
	}
}
