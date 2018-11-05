/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.amelia.support.Objs;

/**
 * Sits as an interface between GroovyShell and Interpreters
 */
public class StackFactory
{
	Map<String, ScriptingContext> scriptStack = Maps.newLinkedHashMap();
	Map<String, ScriptingContext> scriptStackHistory = Maps.newLinkedHashMap();

	public List<ScriptTraceElement> examineStackTrace( StackTraceElement[] stackTrace )
	{
		Objs.notNull( stackTrace );

		List<ScriptTraceElement> scriptTrace = Lists.newLinkedList();

		for ( StackTraceElement ste : stackTrace )
			if ( ste.getFileName() != null && scriptStackHistory.containsKey( ste.getFileName() ) )
				scriptTrace.add( new ScriptTraceElement( scriptStackHistory.get( ste.getFileName() ), ste ) );

		ScriptingContext context = scriptStack.values().toArray( new ScriptingContext[0] )[scriptStack.size() - 1];
		if ( context != null )
		{
			boolean contains = false;
			for ( ScriptTraceElement ste : scriptTrace )
				if ( ste.context().getFileName().equals( context.getFileName() ) )
					contains = true;
			if ( !contains )
				scriptTrace.add( 0, new ScriptTraceElement( context, "" ) );
		}

		return scriptTrace;
	}

	public Map<String, ScriptingContext> getScriptTrace()
	{
		return Collections.unmodifiableMap( scriptStack );
	}

	public Map<String, ScriptingContext> getScriptTraceHistory()
	{
		return Collections.unmodifiableMap( scriptStackHistory );
	}

	public void stack( String scriptName, ScriptingContext context )
	{
		scriptStackHistory.put( scriptName, context );
		scriptStack.put( scriptName, context );
	}

	/**
	 * Removes the last stacked {@link ScriptingContext} from the stack
	 */
	public void unstack()
	{
		if ( scriptStack.size() == 0 )
			return;
		String[] keys = scriptStack.keySet().toArray( new String[0] );
		scriptStack.remove( keys[keys.length - 1] );
	}
}
