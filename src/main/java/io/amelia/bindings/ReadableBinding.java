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

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;

public class ReadableBinding
{
	final String ourNamespace;

	public ReadableBinding( String ourNamespace )
	{
		this.ourNamespace = Bindings.normalizeNamespace( ourNamespace );
	}

	/**
	 * Looks for an instance that implements the {@link FacadeBinding}.
	 * <p>
	 * On top of checking the provided namespace, we also append `facade` and `instance`.
	 *
	 * @param <T>       The {@link FacadeBinding} subclass.
	 * @param namespace The namespace to check
	 *
	 * @return The FacadeService instance, null otherwise.
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends FacadeBinding> VoluntaryWithCause<T, BindingsException.Error> getFacadeBinding( @Nonnull String namespace )
	{
		Voluntary<FacadeRegistration> facadeRegistration = getFacadeBindingRegistration( namespace );
		if ( !facadeRegistration.isPresent() )
			return VoluntaryWithCause.withException( new BindingsException.Error( "The specified namespace was not resolvable." ) );
		return VoluntaryWithCause.ofWithCause( ( T ) facadeRegistration.get().getHighestPriority() );
	}

	public <T extends FacadeBinding> VoluntaryWithCause<T, BindingsException.Error> getFacadeBinding( @Nonnull String namespace, @Nonnull Class<T> expectedClassBinding )
	{
		VoluntaryWithCause<T, BindingsException.Error> result = getFacadeBinding( namespace );
		if ( result.isPresent() && !expectedClassBinding.isAssignableFrom( result.get().getClass() ) )
			return VoluntaryWithCause.withException( new BindingsException.Error( "The specified class did not match the registered facade class. [resultingClass=" + result.get().getClass().getSimpleName() + "]" ) );
		return result;
	}

	/**
	 * Queries for a facade registration. This will return if no facade has been registered.
	 *
	 * @param namespace The facade namespace appended to the base namespace.
	 *
	 * @return facade registration or null
	 */
	public Voluntary<FacadeRegistration> getFacadeBindingRegistration( @Nonnull String namespace )
	{
		Optional<FacadeRegistration> facadeRegistration = getObject( namespace, FacadeRegistration.class ).findAny();

		if ( !facadeRegistration.isPresent() )
			facadeRegistration = getObject( namespace + ".facade", FacadeRegistration.class ).findAny();

		if ( !facadeRegistration.isPresent() )
			facadeRegistration = getObject( namespace + ".instance", FacadeRegistration.class ).findAny();

		return Voluntary.of( facadeRegistration );
	}

	/**
	 * Get registrations for facade class.
	 *
	 * @param namespace The facade namespace appended to the base namespace.
	 *
	 * @return a stream of registrations
	 */
	public Stream<FacadeRegistration> getFacadeRegistrations( @Nonnull String namespace )
	{
		BindingMap map = Bindings.getChild( Bindings.normalizeNamespace( ourNamespace, namespace ) );
		if ( map == null )
			return Stream.empty();
		return map.findValues( FacadeRegistration.class ).map( baseBinding -> ( FacadeRegistration ) baseBinding.getInstances() );
	}

	public <T extends FacadeBinding> Stream<T> getFacades( @Nonnull Class<T> facadeService )
	{
		return getFacadeRegistrations( "" ).filter( facadeRegistration -> facadeService.isAssignableFrom( facadeRegistration.getBindingClass() ) ).map( facadeRegistration -> ( T ) facadeRegistration.getHighestPriority() );
	}

	public <T> Stream<T> getObject( @Nonnull String namespace, @Nonnull Class<T> expectedClass )
	{
		namespace = Bindings.normalizeNamespace( ourNamespace, namespace );

		BindingMap ref = Bindings.getChild( namespace );
		Stream<T> result;

		if ( ref == null )
		{
			Voluntary voluntary = Bindings.resolveNamespace( namespace, expectedClass );
			result = voluntary.isPresent() ? Stream.empty() : Stream.of( ( T ) voluntary.get() );
		}
		else
			result = ref.getValues( expectedClass );

		return result;

		/* return StreamSupport.stream( new Spliterator<T>()
		{
			@Override
			public int characteristics()
			{
				return 0;
			}

			@Override
			public long estimateSize()
			{
				return 0;
			}

			@Override
			public boolean tryAdvance( Consumer<? super T> action )
			{
				Bindings.Lock.callWithReadLock( () -> {
					Object obj = ref == null ?  :ref.getValue();

					if ( obj != null && !expectedClass.isAssignableFrom( obj.getClass() ) )
						return Voluntary.withException( new BindingsException.Error( "The object returned for namespace `" + namespace + "` wasn't assigned to class `" + expectedClass.getSimpleName() + "`." ) );

					return Voluntary.ofNullable( ( T ) obj );
				} );

				return false;
			}

			@Override
			public Spliterator<T> trySplit()
			{
				return null;
			}
		}, false ); */
	}

	public <T> Stream<T> getObject( @Nonnull Class<T> objectClass )
	{
		return getObject( ourNamespace, objectClass );
	}

	@SuppressWarnings( "unchecked" )
	public <T> Stream<T> getObject( @Nonnull String namespace )
	{
		return ( Stream<T> ) getObject( namespace, Object.class );
	}

	public ReadableBinding getSubNamespace( String namespace )
	{
		return Bindings.getNamespace( ourNamespace + "." + namespace );
	}

	/**
	 * <p>
	 * Setter Example:
	 * <pre>
	 * {
	 *      NamespaceBinding nb = Bindings.bindNamespace("com.google.somePlugin");
	 *      nb.setObject("facade", new SomeFacadeService());
	 *      nb.setObject("obj", new Object());
	 * }
	 * </pre>
	 * <p>
	 * Getter Example:
	 * <pre>
	 * {
	 *      // When getting a facade service; we look at the namespace, then the namespace plus "facade" for an object that extends the FacadeService interface.
	 *      Bindings.getFacade("com.google.somePlugin");
	 *      // You can also define the expected facade class to ensure not just any facade is returned. This ensures you receive null instead of a ClassCastException.
	 *      Bindings.getFacade("com.google.somePlugin", SomeFacadeService.class);
	 *      // Any object can be set and retrieved from the bindings.
	 *      Bindings.getObject("com.google.somePlugin.obj");
	 * }
	 * </pre>
	 */
	public WritableBinding writable()
	{
		// TODO Validate writing permission - one such being a check of the calling package

		return new WritableBinding( ourNamespace );
	}
}
