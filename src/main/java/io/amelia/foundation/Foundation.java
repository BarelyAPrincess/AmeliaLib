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

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.TypeBase;
import io.amelia.events.Events;
import io.amelia.events.RunlevelEvent;
import io.amelia.injection.Libraries;
import io.amelia.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.MultipleException;
import io.amelia.lang.StartupException;
import io.amelia.looper.LooperRouter;
import io.amelia.looper.MainLooper;
import io.amelia.permissions.Permissions;
import io.amelia.plugins.Plugins;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.Exceptions;
import io.amelia.support.IO;
import io.amelia.support.LooperException;
import io.amelia.support.Maps;
import io.amelia.support.Objs;
import io.amelia.support.Priority;
import io.amelia.support.Streams;
import io.amelia.support.Strs;
import io.amelia.support.Timing;
import io.amelia.support.Voluntary;
import io.amelia.users.Users;
import io.amelia.users.UsersMemory;

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
	public static final String HOOK_ACTION_INIT = "init";
	public static final String HOOK_ACTION_PARSE = "parse";
	public static final String HOOK_ACTION_DEFAULT = "default";

	private static final List<Class<?>> allElseFailClasses = new ArrayList<>();
	private static final Map<Class<?>, Class<?>> classToClassAlias = new HashMap<>();
	private static final Map<Class<?>, List<HookRef>> hooks = new HashMap<>();
	private static final Map<Class<?>, Map<Priority, Object>> mappings = new HashMap<>();
	private static final Map<Class<?>, Object> mappingsSingular = new HashMap<>();

	private static BaseApplication app = null;
	private static Runlevel currentRunlevel = Runlevel.INITIALIZATION;
	private static String currentRunlevelReason = null;
	private static EntitySubject entityNull;
	private static EntitySubject entityRoot;
	private static boolean init = false;
	private static Runlevel previousRunlevel;
	// TODO Cache annotated methods to save time with each start-up. Maybe only do it in development mode?
	private static Reflections reflections;
	/*
	* 	private static <T> Voluntary<T> invokeConstructors( @Nonnull Class<? extends T> declaringClass, @Nonnull Predicate<Constructor> constructorPredicate, @Nonnull Map<String, Object> arguments ) throws ApplicationException.Error
	{
		L.debug( "Invoking constructors on class \"" + declaringClass.getName() + "\"." );

		if ( declaringClass.isInterface() )
			throw new ApplicationException.Error( "Not possible to invoke constructor on interfaces." );

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
					return Voluntary.withException( new ApplicationException.Error( "Could not resolve a value for parameter. {name=" + parameter.getName() + ",type=" + parameter.getType() + "}" ) );

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

		throw new ApplicationException.Error( "Could not find invoke any constructors, either they are missing or none matched the args provided." );
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
				catch ( IllegalAccessException | ApplicationException.Error error )
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
			catch ( IllegalAccessException | InvocationTargetException | ApplicationException.Error e )
			{
				if ( Kernel.isDevelopment() )
					e.printStackTrace();
			}

		return Voluntary.empty();
	}
	*/
	private static Object runlevelTimingObject = new Object();

	static
	{
		init();
	}

	/*public static void addClassToNamespaceAlias( @Nonnull Class<?> fromClass, @Nonnull Namespace toNamespace ) throws ApplicationException.Error
	{
		Object result;
		if ( fromClass == Object.class )
			throw new ApplicationException.Error( "fromClass is disallowed!" );
		if ( ( result = Maps.findKey( classToClassAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null || ( result = Maps.findKey( classToNamespaceAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null )
			throw new ApplicationException.Error( "The fromClass (or a super thereof) alias already maps to \"" + ( result instanceof Class ? ( ( Class ) result ).getName() : result instanceof Namespace ? ( ( Namespace ) result ).getString() : result ) + "\"." );
		// if ( ( result = Maps.findValue( classToNamespaceAlias, value -> value == fromClass, Map.Entry::getValue ) ) != null )
		// throw new ApplicationException.Error( "The fromClass is already specified as a toClass alias, consider mapping from \"" + ( ( Class ) result ).getName() + "\" class instead." );
		classToNamespaceAlias.put( fromClass, toNamespace );
	}*/

	public static void addClassToClassAlias( @Nonnull Class<?> fromClass, @Nonnull Class<?> toClass ) throws ApplicationException.Error
	{
		Object result;
		if ( fromClass == Object.class )
			throw new ApplicationException.Error( "fromClass is disallowed!" );
		if ( fromClass == toClass )
			return;
		if ( Modifier.isAbstract( toClass.getModifiers() ) || toClass.isInterface() || toClass.isEnum() || toClass.isAnnotation() ) // Isn't there more or can this be simpler?
			throw new ApplicationException.Error( "The toClass must be instigatable." );
		if ( !fromClass.isAssignableFrom( toClass ) )
			throw new ApplicationException.Error( "Class alias must be assignable from, are you using the root-most class in common?" );
		// if ( ( result = Maps.findKey( classToClassAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null || ( result = Maps.findKey( classToNamespaceAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue ) ) != null )
		// throw new ApplicationException.Error( "The fromClass (or a super thereof) alias already maps to \"" + ( result instanceof Class ? ( ( Class ) result ).getName() : result instanceof Namespace ? ( ( Namespace ) result ).getString() : result ) + "\"." );
		if ( ( result = Maps.findValue( classToClassAlias, value -> value == fromClass, Map.Entry::getValue ) ) != null )
			throw new ApplicationException.Error( "The fromClass is already specified as a toClass alias, consider mapping from \"" + ( ( Class ) result ).getName() + "\" class instead." );
		classToClassAlias.put( fromClass, toClass );
	}

	public static void addHook( @Nonnull Class<?> hookClass, @Nonnull String hookAction, @Nonnull Method hookMethod, @Nonnull Priority hookPriority )
	{
		synchronized ( hooks )
		{
			if ( hookAction.length() == 0 )
				hookAction = "default";
			if ( !Strs.isCamelCase( hookAction ) )
				throw new ApplicationException.Ignorable( "Hook name must be alphanumeric." );
			hooks.computeIfAbsent( hookClass, key -> new ArrayList<>() ).add( new HookRef( hookAction, hookMethod, hookPriority ) );
		}
	}

	public static Class<?> findSuperOrInterface( Class<?> fromClass )
	{
		// Block Object class
		if ( fromClass == Object.class )
			return null;

		// Check if the fromClass is present
		if ( mappings.containsKey( fromClass ) )
			return fromClass;

		// Check if the fromClass super is present
		Class<?> result = fromClass.getSuperclass();
		if ( result != null )
		{
			result = findSuperOrInterface( result );
			if ( result != null )
				return result;
		}

		// Scan interfaces, if supers failed
		for ( Class<?> interfaceClass : fromClass.getInterfaces() )
		{
			result = findSuperOrInterface( interfaceClass );
			if ( result != null )
				return result;
		}

		return null;
	}

	public static <T extends BaseApplication> T getApplication()
	{
		if ( isRunlevel( Runlevel.DISPOSED ) )
			throw new ApplicationException.Runtime( "The application has been DISPOSED!" );
		if ( app == null )
			throw new ApplicationException.Runtime( "The application instance has never been set!" );
		return ( T ) app;
	}

	public static Class<?> getClassToClassAlias( @Nonnull Class<?> fromClass )
	{
		// We make sure the fromClass isn't already a resolved alias - we will only do this once to prevent loop bugs.
		if ( Maps.findValue( classToClassAlias, value -> value == fromClass, Map.Entry::getValue ) != null )
			return null;
		return Maps.findKey( classToClassAlias, key -> key.isAssignableFrom( fromClass ), Map.Entry::getValue );
	}

	public static String getCurrentRunlevelReason()
	{
		return currentRunlevelReason;
	}

	public static String getGenericRunlevelReason( @Nonnull Runlevel runlevel )
	{
		String uuid = getApplication().uuid().toString(); // getApplication().getEnv().getString( "instance-id" ).orElse( null );

		if ( runlevel == Runlevel.RELOAD )
			return String.format( "Server \"%s\" is reloading. Be back soon. :D", uuid );
		else if ( runlevel == Runlevel.CRASHED )
			return String.format( "Server \"%s\" has crashed. Sorry about that. :(", uuid );
		else if ( runlevel == Runlevel.SHUTDOWN )
			return String.format( "Server \"%s\" is shutting down. Good bye! :|", uuid );
		else
			return "No reason provided.";
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

	public static void init()
	{
		if ( init )
			return;

		allElseFailClasses.add( UsersMemory.class );

		// The Hooks system is similar to events but much less feature rich, we highly recommend not using this feature unless you need to catch critical/early events.

		try
		{
			synchronized ( Foundation.class )
			{
				L.info( "Initializing Hooks" );
				reflections = new Reflections( new MethodAnnotationsScanner() );

				reflections.getMethodsAnnotatedWith( Hook.class ).forEach( hookMethod -> {
					Hook annotation = hookMethod.getAnnotation( Hook.class );
					if ( annotation == null )
						throw new ApplicationException.Ignorable( "That method must be annotated with Hook!" );
					if ( !Modifier.isStatic( hookMethod.getModifiers() ) )
						throw new ApplicationException.Ignorable( "That hook method is not static!" );
					Priority priority = annotation.priority();
					addHook( annotation.hookClass(), annotation.hookAction(), hookMethod, priority );
					Foundation.L.info( "%s    -> Discovered hook method \"%s#%s\" with class \"%s\" and action \"%s\" at priority \"%s\".", EnumColor.GRAY, hookMethod.getDeclaringClass().getName(), hookMethod.getName(), annotation.hookClass(), annotation.hookAction(), priority );
				} );
			}

			invokeHook( Foundation.class, HOOK_ACTION_INIT );
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

	public static void initProviders( @Nonnull Class<?> declaringClass )
	{
		for ( Method method : declaringClass.getMethods() )
			if ( method.isAnnotationPresent( ProvidesClass.class ) && Modifier.isStatic( method.getModifiers() ) )
			{
				ProvidesClass annotation = method.getAnnotation( ProvidesClass.class );
				L.info( "Bound class \"" + annotation.value().getName() + "\" to method \"" + declaringClass.getName() + "#" + method.getName() + "\"." );
				Map<Priority, Object> map = mappings.computeIfAbsent( annotation.value(), key -> new HashMap<>() );
				if ( map.containsKey( annotation.priority() ) )
					L.warning( "The mapping for class \"" + annotation.value() + "\" is already provided at priority \"" + annotation.priority() + "\", it will be overridden." );
				map.put( annotation.priority(), method );
			}

		for ( Field field : declaringClass.getFields() )
			if ( field.isAnnotationPresent( ProvidesClass.class ) && Modifier.isStatic( field.getModifiers() ) )
			{
				ProvidesClass annotation = field.getAnnotation( ProvidesClass.class );
				L.info( "Bound class \"" + annotation.value().getName() + "\" to field \"" + declaringClass.getName() + "#" + field.getName() + "\"." );
				Map<Priority, Object> map = mappings.computeIfAbsent( annotation.value(), key -> new HashMap<>() );
				if ( map.containsKey( annotation.priority() ) )
					L.warning( "The mapping for class \"" + annotation.value() + "\" is already provided at priority \"" + annotation.priority() + "\", it will be overridden." );
				map.put( annotation.priority(), field );
			}
	}

	public static void invokeHook( Class<?> hookClass, String hookAction, Object... hookArguments ) throws ApplicationException.Error
	{
		synchronized ( hooks )
		{
			Streams.forEachWithException( hooks.computeIfAbsent( hookClass, key -> new ArrayList<>() ).stream().filter( hookRef -> hookAction.equals( hookRef.getHookAction() ) ), hookRef -> {
				try
				{
					hookRef.invoke( hookArguments );
				}
				catch ( InvocationTargetException | IllegalAccessException e )
				{
					throw new ApplicationException.Error( "Encountered an exception while attempting to invoke hook.", e );
				}
			} );
		}
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

	public static <T> Voluntary<T> make( @Nonnull Class<?> fromClass ) throws ApplicationException.Error
	{
		return make( fromClass, new HashMap<>() );
	}

	public static <T> Voluntary<T> make( @Nonnull Class<?> fromClass, @Nonnull Map<String, ?> arguments ) throws ApplicationException.Error
	{
		Voluntary<T> result = Voluntary.empty();

		Class<?> classToClassAliasResult = getClassToClassAlias( fromClass );
		if ( classToClassAliasResult != null )
		{
			result = make( classToClassAliasResult, arguments );
			if ( result.isPresent() )
				return result;
		}

		/*Namespace classToNamespaceAliasResult = getClassToNamespaceAlias( fromClass );
		if ( classToNamespaceAliasResult != null )
			result = make( classToNamespaceAliasResult, arguments );*/

		Class<?> resultClass = findSuperOrInterface( fromClass );
		if ( resultClass == null )
			resultClass = fromClass;

		// If class is annotated as being singular, we try to find already instigated instances of it.
		boolean singular = resultClass.isAnnotationPresent( Singular.class );
		if ( singular && mappingsSingular.containsKey( resultClass ) )
			return Voluntary.of( ( T ) mappingsSingular.get( resultClass ) );

		List<Throwable> causes = new ArrayList<>();

		if ( mappings.containsKey( resultClass ) )
			for ( Object obj : mappings.get( resultClass ).values() )
				if ( !result.isPresent() )
					if ( obj instanceof Constructor )
						try
						{
							Constructor constructor = ( Constructor ) obj;
							if ( fromClass.isAssignableFrom( constructor.getDeclaringClass() ) )
								result = ( Voluntary<T> ) Voluntary.of( constructor.newInstance( resolveParameters( constructor.getParameters(), arguments ) ) );
							else
								L.warning( "The class type \"" + constructor.getDeclaringClass() + "\" for constructor \"" + constructor.getDeclaringClass().getName() + "#" + constructor.getName() + "\" is not assignable to class \"" + fromClass.getName() + "\"." );
						}
						catch ( IllegalAccessException | InvocationTargetException | InstantiationException e )
						{
							if ( e instanceof InvocationTargetException )
								causes.add( ( e ).getCause() );
							else
								causes.add( e );
						}
					else if ( obj instanceof Method )
						try
						{
							Method method = ( Method ) obj;
							if ( fromClass.isAssignableFrom( method.getReturnType() ) )
								result = ( Voluntary<T> ) Voluntary.of( method.invoke( null, resolveParameters( method.getParameters(), arguments ) ) );
							else
								L.warning( "The return type \"" + method.getReturnType() + "\" for method \"" + method.getDeclaringClass().getName() + "#" + method.getName() + "\" is not assignable to class \"" + fromClass.getName() + "\"." );
						}
						catch ( IllegalAccessException | InvocationTargetException e )
						{
							if ( e instanceof InvocationTargetException )
								causes.add( ( ( InvocationTargetException ) e ).getCause() );
							else
								causes.add( e );
						}
					else if ( obj instanceof Field )
						try
						{
							Field field = ( Field ) obj;
							if ( fromClass.isAssignableFrom( field.getType() ) )
								result = ( Voluntary<T> ) Voluntary.of( field.get( null ) );
							else
								L.warning( "The field type \"" + field.getType() + "\" for field \"" + field.getDeclaringClass().getName() + "#" + field.getName() + "\" is not assignable to class \"" + fromClass.getName() + "\"." );
						}
						catch ( IllegalAccessException e )
						{
							causes.add( e );
						}

		// We attempt to scan the allElseFailClasses for assignments which are usually super basic versions of their super class.
		if ( !result.isPresent() )
			for ( Class<?> cls : allElseFailClasses )
				if ( resultClass.isAssignableFrom( cls ) )
					for ( Constructor constructor : cls.getDeclaredConstructors() )
						if ( !result.isPresent() )
							try
							{
								result = ( Voluntary<T> ) Voluntary.of( constructor.newInstance( resolveParameters( constructor.getParameters(), arguments ) ) );
							}
							catch ( IllegalAccessException | InvocationTargetException | InstantiationException e )
							{
								if ( e instanceof InvocationTargetException )
									causes.add( ( e ).getCause() );
								else
									causes.add( e );
							}

		if ( !result.isPresent() )
			if ( causes.size() == 1 )
				throw causes.get( 0 ) instanceof ApplicationException.Error ? ( ApplicationException.Error ) causes.get( 0 ) : new ApplicationException.Error( causes.get( 0 ) );
			else if ( causes.size() > 0 )
				throw new MultipleException( causes.stream().map( exp -> exp instanceof ExceptionContext ? ( ExceptionContext ) exp : new ApplicationException.Error( exp ) ).collect( Collectors.toList() ) );

		if ( singular && result.isPresent() )
			mappingsSingular.put( resultClass, result.get() );

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

			entityNull = getUsers().createVirtualUser( nullUuid );
			entityRoot = getUsers().createVirtualUser( rootUuid );

			// entityNull = Exceptions.tryCatchOrNotPresent( () -> make( EntitySubject.class, Maps.builder( "uuid", nullUuid ).hashMap() ), exp -> exp instanceof ApplicationException.Error ? ( ApplicationException.Error ) exp : new ApplicationException.Error( "Failed to create the NULL Entity.", exp ) );
			// entityRoot = Exceptions.tryCatchOrNotPresent( () -> make( EntitySubject.class, Maps.builder( "uuid", rootUuid ).hashMap() ), exp -> exp instanceof ApplicationException.Error ? ( ApplicationException.Error ) exp : new ApplicationException.Error( "Failed to create the ROOT Entity.", exp ) );
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
			throw new ApplicationException.Error( "Not Implemented. Sorry!" );

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
			String depends = IO.resourceToString( "dependencies.txt" );
			if ( !Objs.isEmpty( depends ) ) // Will be null if the file does not exist
				for ( String depend : depends.split( "\n" ) )
					if ( !depend.startsWith( "#" ) )
						Libraries.loadLibrary( new MavenReference( "builtin", depend ) );
		}
		catch ( IOException e )
		{
			throw new StartupException( "Failed to read the built-in dependencies file.", e );
		}
		L.info( EnumColor.AQUA + "Finished downloading deployment libraries." );

		// Call to make sure the INITIALIZATION runlevel is acknowledged by the application.
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

	public static Object[] resolveParameters( @Nonnull Parameter[] parameters ) throws ApplicationException.Error
	{
		return resolveParameters( parameters, new HashMap<>() );
	}

	/**
	 * Attempts to resolve the provided parameters in order provided.
	 */
	public static Object[] resolveParameters( @Nonnull Parameter[] parameters, @Nonnull Map<String, ?> arguments ) throws ApplicationException.Error
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
				throw new ApplicationException.Error( "We failed to resolve the object for parameter " + parameter.getName() + " and the Nullable annotation was not present." );
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

	private synchronized static void setRunlevel0( @Nonnull Runlevel runlevel, @Nullable String reason )
	{
		try
		{
			if ( reason == null || reason.length() == 0 )
				reason = getGenericRunlevelReason( runlevel );

			if ( LooperRouter.getMainLooper().isThreadJoined() && !LooperRouter.getMainLooper().isHeldByCurrentThread() )
				throw new ApplicationException.Error( "Runlevel can only be set from the main looper thread. Be more careful next time." );
			if ( currentRunlevel == runlevel )
			{
				L.warning( "Runlevel is already set to \"" + runlevel.name() + "\". This might be a severe race bug." );
				return;
			}
			if ( !runlevel.checkRunlevelOrder( currentRunlevel ) )
				throw new ApplicationException.Error( "RunLevel \"" + runlevel.name() + "\" was set out of order. Present runlevel was \"" + currentRunlevel.name() + "\". This is potentially a race bug or there were exceptions thrown." );

			Timing.start( runlevelTimingObject );

			previousRunlevel = currentRunlevel;
			currentRunlevel = runlevel;
			currentRunlevelReason = reason;

			if ( runlevel == Runlevel.RELOAD || runlevel == Runlevel.SHUTDOWN || runlevel == Runlevel.CRASHED )
				L.info( EnumColor.join( EnumColor.GOLD ) + "Application is entering runlevel \"" + runlevel.name() + "\", for reason: " + reason + "." );

			onRunlevelChange();

			if ( currentRunlevel == Runlevel.DISPOSED )
				L.info( EnumColor.join( EnumColor.GOLD ) + "Application has successfully shutdown! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
			else if ( currentRunlevel == Runlevel.STARTED )
				L.info( EnumColor.join( EnumColor.GOLD ) + "Application has successfully started! It took " + Timing.finish( runlevelTimingObject ) + "ms!" );
			else
				L.info( EnumColor.AQUA + "Application has entered runlevel \"" + runlevel.name() + "\". It took " + Timing.finish( runlevelTimingObject ) + "ms!" );

			if ( runlevel == Runlevel.CRASHED )
				throw new FoundationCrashException();
		}
		catch ( ApplicationException.Error e )
		{
			if ( runlevel == Runlevel.CRASHED )
				throw new ApplicationException.Runtime( e );
			ExceptionReport.handleSingleException( e );
		}
	}

	public static void setRunlevelLater( @Nonnull Runlevel runlevel )
	{
		setRunlevelLater( runlevel, null );
	}

	public static void setRunlevelLater( @Nonnull Runlevel runlevel, @Nullable String reason )
	{
		if ( reason == null || reason.length() == 0 )
			setRunlevelLater( runlevel, getGenericRunlevelReason( runlevel ) );
		else
			LooperRouter.getMainLooper().postTask( entry -> setRunlevel0( runlevel, reason ) );
	}

	public static void shutdown( String reason )
	{
		try
		{
			setRunlevel( Runlevel.SHUTDOWN, reason );
		}
		catch ( LooperException.InvalidState e )
		{
			// L.warning( "shutdown() called but ignored because MainLooper is already quitting. {shutdownReason=" + reason + "}" );
			// TEMP?
			e.printStackTrace();
			System.exit( 1 );
		}
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

	private static class HookRef implements Comparable<HookRef>
	{
		private final String hookAction;
		private final Method hookMethod;
		private final Priority hookPriority;
		private final Parameter[] parameters;

		HookRef( @Nonnull String hookAction, @Nonnull Method hookMethod, @Nonnull Priority hookPriority )
		{
			this.hookAction = hookAction;
			this.hookMethod = hookMethod;
			this.hookPriority = hookPriority;
			this.parameters = hookMethod.getParameters();
		}

		@Override
		public int compareTo( @Nonnull HookRef other )
		{
			return Integer.compare( hookPriority.intValue(), other.hookPriority.intValue() );
		}

		public String getHookAction()
		{
			return hookAction;
		}

		Priority getHookPriority()
		{
			return hookPriority;
		}

		void invoke( Object... arguments ) throws InvocationTargetException, IllegalAccessException, ApplicationException.Error
		{
			L.info( "%s    -> Invoking hook \"%s#%s\" at priority \"%s\"", EnumColor.GRAY, hookMethod.getDeclaringClass().getName(), hookMethod.getName(), hookPriority );

			if ( arguments.length != parameters.length )
				throw new ApplicationException.Error( "Parameter count does not match the provided argument count." );

			for ( int i = 0; i < parameters.length; i++ )
				if ( !parameters[i].getType().isAssignableFrom( arguments[i].getClass() ) )
					throw new ApplicationException.Error( "Parameter type " + parameters[i].getType().getSimpleName() + " does not match the provided argument type " + arguments[i].getClass().getSimpleName() + "." );

			// TODO Skip failed hook calls.
			// TODO Save invoked parameters to hook getUserContext as they're likely the best source we got, this will govern all future hooks and invokes.

			hookMethod.invoke( null, arguments );
		}
	}
}
