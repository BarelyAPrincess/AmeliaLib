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

import java.nio.charset.Charset;

import groovy.lang.Script;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.ScriptingException;
import io.netty.buffer.ByteBuf;

/**
 * Contains the end result of {@link ScriptingFactory#eval(ScriptingContext)}
 */
public class ScriptingResult
{
	private final ScriptingContext context;
	private ByteBuf content;
	private ExceptionReport exceptionReport = new ExceptionReport();
	private Object obj = null;
	private String reason = null;
	private Script script = null;
	private boolean success = false;

	ScriptingResult( ScriptingContext context, ByteBuf content )
	{
		this.context = context;
		this.content = content;
	}

	@Override
	public ScriptingResult addException( Exception exception )
	{
		IException.check( exception );
		if ( exception != null )
			if ( exception instanceof ScriptingException )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException ) exception ).hasScriptTrace() )
					if ( context.getScriptingFactory() != null )
						( ( ScriptingException ) exception ).populateScriptTrace( context.getScriptingFactory().stack() );
					else if ( context.getRequest() != null )
						( ( ScriptingException ) exception ).populateScriptTrace( context.getRequest().getScriptingFactory().stack() );
				caughtExceptions.add( exception );
			}
			else
				super.addException( exception );
		return this;
	}

	@Override
	public ScriptingResult addException( ReportingLevel level, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof ScriptingException )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException ) throwable ).hasScriptTrace() )
					if ( context.getScriptingFactory() != null )
						( ( ScriptingException ) throwable ).populateScriptTrace( context.getScriptingFactory().stack() );
					else if ( context.request() != null )
						( ( ScriptingException ) throwable ).populateScriptTrace( context.request().getScriptingFactory().stack() );
				caughtExceptions.add( ( ScriptingException ) throwable );
			}
			else
				caughtExceptions.add( new ScriptingException( level, throwable ).populateScriptTrace( context.getScriptingFactory().stack() ) );
		return this;
	}

	public ByteBuf content()
	{
		return content;
	}

	public ScriptingContext context()
	{
		return context;
	}

	public ExceptionReport getExceptionReport()
	{
		return exceptionReport;
	}

	public IException[] getExceptions()
	{
		return caughtExceptions.toArray( new IException[0] );
	}

	public Object getObject()
	{
		return obj;
	}

	public void setObject( Object obj )
	{
		this.obj = obj;
	}

	public String getReason()
	{
		if ( reason == null || reason.isEmpty() )
			reason = "There was no available result reason at this time.";
		return reason;
	}

	public ScriptingResult setReason( String reason )
	{
		this.reason = reason;
		return this;
	}

	public Script getScript()
	{
		return script;
	}

	public void setScript( Script script )
	{
		this.script = script;
	}

	public String getString()
	{
		return ( content == null ? "" : content.toString( Charset.defaultCharset() ) );
	}

	public void handleException( ScriptingContext context, Throwable throwable ) throws EvalSevereError
	{

	}

	public boolean hasObject()
	{
		return obj != null;
	}

	public boolean isSuccessful()
	{
		return success;
	}

	public ScriptingResult setFailure()
	{
		success = false;
		return this;
	}

	public ScriptingResult setSuccess()
	{
		success = true;
		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "EvalFactoryResult{success=%s,reason=%s,size=%s,obj=%s,script=%s,context=%s}", success, reason, content.writerIndex(), obj, script, context );
	}
}
