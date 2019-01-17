/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.bindings.Binding;
import io.amelia.bindings.BindingResolver;
import io.amelia.bindings.Bindings;
import io.amelia.bindings.BindingsException;
import io.amelia.bindings.ParameterClass;
import io.amelia.bindings.ParameterNamespace;
import io.amelia.bindings.Singular;
import io.amelia.data.TypeBase;
import io.amelia.events.Events;
import io.amelia.events.RunlevelEvent;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.StartupAbortException;
import io.amelia.lang.StartupException;
import io.amelia.looper.LooperRouter;
import io.amelia.looper.MainLooper;
import io.amelia.permissions.Permissions;
import io.amelia.plugins.Plugins;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.Exceptions;
import io.amelia.support.IO;
import io.amelia.support.Maps;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.Timing;
import io.amelia.support.Voluntary;
import io.amelia.users.Users;

/**
 * Used for accessing majority of the Foundation API.<br />
 * The first call to this class MUST be the thread that will initiate the main loop.
 * <p>
 * <p>
 * Your main() should look like this:
 * <code>
 * Foundation.prepare();
 * ...
 * ImplementedApplication app = new ImplementedApplication();
 * Foundation.setApplication( app );
 * ...
 * Foundation.start();
 * </code>
 */
public final class Foundation
{
	public static final Kernel.Logger L = Kernel.getLogger( Foundation.class );
	private static final Map<Class<?>, Class<?>> classToClassAlias = new HashMap<>();
	private static final Map<Class<?>, Namespace> classToNamespaceAlias = new HashMap<>();
	private static final Map<String, Object> methodSingularityMap = new HashMap<>();
	private static BaseApplication app = null;
	private static Runlevel currentRunlevel = Runlevel.INITIALIZATION;
	private static String currentRunlevelReason = null;
	private static EntitySubject entityNull;
	private static EntitySubject entityRoot;
	private static Runlevel previousRunlevel;
	private static Object runlevelTimingObject = new Object();

	/*
	* 	private static <T> Voluntary<T> invokeConstructors( @Nonnull Class<? extends T> declaringClass, @Nonnull Predicate<Constructor> constructorPredicate, @Nonnull Map<String, Object> arguments ) throws BindingsException.Error
	{
		L.debug( "Invoking constructors on class \"" + declaringClass.getName() + "\"." );

		if ( declaringClass.isInterface() )
			throw new BindingsException.Error( "Not possible to invoke constructor on interfaces." );

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
					result = Foundation.make( parameter.getType(), args );

				if ( result == null )
					result = Foundation.make( Strs.camelToNamespace( parameter.getName() ), parameter.getType(), args );

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

		throw new BindingsException.Error( "Could not find invoke any constructors, either they are missing or none matched the args provided." );
	}

	public static <T> Voluntary<T> invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate )
	{
		return invokeFields( declaringObject, fieldPredicate, null );
	}

	protected static <T> Voluntary<T> invokeFields( @Nonnull Object declaringObject, @Nonnull Predicate<Field> fieldPredicate, @Nullable String namespace )
	{
		L.debug( "Invoking fields on object \"" + declaringObject.getClass().getName() + "\" for namespace \"" + namespace + "\"." );

		for ( Field field : declaringObject.getClass().getDeclaredFields() )
		{
			boolean result = fieldPredicate.test( field );
			L.debug( "\tFound method \"" + Reflection.readoutField( field ) + "\". Potential Match: " + ( result ? "YES" : "NO" ) + "." );
			if ( result )
				try
				{
					field.setAccessible( true );
					T obj = ( T ) field.get( declaringObject );

					if ( !field.isAnnotationPresent( DynamicBinding.class ) && !Objs.isEmpty( namespace ) )
						rootBinding.getChildOrCreate( namespace ).setValue( new BindingReference( obj ) );

					return Voluntary.of( obj );
				}
				catch ( IllegalAccessException | BindingsException.Error error )
				{
					error.printStackTrace();
				}
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

		L.debug( "Invoking methods on object \"" + declaringObject.getClass().getName() + "\" for namespace \"" + namespace + "\"." );

		for ( Method method : declaringObject.getClass().getDeclaredMethods() )
		{
			boolean result = methodPredicate.test( method );
			L.debug( "\tFound method \"" + Reflection.readoutMethod( method ) + "\". Potential Match: " + ( result ? "YES" : "NO" ) + "." );
			if ( result )
				possibleMethods.put( method.getParameterCount(), method );
		}

		// Try each possible method, starting with the one with the least number of parameters.
		for ( Method method : possibleMethods.values() )
			try
			{
				method.setAccessible( true );
				T obj = ( T ) method.invoke( declaringObject, Foundation.resolveParameters( method.getParameters(), objs ) );

				if ( !method.isAnnotationPresent( DynamicBinding.class ) )
				{
					if ( Objs.isEmpty( namespace ) )
						namespace = "";
					rootBinding.getChildOrCreate( namespace ).setValue( new BindingReference( obj ) );
				}

				return Voluntary.of( obj );
			}
			catch ( IllegalAccessException | InvocationTargetException | BindingsException.Error e )
			{
				if ( Kernel.isDevelopment() )
					e.printStackTrace();
			}

		return Voluntary.empty();
	}
	*/

	private static boolean init = false;

	static
	{
		init();
	}

	public static void init()
	{
		if ( init )
			return;

		try
		{
			Bindings.initHooks();
			Bindings.getBindingForClass( Foundation.class ).invokeHook( "init" );
		}
		catch ( ApplicationException.Error e )
		{
			ExceptionReport.handleSingleException( e );
		}

		Kernel.setKernelHandler( new KernelHandler()
		{
			@Override
			public boolean isPrimaryThread()
			{
				return Foundation.isPrimaryThread();
			}
		} );

		init = true;
	}

	public static void addClassToClassAlias( @Nonnull Class<?> fromClass, @Nonnull Class<?> toClass ) throws BindingsException.Error
	{
		Object result;
		if ( fromClass == Object.class )
			throw new BindingsException.Error( "fromClass is disallowed!" );
		if ( fromClass == toClass )
			return;
		if ( Modifier.isAbstract( toClass.getModifiers() ) || toClass.isInterface() || toClass.isEnum() || toClass.isAnnotation() ) // Isn't there more or can this be simpler?
			throw new BindingsException.Error( "The toClass must be instigatable." );
		if ( !fromClass.isAssignableFrom( toClass ) )
			throw new BindingsException.Error( "Class alias must be assignable from, are you using the root-most class in common?" );
		if ( ( result = Maps.findKey( classToClassAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null || ( result = Maps.findKey( classToNamespaceAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null )
			throw new BindingsException.Error( "The fromClass (or a super thereof) alias already maps to \"" + ( result instanceof Class ? ( ( Class ) result ).getName() : result instanceof Namespace ? ( ( Namespace ) result ).getString() : result ) + "\"." );
		if ( ( result = Maps.findValue( classToClassAlias, value -> value == fromClass, Map.Entry::getValue ) ) != null )
			throw new BindingsException.Error( "The fromClass is already specified as a toClass alias, consider mapping from \"" + ( ( Class ) result ).getName() + "\" class instead." );
		classToClassAlias.put( fromClass, toClass );
	}

	public static void addClassToNamespaceAlias( @Nonnull Class<?> fromClass, @Nonnull Namespace toNamespace ) throws BindingsException.Error
	{
		Object result;
		if ( fromClass == Object.class )
			throw new BindingsException.Error( "fromClass is disallowed!" );
		if ( ( result = Maps.findKey( classToClassAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null || ( result = Maps.findKey( classToNamespaceAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null )
			throw new BindingsException.Error( "The fromClass (or a super thereof) alias already maps to \"" + ( result instanceof Class ? ( ( Class ) result ).getName() : result instanceof Namespace ? ( ( Namespace ) result ).getString() : result ) + "\"." );
		// if ( ( result = Maps.findValue( classToNamespaceAlias, value -> value == fromClass, Map.Entry::getValue ) ) != null )
		// throw new BindingsException.Error( "The fromClass is already specified as a toClass alias, consider mapping from \"" + ( ( Class ) result ).getName() + "\" class instead." );
		classToNamespaceAlias.put( fromClass, toNamespace );
	}

	public static <T extends BaseApplication> T getApplication()
	{
		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw ApplicationException.runtime( "The application has been DISPOSED!" );
		if ( app == null )
			throw ApplicationException.runtime( "The application instance has never been set!" );
		return ( T ) app;
	}

	public static Class<?> getClassToClassAlias( @Nonnull Class<?> fromClass )
	{
		// We make sure the fromClass isn't already a resolved alias - we will only do this once to prevent loop bugs.
		if ( Maps.findValue( classToClassAlias, value -> value == fromClass, Map.Entry::getValue ) != null )
			return null;
		return Maps.findKey( classToClassAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue );
	}

	public static Namespace getClassToNamespaceAlias( Class<?> fromClass )
	{
		// We make sure the fromClass isn't already a resolved alias - we will only do this once to prevent loop bugs.
		if ( Maps.findValue( classToClassAlias, value -> value == fromClass, Map.Entry::getValue ) != null )
			return null;
		return Maps.findKey( classToNamespaceAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue );
	}

	public static String getCurrentRunlevelReason()
	{
		return currentRunlevelReason;
	}

	public static Runlevel getLastRunlevel()
	{
		return previousRunlevel;
	}

	public static EntitySubject getNullEntity()
	{
		return entityNull;
	}

	public static Permissions getPermissions()
	{
		return Exceptions.tryCatchOrNotPresent( () -> make( Permissions.class ), exp -> new ApplicationException.Runtime( "The Permissions implementation failed.", exp ) );
	}

	public static Plugins getPlugins()
	{
		return Exceptions.tryCatchOrNotPresent( () -> make( Plugins.class ), exp -> new ApplicationException.Runtime( "The Plugins implementation failed.", exp ) );
	}

	public static EntitySubject getRootEntity()
	{
		return entityRoot;
	}

	public static Runlevel getRunlevel()
	{
		return currentRunlevel;
	}

	public static Users getUsers()
	{
		return Exceptions.tryCatchOrNotPresent( () -> make( Users.class ), exp -> new RuntimeException( "Users is not implemented!", exp ) );
	}

	public static boolean isNullEntity( EntityPrincipal entityPrincipal )
	{
		return entityNull.uuid().equals( entityPrincipal.uuid() );
	}

	public static boolean isNullEntity( UUID uuid )
	{
		return entityNull.uuid().equals( uuid );
	}

	public static boolean isPrimaryThread()
	{
		// If foundation has yet to be set, then it's anyone's guess which thread is primary and were not willing to take that risk. :(
		return app == null || app.isPrimaryThread();
	}

	public static boolean isRootEntity( EntityPrincipal entityPrincipal )
	{
		return entityRoot.uuid().equals( entityPrincipal.uuid() );
	}

	public static boolean isRootEntity( UUID uuid )
	{
		return entityRoot.uuid().equals( uuid );
	}

	public static boolean isRunlevel( Runlevel runlevel )
	{
		return currentRunlevel == runlevel;
	}

	public static <T> Voluntary<T> make( @Nonnull Namespace fromNamespace )
	{
		return make( fromNamespace, new HashMap<>() );
	}

	public static <T> Voluntary<T> make( @Nonnull Namespace fromNamespace, @Nonnull Map<String, ?> arguments )
	{
		Namespace parentNamespace = fromNamespace.getParent();
		Binding binding = Bindings.getBinding( parentNamespace );

		// TODO Not Finished!

		return binding.resolve( fromNamespace.getLocalName() );
	}

	public static <T> Voluntary<T> make( @Nonnull Class<?> fromClass )
	{
		return make( fromClass, new HashMap<>() );
	}

	public static <T> Voluntary<T> make( @Nonnull Class<?> fromClass, @Nonnull Map<String, ?> arguments )
	{
		Voluntary result = Voluntary.empty();

		Class<?> classToClassAliasResult = getClassToClassAlias( fromClass );
		if ( classToClassAliasResult != null )
			result = make( classToClassAliasResult, arguments );

		Namespace classToNamespaceAliasResult = getClassToNamespaceAlias( fromClass );
		if ( classToNamespaceAliasResult != null )
			result = make( classToNamespaceAliasResult, arguments );

		if ( !result.isPresent() )
		{
			Package classPackage = fromClass.getPackage();
			Objs.notNull( classPackage, "We had a problem obtaining the package from class \"" + fromClass.getName() + "\"." );
			Binding binding = Bindings.getBinding( Namespace.of( classPackage.getName() ) );
			result = binding.resolve( fromClass );
		}

		return result;
	}

	/**
	 * Handles post runlevel change. Should almost always be the very last method call when the runlevel changes.
	 */
	private static void onRunlevelChange() throws ApplicationException.Error
	{
		Events.getInstance().callEventWithException( new RunlevelEvent( previousRunlevel, currentRunlevel ) );

		app.onRunlevelChange( previousRunlevel, currentRunlevel );

		// Internal runlevel changes happen after this point. Generally progressing the application from each runlevel to the next.

		if ( currentRunlevel == Runlevel.STARTUP )
		{
			UUID nullUuid = UUID.fromString( ConfigRegistry.config.getString( ConfigKeys.UUID_NULL ) );
			UUID rootUuid = UUID.fromString( ConfigRegistry.config.getString( ConfigKeys.UUID_ROOT ) );

			entityNull = Exceptions.tryCatchOrNotPresent( () -> make( EntitySubject.class, Maps.builder( "uuid", nullUuid ).hashMap() ), exp -> new ApplicationException.Error( "Failed to create the NULL Entity.", exp ) );
			entityRoot = Exceptions.tryCatchOrNotPresent( () -> make( EntitySubject.class, Maps.builder( "uuid", rootUuid ).hashMap() ), exp -> new ApplicationException.Error( "Failed to create the ROOT Entity.", exp ) );
		}

		// Indicates the application has begun the main loop
		if ( currentRunlevel == Runlevel.MAINLOOP )
			if ( app instanceof NetworkedApplication )
				setRunlevelLater( Runlevel.NETWORKING );
			else
				setRunlevelLater( Runlevel.STARTED );

		// Indicates the application has started all and any networking
		if ( currentRunlevel == Runlevel.NETWORKING )
			setRunlevelLater( Runlevel.STARTED );

		// if ( currentRunlevel == Runlevel.CRASHED || currentRunlevel == Runlevel.RELOAD || currentRunlevel == Runlevel.SHUTDOWN )
		// L.notice( currentRunlevelReason );

		// TODO Implement the RELOAD runlevel!
		if ( currentRunlevel == Runlevel.RELOAD )
			throw ApplicationException.error( "Not Implemented. Sorry!" );

		if ( currentRunlevel == Runlevel.SHUTDOWN )
			app.quitSafely();

		if ( currentRunlevel == Runlevel.CRASHED )
			app.quitUnsafe();

		if ( currentRunlevel == Runlevel.DISPOSED )
		{
			// Runlevel DISPOSED is activated over the ApplicationLooper#joinLoop method returns.

			app.dispose();
			app = null;

			try
			{
				Thread.sleep( 100 );
			}
			catch ( InterruptedException e )
			{
				// Ignore
			}

			System.exit( 0 );
		}
	}

	/**
	 * Loads the built-in dependencies written by the gradle script.
	 * To best avoid {@link ClassNotFoundException}, this should be the very first call made by the main(String... args) method.
	 */
	public static void prepare() throws ApplicationException.Error
	{
		requireApplication();
		requirePrimaryThread();
		requireRunlevel( Runlevel.INITIALIZATION, "prepare() must be called at runlevel INITIALIZATION" );

		L.info( "Loading deployment libraries from \"" + Libraries.LIBRARY_DIR + "\"" );
		try
		{
			/* Load Deployment Libraries */
			L.info( "Loading deployment libraries defined in \"dependencies.txt\"." );
			for ( String depend : IO.resourceToString( "dependencies.txt" ).split( "\n" ) )
				Libraries.loadLibrary( new MavenReference( "builtin", depend ) );
		}
		catch ( IOException e )
		{
			throw new StartupException( "Failed to read the built-in dependencies file.", e );
		}
		L.info( EnumColor.AQUA + "Finished downloading deployment libraries." );

		// Call to make sure the INITIALIZATION Runlevel is acknowledged by the application.
		onRunlevelChange();
	}

	private static void requireApplication() throws ApplicationException.Error
	{
		if ( app == null )
			throw new ApplicationException.Error( "Application is expected to have been initialized by this point." );
	}

	public static void requirePrimaryThread()
	{
		requirePrimaryThread( null );
	}

	public static void requirePrimaryThread( String errorMessage )
	{
		if ( !isPrimaryThread() )
			throw new StartupException( errorMessage == null ? "Method MUST be called from the primary thread that initialed started the Kernel." : errorMessage );
	}

	public static void requireRunlevel( Runlevel runlevel )
	{
		requireRunlevel( runlevel, null );
	}

	public static void requireRunlevel( Runlevel runlevel, String errorMessage )
	{
		if ( !isRunlevel( runlevel ) )
			throw new StartupException( errorMessage == null ? "Method MUST be called at runlevel " + runlevel.name() : errorMessage );
	}

	public static Object[] resolveParameters( @Nonnull Parameter[] parameters ) throws BindingsException.Error
	{
		return resolveParameters( parameters, new HashMap<>() );
	}

	/**
	 * Attempts to resolve the provided parameters in order provided.
	 */
	public static Object[] resolveParameters( @Nonnull Parameter[] parameters, @Nonnull Map<String, ?> arguments ) throws BindingsException.Error
	{
		if ( parameters.length == 0 )
			return new Object[0];

		Object[] parameterObjects = new Object[parameters.length];

		for ( int i = 0; i < parameters.length; i++ )
		{
			Parameter parameter = parameters[i];
			@Nonnull
			Voluntary<?> obj = Voluntary.empty();

			if ( arguments.containsKey( parameter.getName() ) )
				obj = Voluntary.of( arguments.get( parameter.getName() ) );

			// TODO Reverse resolve if any of the provided arguments are from the specified namespace.
			if ( !obj.isPresent() && parameter.isAnnotationPresent( ParameterNamespace.class ) )
				obj = make( Namespace.of( parameter.getAnnotation( ParameterNamespace.class ).value() ), arguments );

			if ( !obj.isPresent() && parameter.isAnnotationPresent( ParameterClass.class ) )
			{
				final Class<?> classType = parameter.getAnnotation( ParameterClass.class ).value();
				obj = Voluntary.of( arguments.entrySet().stream().filter( entry -> classType.isAssignableFrom( entry.getValue().getClass() ) ).map( Map.Entry::getValue ).findAny() );
				if ( !obj.isPresent() )
					obj = make( classType, arguments );
			}

			if ( !obj.isPresent() )
			{
				final Class<?> classType = parameter.getType();
				obj = Voluntary.of( arguments.entrySet().stream().filter( entry -> classType.isAssignableFrom( entry.getValue().getClass() ) ).map( Map.Entry::getValue ).findAny() );
				if ( !obj.isPresent() )
					obj = make( classType, arguments );
			}

			// If the obj is null and the parameter is not nullable, then throw an exception.
			if ( !obj.isPresent() && !parameter.isAnnotationPresent( Nullable.class ) )
				throw new BindingsException.Error( "We failed to resolve the object for parameter " + parameter.getName() + " and the Nullable annotation was not present." );
			else
				parameterObjects[i] = obj.orElse( null );
		}

		return parameterObjects;
	}

	/**
	 * Sets an instance of BaseApplication for use by the Kernel
	 *
	 * @param app The BaseApplication instance
	 */
	public static void setApplication( @Nonnull BaseApplication app ) throws ApplicationException.Error
	{
		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw new ApplicationException.Error( "The application has been DISPOSED!" );
		if ( Foundation.app != null )
			throw new ApplicationException.Error( "The application instance has already been set!" );

		LooperRouter.setMainLooper( new FoundationLooper( app ) );
		Kernel.setExceptionRegistrar( app );

		Foundation.app = app;

		if ( !app.hasArgument( "no-banner" ) )
			app.showBanner( Kernel.L );

		L.info( "Application UUID: " + EnumColor.AQUA + app.uuid() );
	}

	public static void setRunlevel( @Nonnull Runlevel level )
	{
		setRunlevel( level, null );
	}

	/**
	 * Systematically changes the application runlevel.
	 * If this method is called by the application main thread, the change is made immediate.
	 */
	public static void setRunlevel( @Nonnull Runlevel runlevel, @Nullable String reason )
	{
		Objs.notNull( runlevel );
		MainLooper mainLooper = LooperRouter.getMainLooper();

		if ( Objs.isEmpty( reason ) )
		{
			String instanceId = getApplication().uuid().toString(); // getApplication().getEnv().getString( "instance-id" ).orElse( null );

			if ( runlevel == Runlevel.RELOAD )
				reason = String.format( "Server \"%s\" is reloading. Be back soon. :D", instanceId );
			else if ( runlevel == Runlevel.CRASHED )
				reason = String.format( "Server \"%s\" has crashed. Sorry about that. :(", instanceId );
			else if ( runlevel == Runlevel.SHUTDOWN )
				reason = String.format( "Server \"%s\" is shutting down. Good bye! :|", instanceId );
			else
				reason = "No reason provided.";
		}

		// If we confirm that the current thread is the same one that run the Looper, we make the runlevel change immediate instead of posting it for later.
		if ( !mainLooper.isThreadJoined() && app.isPrimaryThread() || mainLooper.isHeldByCurrentThread() )
			setRunlevel0( runlevel, reason );
			// joinLoop has not been called and yet we're crashing, time for an interrupt signal.
		else if ( !mainLooper.isThreadJoined() && runlevel == Runlevel.CRASHED )
			throw new StartupException( "Application has CRASHED!" );
			// Otherwise all other runlevels need to be scheduled, including a CRASH.
		else
			setRunlevelLater( runlevel, reason );
	}

	private synchronized static void setRunlevel0( @Nonnull Runlevel runlevel, @Nonnull String reason )
	{
		try
		{
			if ( Objs.isEmpty( reason ) )
				throw new ApplicationException.Error( "The runlevel change reason is empty." );
			if ( LooperRouter.getMainLooper().isThreadJoined() && !LooperRouter.getMainLooper().isHeldByCurrentThread() )
				throw new ApplicationException.Error( "Runlevel can only be set from the main looper thread. Be more careful next time." );
			if ( currentRunlevel == runlevel )
				throw new ApplicationException.Error( "Runlevel is already set to \"" + runlevel.name() + "\". This might be a severe race bug." );
			if ( !runlevel.checkRunlevelOrder( currentRunlevel ) )
				throw new ApplicationException.Error( "RunLevel \"" + runlevel.name() + "\" was set out of order. Present runlevel was \"" + currentRunlevel.name() + "\". This is potentially a race bug or there were exceptions thrown." );

			Timing.start( runlevelTimingObject );

			previousRunlevel = currentRunlevel;
			currentRunlevel = runlevel;
			currentRunlevelReason = reason;

			if ( runlevel == Runlevel.RELOAD || runlevel == Runlevel.SHUTDOWN || runlevel == Runlevel.CRASHED )
				L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "" + EnumColor.NEGATIVE + "Application is entering runlevel \"" + runlevel.name() + "\", for reason \"" + reason + "\"." );

			onRunlevelChange();

			if ( currentRunlevel == Runlevel.DISPOSED )
				L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "" + EnumColor.NEGATIVE + "Application has successfully shutdown! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
			else if ( currentRunlevel == Runlevel.STARTED )
				L.info( EnumColor.join( EnumColor.GOLD, EnumColor.NEGATIVE ) + "Application has successfully started! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
			else
				L.info( EnumColor.AQUA + "Application has entered runlevel \"" + runlevel.name() + "\". It took " + Timing.finish( runlevelTimingObject ) + "ms!" );

			if ( runlevel == Runlevel.CRASHED )
				throw new StartupAbortException();
		}
		catch ( ApplicationException.Error e )
		{
			ExceptionReport.handleSingleException( e );
		}
	}

	public static void setRunlevelLater( @Nonnull Runlevel level )
	{
		setRunlevelLater( level, null );
	}

	public static void setRunlevelLater( @Nonnull Runlevel level, @Nullable String reason )
	{
		Objs.notNull( level );
		LooperRouter.getMainLooper().postTask( entry -> setRunlevel0( level, reason ) );
	}

	public static void shutdown( String reason )
	{
		setRunlevel( Runlevel.SHUTDOWN, reason );
	}

	/**
	 * Will process the application load based on the information provided by the BaseApplication.
	 * Takes Runlevel from INITIALIZATION to RUNNING.
	 * <p>
	 * start() will not return until the main looper quits.
	 */
	public static void start() throws ApplicationException.Error
	{
		requirePrimaryThread();
		requireRunlevel( Runlevel.INITIALIZATION, "start() must be called at runlevel INITIALIZATION" );

		if ( getRunlevel() == Runlevel.CRASHED )
			return;

		// Initiate startup procedures.
		setRunlevel( Runlevel.STARTUP );

		if ( !ConfigRegistry.config.getBoolean( ConfigKeys.DISABLE_METRICS ) )
		{
			// TODO Implement!

			// Send Metrics

			final String instanceId = app.getEnv().getString( "instance-id" ).orElse( null );
		}

		// Abort
		if ( getRunlevel() == Runlevel.CRASHED )
			return;

		// Join this thread to the main looper.
		LooperRouter.getMainLooper().joinLoop();

		// Sets the application to the disposed state once the joinLoop method returns exception free.
		if ( getRunlevel() != Runlevel.CRASHED )
			setRunlevel( Runlevel.DISPOSED );
	}

	private Foundation()
	{
		// Static Access
	}

	public static class ConfigKeys
	{
		/**
		 * Specifies built-in facades which can be registered here or by calling {@link io.amelia.bindings.Bindings)} {@see Bindings#getBinding(String)}}.
		 * Benefits of using configuration for facade registration is it adds the ability for end-users to disable select facades, however, this should be used if the facade is used by scripts.
		 *
		 * <pre>
		 * bindings:
		 *   facades:
		 *     permissions:
		 *       class:io.amelia.foundation.facades.PermissionsService
		 *       priority: NORMAL
		 *     events:
		 *       class: io.amelia.foundation.facades.EventService
		 *       priority: NORMAL
		 * </pre>
		 */
		public static final TypeBase BINDINGS_FACADES = new TypeBase( "bindings.facades" );
		public static final TypeBase APPLICATION_BASE = ConfigRegistry.ConfigKeys.APPLICATION_BASE;
		/**
		 * Specifies a config key for disabling a application metrics.
		 *
		 * <pre>
		 * foundation:
		 *   disableMetrics: false
		 * </pre>
		 */
		public static final TypeBase.TypeBoolean DISABLE_METRICS = new TypeBase.TypeBoolean( APPLICATION_BASE, "disableMetrics", false );

		public static final TypeBase.TypeString UUID_NULL = new TypeBase.TypeString( APPLICATION_BASE, "entityUuidNull", Encrypt.uuid() );
		public static final TypeBase.TypeString UUID_ROOT = new TypeBase.TypeString( APPLICATION_BASE, "entityUuidRoot", Encrypt.uuid() );

		private ConfigKeys()
		{
			// Static Access
		}
	}
}
