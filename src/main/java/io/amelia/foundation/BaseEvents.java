package io.amelia.foundation;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import io.amelia.events.AbstractEvent;
import io.amelia.events.EventException;
import io.amelia.support.ConsumerWithException;
import io.amelia.support.Priority;

public interface BaseEvents
{
	Kernel.Logger L = Kernel.getLogger( BaseEvents.class );

	<T extends AbstractEvent> T callEvent( @Nonnull T event );

	<T extends AbstractEvent> T callEventWithException( @Nonnull T event ) throws EventException.Error;

	void listen( @Nonnull RegistrarBase registrar, @Nonnull Object listener, @Nonnull Method method ) throws EventException.Error;

	void listen( @Nonnull RegistrarBase registrar, @Nonnull Object listener );

	<E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener );

	<E extends AbstractEvent> void listen( @Nonnull RegistrarBase registrar, @Nonnull Priority priority, @Nonnull Class<E> event, @Nonnull ConsumerWithException<E, EventException.Error> listener );

	void unregisterEvents( @Nonnull RegistrarBase registrar );
}
