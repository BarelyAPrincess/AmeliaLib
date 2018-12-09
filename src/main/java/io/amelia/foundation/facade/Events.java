/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.facade;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import io.amelia.events.AbstractEvent;
import io.amelia.events.EventException;
import io.amelia.events.EventHandlers;
import io.amelia.events.EventPriority;
import io.amelia.foundation.RegistrarBase;
import io.amelia.foundation.bindings.BindingException;
import io.amelia.foundation.bindings.Bindings;
import io.amelia.foundation.impl.EventsImpl;
import io.amelia.support.ConsumerWithException;

public class Events
{
	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 */
	public static <T extends AbstractEvent> T callEvent( @Nonnull T event )
	{
		return getInstance().callEvent( event );
	}

	/**
	 * Calls an event with the given details.<br>
	 * This method only synchronizes when the event is not asynchronous.
	 *
	 * @param event Event details
	 *
	 * @throws EventException.Error Thrown if you try to call an async event on a sync thread
	 */
	public static <T extends AbstractEvent> T callEventWithException( @Nonnull T event ) throws EventException.Error
	{
		return getInstance().callEventWithException( event );
	}

	public static void fireAuthorNag( @Nonnull RegistrarBase registrarBase, @Nonnull String message )
	{
		getInstance().fireAuthorNag( registrarBase, message );
	}

	private static void fireEvent( @Nonnull AbstractEvent event ) throws EventException.Error
	{
		getInstance().fireEvent( event );
	}

	private static EventHandlers getEventListeners( @Nonnull Class<? extends AbstractEvent> event )
	{
		return getInstance().getEventListeners( event );
	}

	public static EventsImpl getInstance()
	{
		return Bindings.resolveClassOrFail( EventsImpl.class, () -> new BindingException.Ignorable( "The Events Subsystem is not loaded. This is either an application or initialization bug." ) );
	}

	private static void listen( final RegistrarBase registrar, final Object listener, final Method method ) throws EventException.Error
	{
		getInstance().listen( registrar, listener, method );
	}

	public static void listen( @Nonnull final RegistrarBase registrar, @Nonnull final Object listener )
	{
		getInstance().listen( registrar, listener );
	}

	public static <E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener )
	{
		getInstance().listen( registrar, event, listener );
	}

	/**
	 * Registers the given event to the specified listener using a directly passed EventExecutor
	 *
	 * @param registrar Registrar of event registration
	 * @param priority  Priority of this event
	 * @param event     Event class to register
	 * @param listener  Consumer that will receive the event
	 */
	public static <E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull EventPriority priority, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener )
	{
		getInstance().listen( registrar, priority, event, listener );
	}

	public static void unregisterEvents( @Nonnull RegistrarBase registrar )
	{
		getInstance().unregisterEvents( registrar );
	}

	private Events()
	{
		// Static
	}
}
