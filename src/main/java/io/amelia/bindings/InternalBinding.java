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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.ContainerWithValue;
import io.amelia.lang.ApplicationException;
import io.amelia.support.EnumColor;
import io.amelia.support.Priority;
import io.amelia.support.Streams;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;

/**
 * Never to be seen or manipulated by outside classes.
 */
final class InternalBinding extends ContainerWithValue<InternalBinding, BindingReference, BindingsException.Error>
{
	@Nonnull
	public static InternalBinding empty()
	{
		try
		{
			return new InternalBinding();
		}
		catch ( BindingsException.Error error )
		{
			// This should never happen!
			throw new RuntimeException( error );
		}
	}

	WeakReference<Binding> privatizedOwner = null;
	private Map<String, List<HookRef>> hooks = new HashMap<>();
	private List<BindingResolver> resolvers = new ArrayList<>();

	private InternalBinding() throws BindingsException.Error
	{
		super( InternalBinding::new, "" );
	}

	protected InternalBinding( String key ) throws BindingsException.Error
	{
		super( InternalBinding::new, key );
	}

	protected InternalBinding( InternalBinding parent, String key ) throws BindingsException.Error
	{
		super( InternalBinding::new, parent, key );
	}

	public void addHook( String name, Method method, Priority priority )
	{
		synchronized ( this )
		{
			if ( !Strs.isCamelCase( name ) )
				throw new BindingsException.Denied( "Hook name must be alphanumeric." );
			hooks.computeIfAbsent( name, key -> new ArrayList<>() ).add( new HookRef( method, priority ) );
		}
	}

	public void addResolver( BindingResolver bindingResolver )
	{
		if ( !resolvers.contains( bindingResolver ) )
			resolvers.add( bindingResolver );
	}

	public Stream<BindingReference> findValues( Class<?> valueClass )
	{
		return getAllChildren().filter( child -> child.hasValue() && valueClass.isAssignableFrom( child.getValue().get().getObjClass() ) ).flatMap( map -> map.value.getInstances() );
	}

	@Override
	protected BindingsException.Error getException( @Nonnull String message, Exception exception )
	{
		// TODO Include node in exception
		return new BindingsException.Error( message );
	}

	public <S> Stream<S> getValues( Class<S> expectedClass )
	{
		BindingReference ref = getValue().orElse( null );
		if ( ref == null || !expectedClass.isAssignableFrom( ref.getObjClass() ) )
			return Stream.empty();
		return ref.getInstances();
	}

	public void invokeHook( String name, Object... arguments ) throws ApplicationException.Error
	{
		// TODO Organize and resolve arguments, ideally using the Bindings if possible.

		synchronized ( this )
		{
			Streams.forEachWithException( hooks.entrySet().stream().filter( entry -> entry.getKey().equals( name ) ).map( Map.Entry::getValue ).flatMap( List::stream ), ref -> {
				try
				{
					ref.invoke( arguments );
				}
				catch ( InvocationTargetException | IllegalAccessException e )
				{
					throw new ApplicationException.Error( "Encountered an exception while attempting to invoke hook.", e );
				}
			} );
		}
	}

	public boolean isPrivatized()
	{
		return ( privatizedOwner != null && privatizedOwner.get() != null || parent != null && parent.isPrivatized() );
	}

	public boolean isPrivatizedTo( @Nonnull Binding binding )
	{
		return ( privatizedOwner != null && privatizedOwner.get() == binding || parent != null && parent.isPrivatizedTo( binding ) );
	}

	public void privatize( Binding binding ) throws BindingsException.Denied
	{
		if ( isPrivatized() )
			throw new BindingsException.Denied( "Namespace \"" + getNamespace().getString() + "\" has already been privatized." );
		// Indented to unprivatize children and notify the potential owners.
		unprivatize();
		privatizedOwner = new WeakReference<>( binding );
	}

	public <T> Voluntary<T> resolve( @Nonnull String key )
	{
		Voluntary<T> result = Voluntary.empty();

		resolvers.sort( new Priority.Comparator<>( BindingResolver::getPriority ) );

		for ( BindingResolver bindingResolver : resolvers )
		{
			BindingResolver.Mapping mapping = bindingResolver.getResolverMapping( key );
			if ( mapping != null )
				return mapping.resolve( result );
		}

		return result;
	}

	public <T> Voluntary<T> resolve( Class<?> classObject )
	{
		Bindings.L.info( "Invoking binding resolvers for class \"" + classObject.getName() + "\"" );

		Voluntary<T> result = Voluntary.empty();

		resolvers.sort( new Priority.Comparator<>( BindingResolver::getPriority ) );

		for ( BindingResolver bindingResolver : resolvers )
			result = bindingResolver.getResolverMapping( classObject ).resolve( result );

		return result;
	}

	private void unprivatize()
	{
		getChildren().forEach( InternalBinding::unprivatize );
		if ( privatizedOwner != null && privatizedOwner.get() != null )
			privatizedOwner.get().destroy();
	}

	/**
	 * Called from the Binding
	 */
	void unprivatize( Binding writableBinding )
	{
		if ( privatizedOwner != null && privatizedOwner.get() != null && privatizedOwner.get() == writableBinding )
			privatizedOwner = null;
	}

	class HookRef implements Comparable<HookRef>
	{
		private final Method method;
		private final Parameter[] parameters;
		private final Priority priority;

		HookRef( Method method, Priority priority )
		{
			this.method = method;
			this.priority = priority;
			this.parameters = method.getParameters();
		}

		@Override
		public int compareTo( @Nonnull HookRef other )
		{
			return Integer.compare( priority.intValue(), other.priority.intValue() );
		}

		Priority getPriority()
		{
			return priority;
		}

		void invoke( Object... arguments ) throws InvocationTargetException, IllegalAccessException, ApplicationException.Error
		{
			Bindings.L.info( "%s    -> Invoking hook \"%s#%s\" at priority \"%s\"", EnumColor.GRAY, method.getDeclaringClass().getName(), method.getName(), priority );

			if ( arguments.length != parameters.length )
				throw new ApplicationException.Error( "Parameter count does not match the provided argument count." );

			for ( int i = 0; i < parameters.length; i++ )
				if ( !parameters[i].getType().isAssignableFrom( arguments[i].getClass() ) )
					throw new ApplicationException.Error( "Parameter type " + parameters[i].getType().getSimpleName() + " does not match the provided argument type " + arguments[i].getClass().getSimpleName() + "." );

			// TODO Skip failed hook calls.
			// TODO Save invoked parameters to hook getUserContext as they're likely the best source we got, this will govern all future hooks and invokes.

			method.invoke( null, arguments );
		}
	}
}
