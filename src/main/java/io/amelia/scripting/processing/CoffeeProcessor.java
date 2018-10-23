/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
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

import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingProcessor;

public class CoffeeProcessor implements ScriptingProcessor
{
	@Override
	public void postEvaluate( ScriptingContext scriptingContext )
	{

	}

	@Override
	public void preEvaluate( ScriptingContext scriptingContext )
	{
		if ( !scriptingContext.getContentType().endsWith( "coffee" ) && !scriptingContext.getContentType().endsWith( "litcoffee" ) && !scriptingContext.getContentType().endsWith( "coffee.md" ) )
			return;

		/*
		 * coffeescript.js must be updated from the git repository.
		 * You must first install NodeJS and do the following:
		 * > git clone https://github.com/jashkenas/coffee-script.git
		 * > cd coffee-script
		 * > npm install uglify-js @babel/core @babel/preset-env babel-preset-minify
		 * > ./bin/cake build:browser
		 * The compiled js file will be at `/docs/browser-compiler/coffeescript.js` as of v2.3.2.
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
				compileScope.put( "coffeeScriptSource", compileScope, scriptingContext.readString() );

				scriptingContext.resetAndWrite( ( ( String ) context.evaluateString( compileScope, String.format( "CoffeeScript.compile(coffeeScriptSource, %s);", String.format( "{bare: %s, filename: '%s'}", true, scriptingContext.getFileName() ) ), "CoffeeScriptCompiler-" + scriptingContext.getFileName(), 0, null ) ).getBytes() );
			}
			finally
			{
				reader.close();
				Context.exit();
			}
		}
		catch ( IOException | JavaScriptException e )
		{
		}
	}
}
