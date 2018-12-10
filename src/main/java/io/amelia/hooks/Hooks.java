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

import org.reflections.Reflections;

import java.util.Set;
import java.util.TreeSet;

import io.amelia.support.Namespace;

public class Hooks
{
	private static final Reflections reflections = new Reflections();
	private static volatile Set<HookRef> HOOK_REFS = new TreeSet<>();

	static
	{
		refreshHooks();
	}

	public static synchronized void invoke( String namespace, Object... arguments )
	{
		Namespace ns = Namespace.of( namespace );
		if ( ns.getNodeCount() < 3 )
			throw new RuntimeException( "That namespace can't be less than three nodes in size! " + ns );
		// This should be all it takes to find a specific hook and invoke them in the priority order.
		HOOK_REFS.stream().filter( hookRef -> hookRef.getNamespace().startsWith( ns ) ).forEach( hookRef -> hookRef.invoke( arguments ) );
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
