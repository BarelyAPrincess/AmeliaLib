/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.processing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import io.amelia.foundation.events.EventHandler;
import io.amelia.scripting.event.PreEvalEvent;

public class PreCoffeeProcessor
{
	@EventHandler
	public void onEvent( PreEvalEvent event )
	{
		if ( !event.getScriptingContext().getContentType().endsWith( "coffee" ) && !event.getScriptingContext().getContentType().endsWith( "litcoffee" ) && !event.getScriptingContext().getContentType().endsWith( "coffee.md" ) )
			return;

		/*
		 * coffeescript.js must be updated from the git repository.
		 * You must first install NodeJS and do the following:
		 * > git clone https://github.com/jashkenas/coffee-script.git
		 * > cd coffee-script
		 * > npm install uglify-js
		 * > ./bin/cake build:browser
		 * The compiled js file will be at `/docs/browser-compiler/coffeescript.js` as of v2.2.4.
		 */
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream( "coffeescript.js" );

		try
		{
			Reader reader = new InputStreamReader( inputStream, "UTF-8" );

			Context context = Context.enter();
			context.setOptimizationLevel( -1 ); // Without this, Rhino hits a 64K bytecode limit and fails

			try
			{
				Scriptable globalScope = context.initStandardObjects();
				context.evaluateReader( globalScope, reader, "coffeescript.js", 0, null );

				Scriptable compileScope = context.newObject( globalScope );
				compileScope.setParentScope( globalScope );
				compileScope.put( "coffeeScriptSource", compileScope, event.getScriptingContext().readString() );

				event.getScriptingContext().resetAndWrite( ( ( String ) context.evaluateString( compileScope, String.format( "CoffeeScript.compile(coffeeScriptSource, %s);", String.format( "{bare: %s, filename: '%s'}", true, event.getScriptingContext().getFileName() ) ), "CoffeeScriptCompiler-" + event.getScriptingContext().getFileName(), 0, null ) ).getBytes() );
			}
			finally
			{
				reader.close();
				Context.exit();
			}
		}
		catch ( JavaScriptException e )
		{
			return;
		}
		catch ( IOException e )
		{
			return;
		}
	}
}
