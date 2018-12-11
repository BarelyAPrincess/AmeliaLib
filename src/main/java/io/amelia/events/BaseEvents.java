/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.foundation.RegistrarBase;
import io.amelia.lang.DeprecatedDetail;
import io.amelia.lang.ReportingLevel;
import io.amelia.support.ConsumerWithException;
import io.amelia.support.Objs;
import io.amelia.support.Priority;

public class BaseEvents
{
	public static final Kernel.Logger L = Kernel.getLogger( BaseEvents.class );

	private Map<Class<? extends AbstractEvent>, EventHandlers> handlers = new ConcurrentHashMap<>();
	private Object lock = new Object();

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 */
	public <T extends AbstractEvent> T callEvent( @Nonnull T event )
	{
		try
		{
			return callEventWithException( event );
		}
		catch ( EventException.Error ex )
		{
			// Ignore
		}

		return event;
	}

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 *
	 * @throws EventException.Error Thrown if you try to call an async event on a sync thread
	 */
	public <T extends AbstractEvent> T callEventWithException( @Nonnull T event ) throws EventException.Error
	{
		if ( event.isAsynchronous() )
		{
			if ( Thread.holdsLock( lock ) )
				throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from inside synchronized code." );
			if ( Kernel.getKernelHandler().isPrimaryThread() )
				throw new IllegalStateException( event.getEventName() + " cannot be triggered asynchronously from primary server thread." );
			fireEvent( event );
		}
		else
			synchronized ( lock )
			{
				fireEvent( event );
			}

		return event;
	}

	public void fireAuthorNag( @Nonnull RegistrarBase registrarBase, @Nonnull String message )
	{
		callEvent( new AuthorNagEvent( registrarBase, message ) );

		/* Events.listen( Foundation.getApplication(), ApplicationEvent.class, ( e ) -> {

		} ); */
	}

	public void fireEvent( @Nonnull AbstractEvent event ) throws EventException.Error
	{
		event.onEventPreCall();

		for ( RegisteredListener registration : getEventListeners( event.getClass() ) )
		{
			if ( !registration.getRegistrar().isEnabled() )
				continue;

			// TODO Future implementation; report exceptions to plugin developers.
			try
			{
				registration.callEvent( event );
			}
			catch ( EventException.Error ex )
			{
				if ( ex.getCause() == null )
				{
					ex.printStackTrace();
					L.severe( "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName() + "\nEvent Exception Reason: " + ex.getMessage() );
				}
				else
				{
					ex.getCause().printStackTrace();
					L.severe( "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName() + "\nEvent Exception Reason: " + ex.getCause().getMessage() );
				}
				throw ex;
			}
			catch ( Throwable ex )
			{
				L.severe( "Could not pass event " + event.getEventName() + " to " + registration.getRegistrar().getName(), ex );
			}
		}

		event.onEventPostCall();
	}

	public EventHandlers getEventListeners( @Nonnull Class<? extends AbstractEvent> event )
	{
		EventHandlers eventHandlers = handlers.get( event );

		if ( eventHandlers == null )
		{
			eventHandlers = new EventHandlers();
			handlers.put( event, eventHandlers );
		}

		return eventHandlers;
	}

	public void listen( @Nonnull final RegistrarBase registrar, @Nonnull final Object listener, @Nonnull final Method method ) throws EventException.Error
	{
		final EventHandler eventHandler = method.getAnnotation( EventHandler.class );
		if ( eventHandler == null )
			return;

		final Class<?> checkClass;
		if ( method.getParameterTypes().length != 1 || !AbstractEvent.class.isAssignableFrom( checkClass = method.getParameterTypes()[0] ) )
			throw new EventException.Error( "The EventHandler method signature \"" + method.toGenericString() + "\" in \"" + listener.getClass() + "\" is invalid. It must has at least one argument with an event that extends AbstractEvent." );

		final Class<? extends AbstractEvent> eventClass = checkClass.asSubclass( AbstractEvent.class );
		method.setAccessible( true );

		if ( ReportingLevel.E_DEPRECATED.isEnabled() )
			for ( Class<?> clazz = eventClass; AbstractEvent.class.isAssignableFrom( clazz ); clazz = clazz.getSuperclass() )
			{
				if ( clazz.isAnnotationPresent( DeprecatedDetail.class ) )
				{
					DeprecatedDetail deprecated = clazz.getAnnotation( DeprecatedDetail.class );

					L.warning( String.format( "The creator '%s' has registered a listener for %s on method '%s', but the event is Deprecated for reason '%s'.", registrar.getName(), clazz.getName(), method.toGenericString(), deprecated.reason() ) );
					break;
				}

				if ( clazz.isAnnotationPresent( Deprecated.class ) )
				{
					L.warning( String.format( "The creator '%s' has registered a listener for %s on method '%s', but the event is Deprecated!", registrar.getName(), clazz.getName(), method.toGenericString() ) );
					break;
				}
			}

		listen( registrar, eventHandler.priority(), eventClass, event -> {
			try
			{
				if ( !eventClass.isAssignableFrom( event.getClass() ) )
					return;
				method.invoke( listener, event );
			}
			catch ( InvocationTargetException ex )
			{
				throw new EventException.Error( ex.getCause() );
			}
			catch ( Throwable t )
			{
				throw new EventException.Error( t );
			}
		} );
	}

	public void listen( @Nonnull final RegistrarBase registrar, @Nonnull final Object listener )
	{
		Objs.notNull( registrar, "Registrar can not be null" );
		Objs.notNull( listener, "Listener can not be null" );

		try
		{
			Set<Method> methods = new HashSet<>();
			methods.addAll( Arrays.asList( listener.getClass().getMethods() ) );
			methods.addAll( Arrays.asList( listener.getClass().getDeclaredMethods() ) );
			for ( Method method : methods )
				listen( registrar, listener, method );
		}
		catch ( NoClassDefFoundError e )
		{
			// TODO Does this need better handling? Shouldn't it pass to the caller in some form?
			L.severe( String.format( "%s has failed to register events for %s because %s does not exist.", registrar.getName(), listener.getClass(), e.getMessage() ) );
		}
		catch ( EventException.Error e )
		{
			L.severe( e.getMessage() );
		}
	}

	public <E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener )
	{
		listen( registrar, Priority.NORMAL, event, listener );
	}

	/**
	 * Registers the given event to the specified listener using a directly passed EventExecutor
	 *
	 * @param registrar Registrar of event registration
	 * @param priority  Priority of this event
	 * @param event     Event class to register
	 * @param listener  Consumer that will receive the event
	 */
	public <E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull Priority priority, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener )
	{
		getEventListeners( event ).register( new RegisteredListener<>( registrar, priority, listener ) );
	}

	public void unregisterEvents( @Nonnull RegistrarBase registrar )
	{
		EventHandlers.unregisterAll( registrar );
	}
}
