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
import java.util.stream.Stream;

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

	public ScriptingResult addException( Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof ScriptingException.Error )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException.Error ) throwable ).hasScriptTrace() )
					if ( context.getScriptingFactory() != null )
						( ( ScriptingException.Error ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
					else if ( context.getResult() != null )
						( ( ScriptingException.Error ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
				exceptionReport.addException( ( ScriptingException.Error ) throwable );
			}
			else if ( throwable instanceof ScriptingException.Runtime )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException.Runtime ) throwable ).hasScriptTrace() )
					if ( context.getScriptingFactory() != null )
						( ( ScriptingException.Runtime ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
					else if ( context.getResult() != null )
						( ( ScriptingException.Runtime ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
				exceptionReport.addException( ( ScriptingException.Runtime ) throwable );
			}
			else
				exceptionReport.addException( new ScriptingException.Error( throwable ).populateScriptTrace( context.getScriptingFactory().getStack() ) );
		return this;
	}

	public ScriptingResult addException( ReportingLevel level, Throwable throwable )
	{
		if ( throwable != null )
			if ( throwable instanceof ScriptingException.Error )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException.Error ) throwable ).hasScriptTrace() )
					if ( context.getScriptingFactory() != null )
						( ( ScriptingException.Error ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
					else if ( context.getResult() != null )
						( ( ScriptingException.Error ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
				exceptionReport.addException( level, ( ScriptingException.Error ) throwable );
			}
			else if ( throwable instanceof ScriptingException.Runtime )
			{
				// If this EvalException never had it's script trace populated, we handle it here
				if ( !( ( ScriptingException.Runtime ) throwable ).hasScriptTrace() )
					if ( context.getScriptingFactory() != null )
						( ( ScriptingException.Runtime ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
					else if ( context.getResult() != null )
						( ( ScriptingException.Runtime ) throwable ).populateScriptTrace( context.getScriptingFactory().getStack() );
				exceptionReport.addException( level, ( ScriptingException.Runtime ) throwable );
			}
			else
				exceptionReport.addException( level, new ScriptingException.Error( throwable ).populateScriptTrace( context.getScriptingFactory().getStack() ) );
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

	public Stream<ExceptionContext> getSevereExceptions()
	{
		return exceptionReport.getSevereExceptions();
	}

	public Stream<ExceptionContext> getIgnorableExceptions()
	{
		return exceptionReport.getIgnorableExceptions();
	}

	public Stream<ExceptionContext> getExceptions()
	{
		return exceptionReport.getExceptions();
	}

	public Object getObject()
	{
		return obj;
	}

	public String getReason()
	{
		if ( reason == null || reason.isEmpty() )
			reason = "There was no available result reason at this time.";
		return reason;
	}

	public Script getScript()
	{
		return script;
	}

	public String getString()
	{
		return ( content == null ? "" : content.toString( Charset.defaultCharset() ) );
	}

	public void handleException( Throwable throwable )
	{
		exceptionReport.handleException( throwable );
		setFailure();
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

	public void setObject( Object obj )
	{
		this.obj = obj;
	}

	public ScriptingResult setReason( String reason )
	{
		this.reason = reason;
		return this;
	}

	public void setScript( Script script )
	{
		this.script = script;
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
