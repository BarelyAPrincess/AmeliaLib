/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import com.google.common.collect.Maps;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.data.TypeBase;
import io.amelia.events.EventException;
import io.amelia.events.Events;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ScriptingException;
import io.amelia.scripting.event.PostEvalEvent;
import io.amelia.scripting.event.PreEvalEvent;
import io.amelia.scripting.groovy.GroovyRegistry;
import io.amelia.scripting.processing.CoffeeProcessor;
import io.amelia.scripting.processing.ImageProcessor;
import io.amelia.scripting.processing.JSMinProcessor;
import io.amelia.scripting.processing.LessProcessor;
import io.amelia.support.Encrypt;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.Pair;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ScriptingFactory
{
	public static final Kernel.Logger L = Kernel.getLogger( ScriptingFactory.class );
	private static List<ScriptingProcessor> processors = new ArrayList<>();
	private static volatile List<ScriptingRegistry> scripting = new ArrayList<>();

	static
	{
		new GroovyRegistry();

		/*
		 * Register Pre-Processors
		 */
		// register( new PreLinksParserWrapper() );
		// register( new PreIncludesParserWrapper() );
		if ( ConfigRegistry.config.getValue( Config.PROCESSORS_COFFEE ) )
			register( new CoffeeProcessor() );
		if ( ConfigRegistry.config.getValue( Config.PROCESSORS_LESS ) )
			register( new LessProcessor() );
		// register( new SassPreProcessor() );

		/*
		 * Register Post-Processors
		 */
		if ( ConfigRegistry.config.getValue( Config.PROCESSORS_MINIFY_JS ) )
			register( new JSMinProcessor() );
		if ( ConfigRegistry.config.getValue( Config.PROCESSORS_IMAGES ) )
			register( new ImageProcessor() );
	}

	// For Web Use
	public static ScriptingFactory create( BindingProvider provider )
	{
		return new ScriptingFactory( provider.getBinding() );
	}

	// For General Use
	public static ScriptingFactory create( Map<String, Object> rawBinding )
	{
		return new ScriptingFactory( new ScriptBinding( rawBinding ) );
	}

	// For General Use
	public static ScriptingFactory create( ScriptBinding binding )
	{
		return new ScriptingFactory( binding );
	}

	public static void register( ScriptingProcessor scriptingProcessor )
	{
		if ( !processors.contains( scriptingProcessor ) )
			processors.add( scriptingProcessor );
	}

	/**
	 * Registers the provided ScriptingProcessing with the EvalFactory
	 *
	 * @param registry The {@link ScriptingRegistry} instance to handle provided types
	 */
	public static void register( ScriptingRegistry registry )
	{
		if ( !scripting.contains( registry ) )
			scripting.add( registry );
	}

	private final ScriptBinding binding;
	private final List<Pair<ByteBuf, StackType>> bufferStack = new LinkedList<>();
	private final Map<ScriptingEngine, List<String>> engines = Maps.newLinkedHashMap();
	private final ByteBuf output = Unpooled.buffer();
	private final StackFactory stackFactory = new StackFactory();
	private Charset charset = Charset.forName( ConfigRegistry.config.getString( "server.defaultEncoding" ).orElse( "UTF-8" ) );
	private YieldBuffer yieldBuffer = null;

	private ScriptingFactory( ScriptBinding binding )
	{
		Objs.notNull( binding, "The ScriptBinding can't be null" );
		this.binding = binding;
	}

	public ScriptBinding binding()
	{
		return binding;
	}

	/**
	 * Returns the output buffer to it's last state
	 */
	private void bufferPop( int level )
	{
		if ( bufferStack.size() == 0 )
			throw new IllegalStateException( "Buffer stack is empty." );

		if ( bufferStack.size() - 1 < level )
			throw new IllegalStateException( "Buffer stack size was too low." );

		// Check for possible forgotten obEnd()'s. Could loop as each detection will move up one next level.
		if ( bufferStack.size() > level + 1 && bufferStack.get( level + 1 ).getValue() == StackType.OB )
			obFlush( level + 1 );

		// Determines if the buffer was not push'd or pop'd in the correct order, often indicating outside manipulation of the bufferStack.
		if ( bufferStack.size() - 1 > level )
			throw new IllegalStateException( "Buffer stack size was too high." );

		output.clear();
		output.writeBytes( bufferStack.remove( level ).getKey() );
	}

	/**
	 * Stores the current output buffer for the stacked capture
	 */
	private int bufferPush( StackType type )
	{
		bufferStack.add( new Pair<>( output.copy(), type ) );
		output.clear();
		return bufferStack.size() - 1;
	}

	public Charset charset()
	{
		return charset;
	}

	private void compileEngines( ScriptingContext context )
	{
		for ( ScriptingRegistry registry : scripting )
			for ( ScriptingEngine engine : registry.makeEngines( context ) )
				if ( !contains( engine ) )
				{
					engine.setBinding( binding );
					engine.setOutput( output, charset );
					engines.put( engine, engine.getTypes() );
				}
	}

	private boolean contains( ScriptingEngine engine2 )
	{
		for ( ScriptingEngine engine1 : engines.keySet() )
			if ( engine1.getClass() == engine2.getClass() )
				return true;
		return false;
	}

	public ScriptingResult eval( ScriptingContext context )
	{
		final ScriptingResult result = context.getResult();

		context.setScriptingFactory( this );
		context.setCharset( charset );
		context.setBaseSource( new String( context.readBytes(), charset ) );
		binding.setVariable( "__FILE__", context.getFileName() == null ? "<no file>" : context.getFileName() );

		if ( result.getExceptionReport().hasSevereExceptions() )
			return result;

		try
		{
			String name;
			if ( context.isVirtual() )
				name = "EvalScript" + Encrypt.rand( 8 ) + ".hps";
			else
			{
				String rel = IO.relPath( context.getPath().getParent(), context.getSourceDirectory() ).replace( '\\', '.' ).replace( '/', '.' );
				context.setCachePath( Paths.get( rel.contains( "." ) ? rel.substring( 0, rel.indexOf( "." ) ) : rel ).resolve( context.getCachePath() ) );
				context.setScriptPackage( rel.contains( "." ) ? rel.substring( rel.indexOf( "." ) + 1 ) : "" );
				name = context.getPath().getFileName().toString();
			}

			context.setScriptName( name );
			stackFactory.stack( name, context );

			processors.forEach( scriptingProcessor -> scriptingProcessor.transformScriptingContext( context ) );
			processors.forEach( scriptingProcessor -> scriptingProcessor.preEvaluate( context ) );

			// TODO should evaluations still flow through the events or is that becoming too resource intensive?
			PreEvalEvent preEvent = new PreEvalEvent( context );
			try
			{
				Events.getInstance().callEventWithException( preEvent );
			}
			catch ( Exception e )
			{
				result.handleException( e.getCause() == null ? e : e.getCause() );
			}

			if ( preEvent.isCancelled() )
				result.handleException( new ScriptingException.Error( "Script evaluation was cancelled by internal event" ) );

			if ( engines.size() == 0 )
				compileEngines( context );

			if ( engines.size() > 0 )
				for ( Entry<ScriptingEngine, List<String>> entry : engines.entrySet() )
					if ( entry.getValue() == null || entry.getValue().size() == 0 || entry.getValue().contains( context.getShell().toLowerCase() ) )
					{
						int level = bufferPush( StackType.SCRIPT );
						try
						{
							// Determine if data was written to the context during the eval(). Indicating data was either written directly or a sub-eval was called.
							String hash = context.getBufferHash();
							entry.getKey().eval( context );
							if ( context.getBufferHash().equals( hash ) )
								context.resetAndWrite( output );
							else
								context.write( output );
							break;
						}
						catch ( Throwable cause )
						{
							result.handleException( cause );
						}
						finally
						{
							bufferPop( level );
						}
					}

			processors.forEach( scriptingProcessor -> scriptingProcessor.postEvaluate( context ) );

			PostEvalEvent postEvent = new PostEvalEvent( context );
			try
			{
				Events.getInstance().callEventWithException( postEvent );
			}
			catch ( EventException.Error e )
			{
				result.handleException( e.getCause() == null ? e : e.getCause() );
			}
		}
		/* catch ( EvalSevereError e )
		{
			// Evaluation has aborted and we return the ScriptingResult AS-IS.
			return result.setFailure();
		}*/
		finally
		{
			stackFactory.unstack();
		}

		return result.setSuccess();
	}

	public Charset getCharset()
	{
		return charset;
	}

	public String getFileName()
	{
		List<ScriptTraceElement> scriptTrace = getScriptTrace();

		if ( scriptTrace.size() < 1 )
			return "<unknown>";

		String fileName = scriptTrace.get( scriptTrace.size() - 1 ).context().getFileName();

		if ( fileName == null || fileName.isEmpty() )
			return "<unknown>";

		return fileName;
	}

	/**
	 * Attempts to find the current line number for the current groovy script.
	 *
	 * @return The current line number. Returns -1 if no there was a problem getting the current line number.
	 */
	public int getLineNumber()
	{
		List<ScriptTraceElement> scriptTrace = getScriptTrace();

		if ( scriptTrace.size() < 1 )
			return -1;

		return scriptTrace.get( scriptTrace.size() - 1 ).getLineNumber();
	}

	public ByteBuf getOutputStream()
	{
		return output;
	}

	public List<ScriptTraceElement> getScriptTrace()
	{
		return stackFactory.examineStackTrace( Thread.currentThread().getStackTrace() );
	}

	public YieldBuffer getYieldBuffer()
	{
		if ( yieldBuffer == null )
			yieldBuffer = new YieldBuffer();
		return yieldBuffer;
	}

	public String obEnd( int stackLevel )
	{
		if ( bufferStack.get( stackLevel ).getValue() != StackType.OB )
			throw new IllegalStateException( "The stack level was not an Output Buffer." );

		String content = output.toString( charset );

		bufferPop( stackLevel );

		return content;
	}

	public void obFlush( int stackLevel )
	{
		// Forward the output buffer content into the last buffer
		String content = obEnd( stackLevel );
		print( content );
	}

	public int obStart()
	{
		return bufferPush( StackType.OB );
	}

	/**
	 * Gives externals subroutines access to the current output stream via print()
	 *
	 * @param text The text to output
	 */
	public void print( String text )
	{
		output.writeBytes( text.getBytes( charset ) );
	}

	/**
	 * Gives externals subroutines access to the current output stream via println()
	 *
	 * @param text The text to output
	 */
	public void println( String text )
	{
		output.writeBytes( ( text + "\n" ).getBytes( charset ) );
	}

	public void setEncoding( Charset charset )
	{
		this.charset = charset;
	}

	public void setVariable( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	public StackFactory getStack()
	{
		return stackFactory;
	}

	private enum StackType
	{
		SCRIPT,
		// Indicates script output stack
		OB // Indicates output buffer stack
	}

	public static class Config
	{
		public static final TypeBase SCRIPTING_BASE = new TypeBase( "scripting" );
		public static final TypeBase PROCESSORS_BASE = new TypeBase( SCRIPTING_BASE, "processors" );
		public static final TypeBase.TypeBoolean PROCESSORS_COFFEE = new TypeBase.TypeBoolean( PROCESSORS_BASE, "coffeeEnabled", true );
		public static final TypeBase.TypeBoolean PROCESSORS_LESS = new TypeBase.TypeBoolean( PROCESSORS_BASE, "lessEnabled", true );
		public static final TypeBase.TypeBoolean PROCESSORS_MINIFY_JS = new TypeBase.TypeBoolean( PROCESSORS_BASE, "minifyJSEnabled", true );
		public static final TypeBase.TypeBoolean PROCESSORS_IMAGES = new TypeBase.TypeBoolean( PROCESSORS_BASE, "imagesEnabled", true );
		public static final TypeBase.TypeBoolean PROCESSORS_IMAGES_CACHE = new TypeBase.TypeBoolean( PROCESSORS_BASE, "imagesCacheEnabled", true );
		public static final TypeBase.TypeStringList PREFERRED_EXTENSIONS = new TypeBase.TypeStringList( SCRIPTING_BASE, "preferredExtensions", Arrays.asList( "html", "htm", "groovy", "gsp", "jsp" ) );
	}
}
