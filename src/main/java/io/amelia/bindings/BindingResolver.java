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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.Foundation;
import io.amelia.lang.ExceptionReport;
import io.amelia.support.Namespace;
import io.amelia.support.Priority;
import io.amelia.support.Reflection;
import io.amelia.support.Voluntary;

/**
 * <pre>
 * public Object exampleMethod( @BindingClass( Logger.class ) Object obj )
 * {
 *
 * }
 * </pre>
 * <br />
 *
 * @see io.amelia.foundation.Foundation#addClassToClassAlias(Class, Class)
 * @see io.amelia.foundation.Foundation#addClassToNamespaceAlias(Class, Namespace)
 */
public class BindingResolver
{
	private final List<Mapping> mappings = new CopyOnWriteArrayList<>();
	private final Priority priority;
	private String defaultKey = null;

	public BindingResolver( Priority priority ) throws BindingsException.Error
	{
		this.priority = priority;
		initMappings();
	}

	public void addObject( @Nonnull Object object ) throws BindingsException.Error
	{
		if ( object instanceof Supplier )
			throw new BindingsException.Error( "Added object can't be a supplier, use #setSupplier method instead." );

		Mapping mapping = getResolverMapping( object.getClass() );
		mapping.addObject( object );
	}

	public Priority getPriority()
	{
		return priority;
	}

	@Nonnull
	public Mapping getResolverMapping( @Nullable Class<?> fromClass )
	{
		return mappings.stream().filter( entry -> entry.fromClass == fromClass ).findFirst().orElseGet( () -> {
			Mapping mapping = new Mapping( fromClass );
			mappings.add( mapping );
			return mapping;
		} );
	}

	@Nullable
	public Mapping getResolverMapping( @Nullable String key )
	{
		return mappings.stream().filter( entry -> entry.keys.contains( key == null || key.length() == 0 ? defaultKey : key ) ).findFirst().orElse( null );
	}

	public void initMappings() throws BindingsException.Error
	{
		Bindings.L.info( "Initializing Binding Resolver \"" + getClass().getName() + "\"." );

		for ( Method method : getClass().getMethods() )
		{
			if ( method.isAnnotationPresent( ProvidesClass.class ) )
			{
				Class<?> providesClass = method.getAnnotation( ProvidesClass.class ).value();
				if ( !providesClass.isAssignableFrom( method.getReturnType() ) )
					throw new BindingsException.Error( "The method \"" + method.getName() + "\" provides class \"" + providesClass.getName() + "\" but the return type of \"" + method.getReturnType().getName() + "\" is not assignable." );

				InternalBinding internalBinding = Bindings.getChild( providesClass );
				if ( internalBinding != null )
					internalBinding.addResolver( this );

				Mapping mapping = getResolverMapping( providesClass );
				mapping.setSingular( method.isAnnotationPresent( Singular.class ) );
				mapping.setSupplier( () -> {
					try
					{
						method.setAccessible( true );
						return method.invoke( this, Foundation.resolveParameters( method.getParameters() ) );
					}
					catch ( IllegalAccessException | InvocationTargetException | BindingsException.Error e )
					{
						ExceptionReport.handleSingleException( e );
					}
					return null;
				} );
				mapping.addKey( method.getName() );

				Bindings.L.info( "    -> Found mapping for class \"" + providesClass.getName() + "\" with method \"" + Reflection.readoutMethod( method ) + "\"." );
			}

			if ( method.isAnnotationPresent( ProvidesBinding.class ) )
			{
				Namespace providesNamespace = Namespace.of( method.getAnnotation( ProvidesBinding.class ).value() );

				InternalBinding internalBinding = Bindings.getChild( providesNamespace.getParent() );
				if ( internalBinding != null )
					internalBinding.addResolver( this );

				Mapping mapping = getResolverMapping( providesNamespace.getLocalName() );
				if ( mapping != null )
				{
					mapping.setSingular( method.isAnnotationPresent( Singular.class ) );
					mapping.setSupplier( () -> {
						try
						{
							method.setAccessible( true );
							return method.invoke( this, Foundation.resolveParameters( method.getParameters() ) );
						}
						catch ( IllegalAccessException | InvocationTargetException | BindingsException.Error e )
						{
							ExceptionReport.handleSingleException( e );
						}
						return null;
					} );
					mapping.addKey( method.getName() );

					Bindings.L.info( "    -> Found mapping for namespace \"" + providesNamespace.getString() + "\" with method \"" + Reflection.readoutMethod( method ) + "\"." );
				}
			}
		}

		for ( Field field : getClass().getFields() )
		{
			if ( field.isAnnotationPresent( ProvidesClass.class ) )
			{
				Class<?> providesClass = field.getAnnotation( ProvidesClass.class ).value();
				if ( !providesClass.isAssignableFrom( field.getType() ) )
					throw new BindingsException.Error( "The field \"" + field.getName() + "\" provides class \"" + providesClass.getName() + "\" but the declared type \"" + field.getType().getName() + "\" is not assignable." );

				InternalBinding internalBinding = Bindings.getChild( providesClass );
				if ( internalBinding != null )
					internalBinding.addResolver( this );

				Mapping mapping = getResolverMapping( providesClass );
				mapping.setSingular( field.isAnnotationPresent( Singular.class ) );
				mapping.setSupplier( () -> {
					try
					{
						field.setAccessible( true );
						return field.get( this );
					}
					catch ( IllegalAccessException e )
					{
						ExceptionReport.handleSingleException( e );
					}
					return null;
				} );
				mapping.addKey( field.getName() );

				Bindings.L.info( "    -> Found mapping for class \"" + providesClass.getName() + "\" with field \"" + Reflection.readoutField( field ) + "\"." );
			}

			if ( field.isAnnotationPresent( ProvidesBinding.class ) )
			{
				Namespace providesNamespace = Namespace.of( field.getAnnotation( ProvidesBinding.class ).value() );

				InternalBinding internalBinding = Bindings.getChild( providesNamespace.getParent() );
				if ( internalBinding != null )
					internalBinding.addResolver( this );

				Mapping mapping = getResolverMapping( providesNamespace.getLocalName() );
				if ( mapping != null )
				{
					mapping.setSingular( field.isAnnotationPresent( Singular.class ) );
					mapping.setSupplier( () -> {
						try
						{
							field.setAccessible( true );
							return field.get( this );
						}
						catch ( IllegalAccessException e )
						{
							ExceptionReport.handleSingleException( e );
						}
						return null;
					} );
					mapping.addKey( field.getName() );

					Bindings.L.info( "    -> Found mapping for namespace \"" + providesNamespace.getString() + "\" with field \"" + Reflection.readoutField( field ) + "\"." );
				}
			}
		}
	}

	protected void setDefault( String defaultKey )
	{
		this.defaultKey = defaultKey;
	}

	public class Mapping<Obj>
	{
		// Would the next attempt to set anything be overridden or produce an exception?
		boolean allowOverride = false;
		// The original class this mapping represents
		Class<Obj> fromClass;
		// Available instances for this mapping
		List<Obj> instances = new ArrayList<>();
		// Alias keys that map to this mapping
		Set<String> keys = new HashSet<>();
		// Enforce a single object per mapping rule
		boolean singular;
		// An available supplier to produce this object
		Supplier<Obj> supplier;
		// Alias to another Class
		Class<?> toClass;
		// Alias to another Namespace
		Namespace toNamespace;

		private Mapping( @Nonnull Class<Obj> fromClass )
		{
			this.fromClass = fromClass;
		}

		public void addKey( @Nullable String key )
		{
			keys.add( key );
		}

		public void addObject( @Nonnull Obj obj ) throws BindingsException.Error
		{
			if ( !fromClass.isAssignableFrom( obj.getClass() ) )
				throw new BindingsException.Error( "The class \"" + fromClass.getName() + "\" is not assignable from object class \"" + obj.getClass().getName() + "\"." );

			noToNamespace();
			noToClass();
			if ( singular )
				noInstances();

			instances.add( obj );
		}

		/**
		 * By default this resolver will not allow previously set mappings to be overridden.
		 * When you allow override, the previous mapping will be removed, otherwise an exception will be thrown.
		 */
		public void allowOverride()
		{
			this.allowOverride = true;
		}

		public <T> Stream<T> getObjects()
		{
			return instances.stream().map( obj -> ( T ) obj );
		}

		private void noInstances() throws BindingsException.Error
		{
			if ( instances.size() > 0 )
				if ( allowOverride )
					instances.clear();
				else
					throw new BindingsException.Error( "This mapping for class \"" + fromClass.getName() + "\" already has instances." );
		}

		private void noSupplier() throws BindingsException.Error
		{
			if ( supplier != null )
				if ( allowOverride )
					supplier = null;
				else
					throw new BindingsException.Error( "This mapping for class \"" + fromClass.getName() + "\" has a supplier set." );
		}

		private void noToClass() throws BindingsException.Error
		{
			if ( toClass != null )
				if ( allowOverride )
					toClass = null;
				else
					throw new BindingsException.Error( "This mapping for class \"" + fromClass.getName() + "\" has a toClass set to \"" + toClass.getName() + "\"." );
		}

		private void noToNamespace() throws BindingsException.Error
		{
			if ( toNamespace != null )
				if ( allowOverride )
					toNamespace = null;
				else
					throw new BindingsException.Error( "This mapping for class \"" + fromClass.getName() + "\" has a toNamespace set to \"" + toNamespace.getString() + "\"." );
		}

		public <T> Voluntary<T> resolve( Voluntary<T> result )
		{
			if ( !result.isPresent() )
			{
				if ( instances.size() > 0 )
					result = Voluntary.of( ( T ) instances.get( 0 ) );

				if ( !result.isPresent() && supplier != null )
				{
					Obj obj = supplier.get();
					if ( obj != null )
					{
						instances.add( obj );
						result = Voluntary.of( ( T ) obj );
					}
				}

				if ( !result.isPresent() && toClass != null )
					result = Foundation.make( toClass );

				if ( !result.isPresent() && toNamespace != null )
					result = Foundation.make( toNamespace );
			}

			return result;
		}

		public void setSingular( boolean singular ) throws BindingsException.Error
		{
			if ( singular && instances.size() > 0 )
				if ( allowOverride )
					instances = instances.stream().limit( 1 ).collect( Collectors.toList() );
				else
					throw new BindingsException.Error( "Can't make mapping singular when it contains more than one instance." );

			this.singular = singular;
		}

		public void setSupplier( @Nonnull Supplier<Obj> supplier ) throws BindingsException.Error
		{
			noToClass();
			noToNamespace();

			this.supplier = supplier;
			allowOverride = false;
		}

		public void setToClass( @Nonnull Class<?> toClass ) throws BindingsException.Error
		{
			noToNamespace();
			noInstances();
			noSupplier();

			this.toClass = toClass;
			allowOverride = false;
		}

		public void setToNamespace( @Nonnull Namespace toNamespace ) throws BindingsException.Error
		{
			noToClass();
			noInstances();
			noSupplier();

			this.toNamespace = toNamespace;
			allowOverride = false;
		}

		public void unmap()
		{
			mappings.remove( this );
		}
	}
}
