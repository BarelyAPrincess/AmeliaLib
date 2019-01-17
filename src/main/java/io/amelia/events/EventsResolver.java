package io.amelia.events;

import io.amelia.bindings.BindingResolver;
import io.amelia.bindings.BindingsException;
import io.amelia.bindings.ProvidesClass;
import io.amelia.bindings.Singular;
import io.amelia.support.Priority;

public class EventsResolver extends BindingResolver
{
	private Events eventsInstance = null;

	public EventsResolver() throws BindingsException.Error
	{
		super( Priority.NORMAL );

		// addAlias( Events.class, "events" );
	}

	@Singular
	@ProvidesClass( Events.class )
	public Events events()
	{
		if ( eventsInstance == null )
			eventsInstance = new Events();
		return eventsInstance;
	}
}
