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

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.ConsumerWithException;
import io.amelia.support.EnumColor;
import io.amelia.support.FunctionWithException;
import io.amelia.support.Namespace;
import io.amelia.support.Priority;
import io.amelia.support.QuadFunctionWithException;
import io.amelia.support.Strs;
import io.amelia.support.SupplierWithException;
import io.amelia.support.TriFunctionWithException;
import io.netty.util.internal.ConcurrentSet;

/**
 * TODO Strip of anytime outside of namespaces
 */
public class Bindings
{
	public static final Kernel.Logger L = Kernel.getLogger( Bindings.class );
	private static final InternalBinding rootBinding = InternalBinding.empty();
	private static final Binding root = new Binding( rootBinding );
	private static boolean initalized = false;
	// TODO Cache annotated methods to save time with each start-up. Maybe only do it in development mode?
	private static Reflections reflections;

	public static Binding getBinding( Namespace namespace )
	{
		return new Binding( getChildOrCreate( namespace ) );
	}

	/**
	 * Returns a {@link Binding} for the provided system class.
	 *
	 * The concept is that any class that exists under the io.amelia namespace has a binding accessible to the entire system.
	 *
	 * @param aClass The class needing the binding.
	 *
	 * @return The namespace.
	 */
	public static Binding getBindingForClass( Class<?> aClass )
	{
		Package pack = aClass.getPackage();
		if ( pack == null )
			throw new BindingsException.Denied( "We had a problem obtaining the package from class \"" + aClass.getName() + "\"." );
		return getBinding( Namespace.of( pack.getName() ) );
	}

	@Nullable
	protected static InternalBinding getChild( @Nonnull Class<?> aClass )
	{
		Package pack = aClass.getPackage();
		if ( pack == null )
			throw new BindingsException.Denied( "We had a problem obtaining the package from class \"" + aClass.getName() + "\"." );
		return getChild( Namespace.of( pack.getName() ) );
	}

	@Nullable
	protected static InternalBinding getChild( @Nonnull Namespace namespace )
	{
		// Namespace used to guarantee the namespace complies with our own standards.
		if ( Lock.isWriteLockedByCurrentThread() || Lock.isReadLockedByCurrentThread() )
			return rootBinding.getChild( namespace.getString( "." ) );
		else
			return Lock.callWithReadLock( () -> Bindings.rootBinding.getChild( namespace.getString( "." ) ) );
	}

	@Nullable
	protected static InternalBinding getChildOrCreate( @Nonnull Class<?> aClass )
	{
		Package pack = aClass.getPackage();
		if ( pack == null )
			throw new BindingsException.Denied( "We had a problem obtaining the package from class \"" + aClass.getName() + "\"." );
		return getChildOrCreate( Namespace.of( pack.getName() ) );
	}

	@Nonnull
	protected static InternalBinding getChildOrCreate( @Nonnull Namespace namespace )
	{
		if ( Lock.isWriteLockedByCurrentThread() || Lock.isReadLockedByCurrentThread() )
			return rootBinding.getChildOrCreate( namespace.getString( "." ) );
		else
			return Lock.callWithReadLock( () -> Bindings.rootBinding.getChildOrCreate( namespace.getString( "." ) ) );
	}

	public static Binding getSystemBinding()
	{
		return getBinding( Namespace.of( "io.amelia" ) );
	}

	public static void init() throws ApplicationException.Error
	{
		if ( initalized )
			throw new BindingsException.Denied( "Bindings has already ben initialized!" );
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
		/*Streams.forEachWithException( ConfigRegistry.config.getChild( Foundation.ConfigKeys.BINDINGS_FACADES ).getChildren(), child -> {
			if ( child.hasChild( "class" ) )
			{
				Class<FacadeBinding> facadeClass = child.getStringAsClass( "class", FacadeBinding.class ).orElse( null );
				FacadePriority priority = child.getEnum( "priority", FacadePriority.class ).orElse( FacadePriority.NORMAL );

				if ( facadeClass == null )
					Kernel.L.warning( "We found malformed arguments in the facade config for key -> " + child.getName() );
				else
				{
					Binding binding;
					if ( child.hasChild( "namespace" ) && child.isType( "namespace", String.class ) )
						binding = Bindings.getBinding( Namespace.of( child.getString( "namespace" ).orElseThrow( RuntimeException::new ) ) );
					else
						binding = Bindings.getBindingForClass( facadeClass );

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
		} );*/

		getBindingForClass( Bindings.class ).invokeHook( "init" );
		initalized = true;
	}

	public static void initHooks()
	{
		// The Hooks system is similar to events but less feature rich, we highly recommend not using this feature unless you need to catch critical/early events.

		synchronized ( Bindings.class )
		{
			if ( reflections != null )
				throw new BindingsException.Ignorable( "Hooks have already been initialized." );

			L.info( "Initializing Hooks" );
			reflections = new Reflections( new MethodAnnotationsScanner() );

			reflections.getMethodsAnnotatedWith( Hook.class ).forEach( hookMethod -> {
				Hook annotation = hookMethod.getAnnotation( Hook.class );
				if ( annotation == null )
					throw new ApplicationException.Ignorable( "That method must be annotated with Hook!" );
				if ( !Modifier.isStatic( hookMethod.getModifiers() ) )
					throw new ApplicationException.Ignorable( "That method is not static!" );
				Namespace namespace = Namespace.of( annotation.ns() );
				Priority priority = annotation.priority();
				getBinding( namespace.getParent() ).addHook( namespace.getStringLast(), hookMethod, priority );
				Foundation.L.info( "%s    -> Discovered hook method \"%s#%s\" with namespace \"%s\" at priority \"%s\".", EnumColor.GRAY, hookMethod.getDeclaringClass().getName(), hookMethod.getName(), namespace.toString(), priority );
			} );
		}
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

	private Bindings()
	{
		// Static Access
	}

	protected static class Lock
	{
		private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock( true );
		private static final java.util.concurrent.locks.Lock readLock = lock.readLock();
		private static final Set<WeakReference<Thread>> readLockedThreads = new ConcurrentSet<>();
		private static final java.util.concurrent.locks.Lock writeLock = lock.writeLock();

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

		public static boolean isReadLockedByCurrentThread()
		{
			return readLockedThreads.stream().filter( ref -> Thread.currentThread().equals( ref.get() ) ).map( WeakReference::get ).findAny().isPresent();
		}

		public static boolean isWriteLocked()
		{
			return lock.isWriteLocked();
		}

		public static boolean isWriteLockedByCurrentThread()
		{
			return lock.isWriteLockedByCurrentThread();
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
	}
}
