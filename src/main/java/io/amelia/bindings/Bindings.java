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

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.lang.APINotice;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.ConsumerWithException;
import io.amelia.support.FunctionWithException;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.QuadFunctionWithException;
import io.amelia.support.Streams;
import io.amelia.support.Strs;
import io.amelia.support.SupplierWithException;
import io.amelia.support.TriFunctionWithException;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;
import io.netty.util.internal.ConcurrentSet;

public class Bindings
{
	public static final Kernel.Logger L = Kernel.getLogger( Bindings.class );
	private static final BindingMap bindings = BindingMap.empty();
	static final List<BindingResolver> resolvers = new ArrayList<>();
	private static final WritableBinding root = new WritableBinding( "" );

	@Nullable
	protected static BindingMap getChild( @Nonnull String namespace )
	{
		if ( Lock.isWriteLockedByCurrentThread() || Lock.isReadLockedByCurrentThread() )
			return bindings.getChild( namespace );
		else
			return Lock.callWithReadLock( () -> Bindings.bindings.getChild( namespace ) );
	}

	@Nonnull
	protected static BindingMap getChildOrCreate( @Nonnull String namespace )
	{
		if ( Lock.isWriteLockedByCurrentThread() || Lock.isReadLockedByCurrentThread() )
			return bindings.getChildOrCreate( namespace );
		else
			return Lock.callWithReadLock( () -> Bindings.bindings.getChildOrCreate( namespace ) );
	}

	public static ReadableBinding getNamespace( String namespace )
	{
		return new ReadableBinding( namespace );
	}

	private static Stream<BindingResolver> getResolvers()
	{
		return getResolvers( null );
	}

	private static Stream<BindingResolver> getResolvers( @Nullable String namespace )
	{
		namespace = normalizeNamespace( namespace );

		return Lock.callWithReadLock( namespace0 -> {
			return resolvers.stream().sorted( new BindingResolver.Comparator() ).filter( resolver -> namespace0 == null || namespace0.startsWith( resolver.baseNamespace ) );

			/*for ( Map.Entry<String, WeakReference<BoundNamespace>> entry : boundNamespaces.entrySet() )
				if ( ( namespace == null || namespace.startsWith( entry.getKey() ) ) && entry.getValue().get() != null )
				{
					BindingResolver bindingResolver = entry.getValue().get().getResolver();
					if ( bindingResolver != null )
						list.add( bindingResolver );
				}*/
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
		Package pack = aClass.getPackage();
		if ( pack == null )
			throw new BindingsException.Denied( "We had a problem obtaining the package from class \"" + aClass.getName() + "\"." );
		String packName = pack.getName();
		// TODO Pull package from calling class and determine if it's permitted
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

	private static <T> VoluntaryWithCause<T, BindingsException.Error> invokeConstructors( @Nonnull Class<? super T> declaringClass, @Nonnull Predicate<Constructor> constructorPredicate, @Nonnull Object... args )
	{
		if ( declaringClass.isInterface() )
			return Voluntary.withException( new BindingsException.Error( "Not possible to invoke constructor on interfaces." ) );

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
					return Voluntary.withException( new BindingsException.Error( "Could not resolve a value for parameter. {name=" + parameter.getName() + ",type=" + parameter.getType() + "}" ) );

				arguments[i] = result;
			}

			if ( constructorPredicate.test( constructor ) )
			{
				try
				{
					return Voluntary.ofWithCause( ( T ) constructor.newInstance( arguments ) );
				}
				catch ( InstantiationException | IllegalAccessException | InvocationTargetException e )
				{
					e.printStackTrace();
					// Ignore and try next.
				}
			}
		}

		return Voluntary.withException( new BindingsException.Error( "Could not find invoke any constructors, either they are missing or none matched the args provided." ) );
	}

	public static <T> Voluntary<T> invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate )
	{
		return invokeFields( declaringObject, fieldPredicate, null );
	}

	protected static <T> Voluntary<T> invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate, @Nullable String namespace )
	{
		for ( Field field : declaringObject.getClass().getDeclaredFields() )
			if ( fieldPredicate.test( field ) )
				try
				{
					field.setAccessible( true );
					T obj = ( T ) field.get( declaringObject );

					if ( !field.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
						bindings.getChildOrCreate( namespace ).setValue( new BindingReference( obj ) );

					return Voluntary.of( obj );
				}
				catch ( IllegalAccessException | BindingsException.Error error )
				{
					error.printStackTrace();
				}

		return Voluntary.empty();
	}

	public static <T> Voluntary<T> invokeMethods( @Nonnull Object declaringObject, @Nonnull Predicate<Method> methodPredicate, Object[] objs )
	{
		return invokeMethods( declaringObject, methodPredicate, null, objs );
	}

	protected static <T> Voluntary<T> invokeMethods( @Nonnull Object declaringObject, @Nonnull Predicate<Method> methodPredicate, @Nullable String namespace, Object[] objs )
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
				T obj = ( T ) method.invoke( declaringObject, resolveParameters( method.getParameters(), objs ) );

				if ( !method.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
					bindings.getChildOrCreate( namespace ).setValue( new BindingReference( obj ) );

				return Voluntary.of( obj );
			}
			catch ( IllegalAccessException | InvocationTargetException | BindingsException.Error e )
			{
				e.printStackTrace();
			}

		return Voluntary.empty();
	}

	static String normalizeNamespace( @Nullable String namespace )
	{
		return namespace == null ? null : Strs.toAscii( namespace.replaceAll( "[^a-z0-9_.]", "" ) );
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

	public static <T> VoluntaryWithCause<T, BindingsException.Error> resolveClass( @Nonnull Class<? super T> expectedClass, @Nonnull Object... args )
	{
		VoluntaryWithCause<T, BindingsException.Error> result = Voluntary.ofWithCause( getResolvers().map( bindingResolver -> bindingResolver.get( expectedClass, args ) ).filter( Objs::isNotNull ).map( obj -> ( T ) obj ).findAny() );

		if ( !result.isPresent() )
			result = invokeConstructors( expectedClass, ( constructor ) -> true, args );

		if ( !result.hasErrored() && !result.isPresent() )
			result = result.withCause( new BindingsException.Error( "Could not resolve class " + expectedClass.getSimpleName() ) );

		return result;
	}

	protected static <T> Voluntary<T> resolveNamespace( @Nonnull String namespace, @Nonnull Class<? super T> expectedClass, @Nonnull Object... args )
	{
		Objs.notEmpty( namespace );
		return Voluntary.of( getResolvers( namespace ).map( bindingResolver -> ( T ) bindingResolver.get( namespace, expectedClass ) ).filter( Objs::isNotNull ).findAny() );
	}

	/**
	 * Attempts to resolve the provided parameters in order provided.
	 */
	public static Object[] resolveParameters( @Nonnull Parameter[] parameters, @Nullable Object[] args0 ) throws BindingsException.Error
	{
		if ( parameters.length == 0 )
			return new Object[0];

		Map<Class<?>, Object> args = args0 == null ? new HashMap<>() : Arrays.stream( args0 ).collect( Collectors.toMap( Object::getClass, o -> o ) );
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

			Class<?> classType;

			if ( obj == null )
			{
				if ( parameter.isAnnotationPresent( BindingClass.class ) )
					classType = parameter.getAnnotation( BindingClass.class ).value();
				else
					classType = parameter.getType();

				obj = args.entrySet().stream().filter( entry -> classType.isAssignableFrom( entry.getKey() ) ).map( Map.Entry::getValue ).findAny().orElse( null );

				if ( obj == null )
					obj = resolveClass( parameter.getAnnotation( BindingClass.class ).value() );
			}

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
		// Static Access
	}

	protected static class Lock
	{
		private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock( true );
		private static final java.util.concurrent.locks.Lock readLock = lock.readLock();
		private static final java.util.concurrent.locks.Lock writeLock = lock.writeLock();
		private static final Set<WeakReference<Thread>> readLockedThreads = new ConcurrentSet<>();

		protected static <T1, T2, T3, R, E extends Exception> R callWithReadLock( TriFunctionWithException<T1, T2, T3, R, E> function, T1 arg0, T2 arg1, T3 arg2 ) throws E
		{
			readLock();
			try
			{
				return function.apply( arg0, arg1, arg2 );
			}
			finally
			{
				readUnlock();
			}
		}

		protected static <T1, T2, R, E extends Exception> R callWithReadLock( BiFunctionWithException<T1, T2, R, E> function, T1 arg0, T2 arg1 ) throws E
		{
			readLock();
			try
			{
				return function.apply( arg0, arg1 );
			}
			finally
			{
				readUnlock();
			}
		}

		protected static <T, R, E extends Exception> R callWithReadLock( FunctionWithException<T, R, E> function, T arg0 ) throws E
		{
			readLock();
			try
			{
				return function.apply( arg0 );
			}
			finally
			{
				readUnlock();
			}
		}

		protected static <R, E extends Exception> R callWithReadLock( SupplierWithException<R, E> supplier ) throws E
		{
			readLock();
			try
			{
				return supplier.get();
			}
			finally
			{
				readUnlock();
			}
		}

		protected static <T1, T2, T3, T4, R, E extends Exception> R callWithWriteLock( QuadFunctionWithException<T1, T2, T3, T4, R, E> function, T1 arg0, T2 arg1, T3 arg2, T4 arg3 ) throws E
		{
			writeLock();
			try
			{
				return function.apply( arg0, arg1, arg2, arg3 );
			}
			finally
			{
				writeUnlock();
			}
		}

		protected static <T1, T2, T3, R, E extends Exception> R callWithWriteLock( TriFunctionWithException<T1, T2, T3, R, E> function, T1 arg0, T2 arg1, T3 arg2 ) throws E
		{
			writeLock();
			try
			{
				return function.apply( arg0, arg1, arg2 );
			}
			finally
			{
				writeUnlock();
			}
		}

		protected static <T1, T2, R, E extends Exception> R callWithWriteLock( BiFunctionWithException<T1, T2, R, E> function, T1 arg0, T2 arg1 ) throws E
		{
			writeLock();
			try
			{
				return function.apply( arg0, arg1 );
			}
			finally
			{
				writeUnlock();
			}
		}

		protected static <T, R, E extends Exception> R callWithWriteLock( FunctionWithException<T, R, E> function, T arg0 ) throws E
		{
			writeLock();
			try
			{
				return function.apply( arg0 );
			}
			finally
			{
				writeUnlock();
			}
		}

		protected static <T, E extends Exception> void callWithWriteLock( ConsumerWithException<T, E> consumer, T arg0 ) throws E
		{
			writeLock();
			try
			{
				consumer.accept( arg0 );
			}
			catch ( Exception e )
			{
				throw ( E ) e;
			}
			finally
			{
				writeUnlock();
			}
		}

		protected static <T1, T2> void callWithWriteLock( BiConsumer<T1, T2> consumer, T1 arg0, T2 arg1 )
		{
			writeLock();
			try
			{
				consumer.accept( arg0, arg1 );
			}
			finally
			{
				writeUnlock();
			}
		}

		public static void readLock()
		{
			readLock.lock();
			readLockedThreads.add( new WeakReference<>( Thread.currentThread() ) );
		}

		public static void readUnlock()
		{
			readLock.unlock();
			readLockedThreads.stream().filter( ref -> Thread.currentThread().equals( ref.get() ) ).forEach( readLockedThreads::remove );
		}

		public static boolean isReadLockedByCurrentThread()
		{
			return readLockedThreads.stream().filter( ref -> Thread.currentThread().equals( ref.get() ) ).map( WeakReference::get ).findAny().isPresent();
		}

		public static void writeLock()
		{
			if ( isReadLockedByCurrentThread() )
				throw new RuntimeException( "Read locks can't be upgraded to write locks." );
			writeLock.lock();
		}

		public static void writeUnlock()
		{
			writeLock.unlock();
		}

		public static boolean isWriteLockedByCurrentThread()
		{
			return lock.isWriteLockedByCurrentThread();
		}

		public static boolean isWriteLocked()
		{
			return lock.isWriteLocked();
		}
	}
}
