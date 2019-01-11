/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.hooks;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.util.Set;
import java.util.TreeSet;

import io.amelia.foundation.Foundation;
import io.amelia.lang.ApplicationException;
import io.amelia.support.EnumColor;
import io.amelia.support.Namespace;
import io.amelia.support.Streams;

public class Hooks
{
	// TODO Cache annotated methods
	private static final Reflections reflections = new Reflections( new MethodAnnotationsScanner() );
	private static volatile Set<HookRef> HOOK_REFS = new TreeSet<>();

	static
	{
		refreshHooks();
	}

	public static synchronized void invoke( String namespace, Object... arguments ) throws ApplicationException.Error
	{
		Namespace ns = Namespace.of( namespace );
		if ( ns.getNodeCount() < 3 )
			throw new RuntimeException( "That namespace can't be less than three nodes in size! " + ns );
		// This should be all it takes to find a specific hook and invoke them in the priority order.
		Foundation.L.info( "%sAttempting to invoke namespace \"%s\": ", EnumColor.RED, namespace );
		Streams.forEachWithException( HOOK_REFS.stream().filter( hookRef -> hookRef.getNamespace().startsWith( ns ) ), hookRef -> {
			Foundation.L.info( "%s       Found hook \"%s#%s\" at priority \"%s\"", EnumColor.RED, hookRef.method.getDeclaringClass().getName(), hookRef.method.getName(), hookRef.priority );
			hookRef.invoke( arguments );
		} );
	}

	public static void refreshHooks()
	{
		synchronized ( HOOK_REFS )
		{
			HOOK_REFS.clear();
			reflections.getMethodsAnnotatedWith( Hook.class ).forEach( method -> HOOK_REFS.add( new HookRef( method ) ) );
		}

	}

	public Hooks()
	{
		// Static
	}
}
