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

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.scripting.ScriptingProcessor;

public class LessProcessor implements ScriptingProcessor
{
	@Override
	public void postEvaluate( ScriptingContext scriptingContext )
	{

	}

	@Override
	public void preEvaluate( ScriptingContext scriptingContext )
	{
		if ( !scriptingContext.getContentType().equals( "stylesheet/less" ) || !scriptingContext.getShell().equals( "less" ) )
			return;

		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream( "less-rhino-1.7.4.js" );

		try
		{
			Reader reader = new InputStreamReader( inputStream, "UTF-8" );

			Context context = Context.enter();
			context.setOptimizationLevel( -1 ); // Without this, Rhino hits a 64K bytecode limit and fails

			try
			{
				ScriptableObject globalScope = context.initStandardObjects();

				context.evaluateReader( globalScope, reader, "less-rhino-1.7.4.js", 0, null );

				Scriptable compileScope = context.newObject( globalScope );
				compileScope.setParentScope( globalScope );
				compileScope.put( "lessSource", compileScope, scriptingContext.readString() );

				String filename = "dummyFile.less";

				if ( scriptingContext.getFileName() != null && !scriptingContext.getFileName().isEmpty() )
					filename = new File( scriptingContext.getFileName() ).getName();

				/*
				 * try
				 * {
				 * code = new LessImportParser().runParser( code, new File( fields.fileName ).getParentFile().getAbsoluteFile() );
				 * }
				 * catch ( ShellExecuteException e )
				 * {
				 * e.printStackTrace();
				 * }
				 */

				Map<String, Object> compilerOptions = Maps.newHashMap();

				compilerOptions.put( "filename", filename );
				compilerOptions.put( "compress", true );

				String json = new GsonBuilder().create().toJson( compilerOptions );

				context.evaluateString( compileScope, "var parser = new less.Parser(" + json + ");", "less2css.js", 0, null );

				// String script = "parser.parse(lessSource, function (e, tree) { source = 'Hello World'; } );";

				// Loader.getLogger().debug( "" + getScriptingContext.evaluateString( compileScope, script, "less2css.js", 0, null ) );

				// Loader.getLogger().debug( "" + globalScope.get( "source" ) );

				if ( globalScope.get( "source" ) != null && globalScope.get( "source" ) instanceof String )
					scriptingContext.resetAndWrite( ( String ) globalScope.get( "source" ) );
				else if ( globalScope.get( "source" ) != null )
					ScriptingFactory.L.warning( "We did not get what we expected back from Less.js: " + globalScope.get( "source" ) );
			}
			finally
			{
				reader.close();
				Context.exit();
			}
		}
		catch ( JavaScriptException | IOException e )
		{
			e.printStackTrace();
		}
	}
}
