/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.hooks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import io.amelia.lang.ApplicationException;
import io.amelia.support.Namespace;
import io.amelia.support.Priority;

public class HookRef implements Comparable<HookRef>
{
	private final Parameter[] parameters;
	private Method method;
	private Namespace namespace;
	private Priority priority;

	public HookRef( Method method )
	{
		if ( !Modifier.isStatic( method.getModifiers() ) )
			throw new ApplicationException.Ignorable( "That method is not static!" );
		Hook annotation = method.getAnnotation( Hook.class );
		if ( annotation == null )
			throw new ApplicationException.Ignorable( "That method must be annotated with Hook!" );
		this.namespace = Namespace.of( annotation.ns() );
		// if ( this.namespace.getNodeCount() < 3 )
		//	throw new ApplicationException.Ignorable( "That namespace can't be less than three nodes in size! " + annotation.ns() );
		this.method = method;
		this.priority = annotation.priority();
		this.parameters = method.getParameters();
	}

	@Override
	public int compareTo( HookRef other )
	{
		return Integer.compare( priority.intValue(), other.priority.intValue() );
	}

	public Namespace getNamespace()
	{
		return namespace;
	}

	public Priority getPriority()
	{
		return priority;
	}

	public void invoke( Object... arguments )
	{
		if ( arguments.length != parameters.length )
			throw new ApplicationException.Ignorable( "Parameter count does not match the provided argument count." );

		for ( int i = 0; i < parameters.length; i++ )
			if ( !parameters[i].getType().isAssignableFrom( arguments[i].getClass() ) )
				throw new ApplicationException.Ignorable( "Parameter type " + parameters[i].getType().getSimpleName() + " does not match the provided argument type " + arguments[i].getClass().getSimpleName() + "." );

		// TODO Skip failed hook calls.
		// TODO Save invoked parameters to hook getUserContext as they're likely the best source we got, this will govern all future hooks and invokes.

		try
		{
			method.invoke( null, arguments );
		}
		catch ( IllegalAccessException | InvocationTargetException e )
		{
			throw new ApplicationException.Ignorable( e );
		}
	}
}
