/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.bindings;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.lang.APINotice;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.FunctionWithException;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.QuadFunctionWithException;
import io.amelia.support.Streams;
import io.amelia.support.Strs;
import io.amelia.support.TriFunctionWithException;

public class Bindings
{
	public static final Kernel.Logger L = Kernel.getLogger( Bindings.class );
	protected static final BindingMap bindings = BindingMap.empty();
	protected static final List<BindingResolver> resolvers = new ArrayList<>();
	protected static final WritableBinding root = new WritableBinding( "" );

	static BindingMap getChild( @Nonnull String namespace )
	{
		return bindings.getChild( namespace );
	}

	static BindingMap getChildOrCreate( @Nonnull String namespace )
	{
		return bindings.getChildOrCreate( namespace );
	}

	public static ReadableBinding getNamespace( String namespace )
	{
		return new ReadableBinding( namespace );
	}

	private static List<BindingResolver> getResolvers()
	{
		return getResolvers( null );
	}

	private static List<BindingResolver> getResolvers( String namespace )
	{
		return Lock.callWithReadLock( namespace0 -> {
			List<BindingResolver> list = new ArrayList<>();
			namespace0 = normalizeNamespace( namespace0 );

			/*for ( Map.Entry<String, WeakReference<BoundNamespace>> entry : boundNamespaces.entrySet() )
				if ( ( namespace == null || namespace.startsWith( entry.getKey() ) ) && entry.getValue().get() != null )
				{
					BindingResolver bindingResolver = entry.getValue().get().getResolver();
					if ( bindingResolver != null )
						list.add( bindingResolver );
				}*/

			resolvers.sort( new BindingResolver.Comparator() );

			for ( BindingResolver bindingResolver : resolvers )
				if ( namespace0 == null || namespace0.startsWith( bindingResolver.baseNamespace ) )
					list.add( bindingResolver );

			return list;
		}, namespace );
	}

	public static ReadableBinding getSystemNamespace()
	{
		return getNamespace( "io.amelia" );
	}

	/**
	 * Returns a {@link WritableBinding} for the provided system class.
	 *
	 * The concept is that any class that exists under the io.amelia namespace has a binding accessible to the entire system.
	 *
	 * @param aClass The class needing the binding.
	 *
	 * @return The namespace.
	 */
	public static WritableBinding getSystemNamespace( Class<?> aClass )
	{
		// For now we'll assign system namespaces based on the class package, however, in the future we might want to do some additional checking to make sure someone isn't trying to spoof the protected io.amelia package but still allowing out classes to get through.

		Package pack = aClass.getPackage();
		if ( pack == null )
			throw new BindingsException.Denied( "We had a problem obtaining the package from class \"" + aClass.getName() + "\"." );
		String packName = pack.getName();
		// if ( !packName.startsWith( "io.amelia." ) )
		// throw new BindingsException.Denied( "Only internal class starting with \"io.amelia\" can be got through this method." );
		return new WritableBinding( packName );
	}

	public static void init() throws BindingsException.Error
	{
		if ( !ConfigRegistry.isLoaded() )
			throw new BindingsException.Error( "Bindings can not be initialized before the ConfigRegistry has finished loading." );

		/*
		 * Register facades from configuration:
		 *
		 * {
		 *   class: "io.amelia.facades.permissionBinding",
		 *   namespace: "io.amelia.permissions.facade",
		 *   priority: NORMAL
		 * }
		 */
		Streams.forEachWithException( ConfigRegistry.config.getChild( Foundation.ConfigKeys.BINDINGS_FACADES ).getChildren(), child -> {
			if ( child.hasChild( "class" ) )
			{
				Class<FacadeBinding> facadeClass = child.getStringAsClass( "class", FacadeBinding.class ).orElse( null );
				FacadePriority priority = child.getEnum( "priority", FacadePriority.class ).orElse( FacadePriority.NORMAL );

				if ( facadeClass == null )
					Kernel.L.warning( "We found malformed arguments in the facade config for key -> " + child.getName() );
				else
				{
					WritableBinding binding;
					if ( child.hasChild( "namespace" ) && child.isType( "namespace", String.class ) )
						binding = Bindings.getNamespace( child.getString( "namespace" ).orElseThrow( RuntimeException::new ) ).writable();
					else
						binding = Bindings.getSystemNamespace( facadeClass ).writable();

					try
					{
						binding.registerFacadeBinding( facadeClass, () -> Objs.initClass( facadeClass ), priority );
					}
					catch ( BindingsException.Error e )
					{
						Kernel.L.severe( "Failed to register facade from config for key. {facadeKey=" + child.getName() + "}", e );
					}
				}
			}
			else
				Kernel.L.warning( "We found malformed arguments in the facade config. {facadeKey=" + child.getName() + "}" );
		} );
	}

	private static <T> T invokeConstructors( @Nonnull Class<? super T> declaringClass, @Nonnull Predicate<Constructor> constructorPredicate, @Nonnull Object... args ) throws BindingsException.Error
	{
		if ( declaringClass.isInterface() )
			return null;

		List<Constructor<?>> constructors = Arrays.asList( declaringClass.getDeclaredConstructors() );

		constructors.sort( Comparator.comparingInt( Constructor::getParameterCount ) );

		for ( Constructor<?> constructor : constructors )
		{
			Parameter[] parameters = constructor.getParameters();
			Object[] arguments = new Object[parameters.length];
			for ( int i = 0; i < parameters.length; i++ )
			{
				Parameter parameter = parameters[i];
				Object result = null;
				for ( int a = 0; a < args.length; a++ )
				{
					if ( args[a] instanceof Supplier )
						args[a] = ( ( Supplier ) args[a] ).get();
					if ( parameter.getType().isAssignableFrom( args[a].getClass() ) )
						result = args[a];
				}

				if ( result == null )
					result = Bindings.resolveClass( parameter.getType(), args );

				if ( result == null )
					result = Bindings.resolveNamespace( Strs.camelToNamespace( parameter.getName() ), parameter.getType(), args );

				if ( result == null )
					throw new BindingsException.Error( "Could not resolve a value for parameter. {name=" + parameter.getName() + ",type=" + parameter.getType() + "}" );

				arguments[i] = result;
			}

			if ( constructorPredicate.test( constructor ) )
			{
				try
				{
					return ( T ) constructor.newInstance( arguments );
				}
				catch ( InstantiationException | IllegalAccessException | InvocationTargetException e )
				{
					// Ignore and try next.
				}
			}
		}

		return null;
	}

	public static <T> T invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate )
	{
		return invokeFields( declaringObject, fieldPredicate, null );
	}

	protected static <T> T invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate, @Nullable String namespace )
	{
		for ( Field field : declaringObject.getClass().getDeclaredFields() )
			if ( fieldPredicate.test( field ) )
				try
				{
					field.setAccessible( true );
					T obj = ( T ) field.get( declaringObject );

					if ( !field.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
						bindings.getChildOrCreate( namespace ).set( obj );

					return obj;
				}
				catch ( IllegalAccessException | BindingsException.Error error )
				{
					error.printStackTrace();
				}

		return null;
	}

	public static <T> T invokeMethods( @Nonnull Object declaringObject, @Nonnull Predicate<Method> methodPredicate )
	{
		return invokeMethods( declaringObject, methodPredicate, null );
	}

	protected static <T> T invokeMethods( @Nonnull Object declaringObject, @Nonnull Predicate<Method> methodPredicate, @Nullable String namespace )
	{
		Map<Integer, Method> possibleMethods = new TreeMap<>();

		for ( Method method : declaringObject.getClass().getDeclaredMethods() )
			if ( methodPredicate.test( method ) )
				possibleMethods.put( method.getParameterCount(), method );

		// Try each possible method, starting with the one with the least number of parameters.
		for ( Method method : possibleMethods.values() )
			try
			{
				method.setAccessible( true );
				T obj = ( T ) method.invoke( declaringObject, resolveParameters( method.getParameters() ) );

				if ( !method.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
					bindings.getChildOrCreate( namespace ).set( obj );

				return obj;
			}
			catch ( IllegalAccessException | InvocationTargetException | BindingsException.Error e )
			{
				e.printStackTrace();
			}

		return null;
	}

	static String normalizeNamespace( @Nullable String namespace )
	{
		if ( namespace == null )
			return null;
		return Strs.toAscii( namespace.replaceAll( "[^a-z0-9_.]", "" ) );
	}

	public static String normalizeNamespace( @Nonnull String baseNamespace, @Nullable String namespace )
	{
		baseNamespace = normalizeNamespace( baseNamespace );
		namespace = namespace == null ? "" : normalizeNamespace( namespace );

		if ( !namespace.startsWith( baseNamespace ) )
			namespace = baseNamespace + "." + namespace;

		return Strs.trimAll( namespace, '.' ).replaceAll( "\\.{2,}", "." );
	}

	@APINotice
	public static void registerResolver( @Nonnull String namespace, @Nonnull BindingResolver bindingResolver ) throws BindingsException.Error
	{
		Objs.notEmpty( namespace );
		Objs.notNull( bindingResolver );

		Namespace ns = Namespace.of( namespace ).fixInvalidChars().normalizeAscii();
		if ( ns.startsWith( "io.amelia" ) )
			throw new BindingsException.Error( "Namespace \"io.amelia\" is reserved for internal use only." );
		if ( ns.getNodeCount() < 3 )
			throw new BindingsException.Error( "Resolvers can only be registered to namespaces with no less than 3 nodes." );
		namespace = ns.getString();

		bindingResolver.baseNamespace = namespace;
		resolvers.add( bindingResolver );
	}

	public static <T> T resolveClass( @Nonnull Class<? super T> expectedClass, @Nonnull Object... args )
	{
		try
		{
			return resolveClassOrFail( expectedClass, args );
		}
		catch ( BindingsException.Error e )
		{
			return null;
		}
	}

	public static <T, E extends Exception> T resolveClassOrFail( @Nonnull Class<? super T> expectedClass, @Nonnull Supplier<E> exceptionSupplier, @Nonnull Object... args ) throws E
	{
		try
		{
			return resolveClassOrFail( expectedClass, args );
		}
		catch ( BindingsException.Error e )
		{
			throw exceptionSupplier.get();
		}
	}

	public static <T> T resolveClassOrFail( @Nonnull Class<? super T> expectedClass, @Nonnull Object... args ) throws BindingsException.Error
	{
		for ( BindingResolver bindingResolver : getResolvers() )
		{
			Object obj = bindingResolver.get( expectedClass, args );
			if ( obj != null )
				return ( T ) obj;
		}

		T result = Bindings.invokeConstructors( expectedClass, () -> true, args );
		if ( result != null )
			return result;

		throw new BindingsException.Error( "Could not resolve class " + expectedClass.getSimpleName() );
	}

	protected static <T> T resolveNamespace( @Nonnull String namespace, @Nonnull Class<? super T> expectedClass, @Nonnull Object... args )
	{
		Objs.notEmpty( namespace );

		for ( BindingResolver bindingResolver : getResolvers( namespace ) )
		{
			Object obj = bindingResolver.get( namespace, expectedClass );
			if ( obj != null )
				return ( T ) obj;
		}

		return null;
	}

	public static Object[] resolveParameters( @Nonnull Parameter[] parameters ) throws BindingsException.Error
	{
		if ( parameters.length == 0 )
			return new Object[0];

		Object[] parameterObjects = new Object[parameters.length];

		for ( int i = 0; i < parameters.length; i++ )
		{
			Parameter parameter = parameters[i];
			Object obj = null;

			if ( parameter.isAnnotationPresent( BindingNamespace.class ) )
			{
				String ns = parameter.getAnnotation( BindingNamespace.class ).value();
				obj = root.getObject( ns );
			}

			if ( obj == null && parameter.isAnnotationPresent( BindingClass.class ) )
				obj = resolveClass( parameter.getAnnotation( BindingClass.class ).value() );

			if ( obj == null )
				obj = resolveClass( parameter.getType() );

			// If the obj is null and the parameter is not nullable, then throw an exception.
			if ( obj == null && !parameter.isAnnotationPresent( Nullable.class ) )
				throw new BindingsException.Error( "Method parameter " + parameter.getName() + " had BindingNamespace annotation and we failed to resolve it!" );
			else
				parameterObjects[i] = obj;
		}

		return parameterObjects;
	}

	private Bindings()
	{
		// Private Access
	}

	protected static class Lock
	{
		private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		private static final java.util.concurrent.locks.Lock readLock = lock.readLock();
		private static final java.util.concurrent.locks.Lock writeLock = lock.writeLock();

		protected static <T1, T2, T3, R, E extends Exception> R callWithReadLock( TriFunctionWithException<T1, T2, T3, R, E> function, T1 arg0, T2 arg1, T3 arg2 ) throws E
		{
			readLock.lock();
			try
			{
				return function.apply( arg0, arg1, arg2 );
			}
			finally
			{
				readLock.unlock();
			}
		}

		protected static <T1, T2, R, E extends Exception> R callWithReadLock( BiFunctionWithException<T1, T2, R, E> function, T1 arg0, T2 arg1 ) throws E
		{
			readLock.lock();
			try
			{
				return function.apply( arg0, arg1 );
			}
			finally
			{
				readLock.unlock();
			}
		}

		protected static <T, R, E extends Exception> R callWithReadLock( FunctionWithException<T, R, E> function, T arg0 ) throws E
		{
			readLock.lock();
			try
			{
				return function.apply( arg0 );
			}
			finally
			{
				readLock.unlock();
			}
		}

		protected static <T1, T2, T3, T4, R, E extends Exception> R callWithWriteLock( QuadFunctionWithException<T1, T2, T3, T4, R, E> function, T1 arg0, T2 arg1, T3 arg2, T4 arg3 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0, arg1, arg2, arg3 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		protected static <T1, T2, T3, R, E extends Exception> R callWithWriteLock( TriFunctionWithException<T1, T2, T3, R, E> function, T1 arg0, T2 arg1, T3 arg2 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0, arg1, arg2 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		protected static <T1, T2, R, E extends Exception> R callWithWriteLock( BiFunctionWithException<T1, T2, R, E> function, T1 arg0, T2 arg1 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0, arg1 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		protected static <T, R, E extends Exception> R callWithWriteLock( FunctionWithException<T, R, E> function, T arg0 ) throws E
		{
			writeLock.lock();
			try
			{
				return function.apply( arg0 );
			}
			finally
			{
				writeLock.unlock();
			}
		}

		public void readLock()
		{
			readLock.lock();
		}

		public void readUnlock()
		{
			readLock.unlock();
		}

		public void writeLock()
		{
			writeLock.lock();
		}

		public void writeUnlock()
		{
			writeLock.unlock();
		}
	}
}
