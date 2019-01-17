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

import java.lang.reflect.Method;
import java.util.List;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.EnumColor;
import io.amelia.support.Namespace;
import io.amelia.support.Priority;
import io.amelia.support.Voluntary;

public class Binding
{
	final InternalBinding ourBinding;
	private boolean destroyed;

	public Binding( InternalBinding ourBinding )
	{
		this.ourBinding = ourBinding;
	}

	public void addHook( String name, Method method, Priority priority )
	{
		ourBinding.addHook( name, method, priority );
	}

	public void addResolver( BindingResolver bindingResolver )
	{
		ourBinding.addResolver( bindingResolver );
	}

	public void destroy() throws BindingsException.Denied
	{
		if ( isPrivatized() )
			throw new BindingsException.Denied( "Can't destroy binding when it's privatized." );
		if ( ourBinding.getNamespace().startsWith( "io.amelia" ) )
			throw new BindingsException.Denied( "Can't destroy system bindings." );
		this.destroyed = true;
	}

	public Binding getChild( Namespace namespace )
	{
		return Bindings.getBinding( ourBinding.getNamespace().append( namespace.getString( "." ) ) );
	}

	public void invokeHook( String name, Object... arguments ) throws ApplicationException.Error
	{
		Bindings.L.info( "%sAttempting to invoke hook \"%s#%s\": ", EnumColor.GRAY, ourBinding.getNamespace(), name );
		ourBinding.invokeHook( name, arguments );
	}

	public boolean isDestroyed()
	{
		return destroyed;
	}

	public boolean isPrivatized()
	{
		return !isDestroyed() && ourBinding.isPrivatized();
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
		Namespace namespace = ourBinding.getNamespace();
		if ( namespace.startsWith( "io.amelia" ) )
			throw new BindingsException.Denied( "Can't privatized system bindings." );
		if ( namespace.getNodeCount() < 3 )
			throw new BindingsException.Denied( "Namespaces with less than three nodes can't be privatized, that would create a monopoly." );
		ourBinding.privatize( this );
	}

	public <T> Voluntary<T> resolve( @Nonnull String key )
	{
		return ourBinding.resolve( key );
	}

	public <T> Voluntary<T> resolve( @Nonnull Class<?> classObject )
	{
		return ourBinding.resolve( classObject );
	}

	public Binding resolveNamespace( Namespace namespace )
	{
		if ( namespace.getNodeCount() == 0 )
			return this;
		return Bindings.getBinding( ourBinding.getNamespace().append( namespace.getNames() ) );
	}

	public <S> void set( List<S> obj ) throws BindingsException.Error
	{
		ourBinding.setValue( new BindingReference( obj ) );
		ourBinding.trimChildren();
	}
}
