/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.groovy;

import com.google.common.collect.Maps;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.lang.MissingMethodException;
import groovy.lang.Script;
import groovy.transform.TimedInterrupt;
import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ExceptionCallback;
import io.amelia.lang.ExceptionRegistrar;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.SandboxSecurityException;
import io.amelia.lang.ScriptingException;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingEngine;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.scripting.ScriptingRegistry;
import io.amelia.support.Objs;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handles the registry of the Groovy related scripting language
 */
public class GroovyRegistry implements ScriptingRegistry
{
	private static final Class<?>[] classImports = new Class<?>[] {References.class, NestedScript.class, ServerLoader.class, AccountManager.class, AccountType.class, Account.class, AccountAuthenticator.class, EventDispatcher.class, PermissionDispatcher.class, PluginManager.class, TaskManager.class, Ticks.class, Timings.class, SessionModule.class, SiteModule.class, Site.class, ScriptingContext.class, Versioning.class};
	/*
	 * Groovy Imports :P
	 */
	private static final GroovyImportCustomizer imports = new GroovyImportCustomizer();
	private static final GroovySandbox secure = new GroovySandbox();
	private static final String[] starImports = new String[] {"com.chiorichan.lang", "com.chiorichan.helpers", "com.chiorichan.factory.api", "com.chiorichan.utils", "com.chiorichan.logger", "org.apache.commons.lang3.text", "org.ocpsoft.prettytime", "java.utils", "java.net", "com.google.common.base"};
	private static final Class<?>[] staticImports = new Class<?>[] {Looper.class, ReportingLevel.class, HttpResponseStatus.class};
	/*
	 * Groovy Sandbox Customization
	 */
	private static final ASTTransformationCustomizer timedInterrupt = new ASTTransformationCustomizer( TimedInterrupt.class );
	private static Map<String, String> scriptCacheMd5 = new HashMap<>();

	static
	{
		imports.addImports( classImports );
		imports.addStarImports( starImports );
		imports.addStaticStars( staticImports );

		// Transforms scripts to limit their execution to 30 seconds.
		long timeout = ConfigRegistry.config.getValue( Config.DEFAULT_SCRIPT_TIMEOUT );
		if ( timeout > 0 )
		{
			Map<String, Object> timedInterruptParams = Maps.newHashMap();
			timedInterruptParams.put( "value", timeout );
			timedInterrupt.setAnnotationParameters( timedInterruptParams );
		}
	}

	public static Script getCachedScript( ScriptingContext context, Binding binding )
	{
		try
		{
			if ( scriptCacheMd5.containsKey( context.site().getId() + "//" + context.getScriptClassName() ) && scriptCacheMd5.get( context.site().getId() + "//" + context.getScriptClassName() ).equals( context.md5Hash() ) )
			{
				Class<?> scriptClass = Class.forName( context.getScriptClassName() );
				Constructor<?> con = scriptClass.getConstructor( Binding.class );
				Script script = ( Script ) con.newInstance( binding );
				return script;
			}
			else
				scriptCacheMd5.put( context.site().getId() + "//" + context.getScriptClassName(), context.md5Hash() );
		}
		catch ( Throwable t )
		{
			// Ignore
		}
		return null;
	}

	/**
	 * {@link TimeoutException} is thrown when a script does not exit within an alloted amount of time.<br>
	 * {@link MissingMethodException} is thrown when a groovy script tries to call a non-existent method<br>
	 * {@link SyntaxException} is for when the user makes a syntax coding error<br>
	 * {@link CompilationFailedException} is for when compilation fails from source errors<br>
	 * {@link SandboxSecurityException} thrown when script attempts to access a blacklisted API<br>
	 * {@link GroovyRuntimeException} thrown for basically all remaining Groovy exceptions not caught above
	 */
	public GroovyRegistry()
	{
		// if ( Loader.getConfig().getBoolean( "advanced.scripting.gspEnabled", true ) )
		// EvalFactory.register( new EmbeddedGroovyScriptProcessor() );
		// if ( Loader.getConfig().getBoolean( "advanced.scripting.groovyEnabled", true ) )
		// EvalFactory.register( new GroovyScriptProcessor() );

		ScriptingFactory.register( this );

		ExceptionReport.registerException( new ExceptionCallback()
		{
			@Override
			public ReportingLevel callback( Throwable cause, ExceptionReport report, ExceptionRegistrar registrar )
			{
				MultipleCompilationErrorsException exp = ( MultipleCompilationErrorsException ) cause;
				ErrorCollector e = exp.getErrorCollector();
				boolean abort = false;

				for ( Object errors : e.getErrors() )
					if ( errors instanceof Throwable )
					{
						if ( report.handleException( ( Throwable ) errors, registrar ) )
							abort = true;
					}
					else if ( errors instanceof ExceptionMessage )
					{
						if ( report.handleException( ( ( ExceptionMessage ) errors ).getCause(), registrar ) )
							abort = true;
					}
					else if ( errors instanceof SyntaxErrorMessage )
					{
						report.handleException( ( ( SyntaxErrorMessage ) errors ).getCause(), registrar );
						abort = true;
					}
					else if ( errors instanceof Message )
					{
						StringWriter writer = new StringWriter();
						( ( Message ) errors ).write( new PrintWriter( writer, true ) );
						report.handleException( new ScriptingException( ReportingLevel.E_NOTICE, writer.toString() ), context );
					}
				return abort ? ReportingLevel.E_ERROR : ReportingLevel.E_IGNORABLE;
			}
		}, MultipleCompilationErrorsException.class );

		ExceptionReport.registerException( new ExceptionCallback()
		{
			@Override
			public ReportingLevel callback( Throwable cause, ExceptionReport report, ExceptionRegistrar registrar )
			{
				report.addException( ReportingLevel.E_ERROR, cause );
				return ReportingLevel.E_ERROR;
			}
		}, TimeoutException.class, MissingMethodException.class, CompilationFailedException.class, SandboxSecurityException.class, GroovyRuntimeException.class );

		ExceptionReport.registerException( new ExceptionCallback()
		{
			@Override
			public ReportingLevel callback( Throwable cause, ExceptionReport report, ExceptionRegistrar registrar )
			{
				report.addException( ReportingLevel.E_PARSE, cause );
				return ReportingLevel.E_PARSE;
			}
		}, SyntaxException.class );
	}

	public GroovyShell getNewShell( ScriptingContext context, Binding binding )
	{
		/* Create a compiler configuration */
		CompilerConfiguration configuration = new CompilerConfiguration();

		/* Set imports, timed executor, and implement sandbox */
		configuration.addCompilationCustomizers( imports, timedInterrupt, secure );

		/* Set scripting base class */
		if ( Objs.isEmpty( context.getScriptBaseClass() ) )
			configuration.setScriptBaseClass( ScriptingBaseHttp.class.getName() );
		else
			configuration.setScriptBaseClass( context.getScriptBaseClass() );

		/* Set default encoding */
		configuration.setSourceEncoding( context.getScriptingFactory().charset().name() );

		/* Set compiled script execute directory */
		configuration.setTargetDirectory( context.getCachePath() );

		/* Create and return new GroovyShell instance */
		return new GroovyShell( Kernel.class.getClassLoader(), binding, configuration );
	}

	@Override
	public ScriptingEngine[] makeEngines( ScriptingContext context )
	{
		return new ScriptingEngine[] {new GroovyEngine( this ), new GroovyServerPagesEngine( this )};
	}

	public Script makeScript( GroovyShell shell, ScriptingContext context ) throws ScriptingException.Error
	{
		return makeScript( shell, context.readString(), context );
	}

	public Script makeScript( GroovyShell shell, String source, ScriptingContext context ) throws ScriptingException.Error
	{
		// TODO Determine if a package node is prohibited and replace with an alternative, e.g., public, private, etc.

		Pattern p = Pattern.compile( "[ \\t]*package\\s+([a-zA_Z_][\\.\\w]*)\\s*;?\\s*\\n" );
		Matcher m = p.matcher( source );

		if ( m.find() )
		{
			String packageName = m.group( 1 );
			ScriptingFactory.L.warning( "The script " + context.getFileName() + " contains the directive `package " + packageName + "`, however, the GroovyEngine sets the package and this will be overwritten." );

			context.setBaseSource( source );
		}
		else if ( !Objs.isEmpty( context.getScriptPackage() ) )
		{
			source = "package " + context.getScriptPackage() + "; " + source;
			context.setBaseSource( source );
		}

		return shell.parse( source, context.getScriptName() );
	}

	public static class Config
	{
		public static final TypeBase.TypeLong DEFAULT_SCRIPT_TIMEOUT = new TypeBase.TypeLong( ScriptingFactory.Config.SCRIPTING_BASE, "defaultScriptTimeout", 30L );
	}
}
