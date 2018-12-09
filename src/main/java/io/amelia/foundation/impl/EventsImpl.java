package io.amelia.foundation.impl;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import io.amelia.events.AbstractEvent;
import io.amelia.events.EventException;
import io.amelia.events.EventHandlers;
import io.amelia.events.EventPriority;
import io.amelia.foundation.RegistrarBase;
import io.amelia.support.ConsumerWithException;

public interface EventsImpl
{
	<T extends AbstractEvent> T callEvent( @Nonnull T event );

	<T extends AbstractEvent> T callEventWithException( @Nonnull T event ) throws EventException.Error;

	void fireAuthorNag( @Nonnull RegistrarBase registrarBase, @Nonnull String message );

	void fireEvent( @Nonnull AbstractEvent event ) throws EventException.Error;

	EventHandlers getEventListeners( @Nonnull Class<? extends AbstractEvent> event );

	void listen( @Nonnull RegistrarBase registrar, @Nonnull Object listener, @Nonnull Method method ) throws EventException.Error;

	void listen( @Nonnull RegistrarBase registrar, @Nonnull Object listener );

	<E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener );

	<E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull EventPriority priority, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener );

	abstract void unregisterEvents( @Nonnull RegistrarBase registrar );
}
