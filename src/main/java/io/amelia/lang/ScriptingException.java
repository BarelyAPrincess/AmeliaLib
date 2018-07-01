/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import java.util.List;

import io.amelia.scripting.ScriptTraceElement;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.StackFactory;

public class ScriptingException
{
	public static boolean isInnerClass( Throwable throwable )
	{
		return throwable instanceof Error || throwable instanceof Runtime;
	}

	private ScriptingException()
	{
		// Static Class
	}

	public static class Error extends ApplicationException.Error
	{
		private List<ScriptTraceElement> scriptTrace = null;

		public Error()
		{
			super( ReportingLevel.E_ERROR );
		}

		public Error( String message )
		{
			super( ReportingLevel.E_ERROR, message );
		}

		public Error( String message, Throwable cause )
		{
			super( ReportingLevel.E_ERROR, message, cause );
		}

		public Error( Throwable cause )
		{
			super( ReportingLevel.E_ERROR, cause );
		}

		@Override
		public String getMessage()
		{
			if ( isScriptingException() )
			{
				ScriptTraceElement element = getScriptTrace()[0];
				Throwable t = getCause() == null ? this : getCause();
				return String.format( "Exception %s thrown in file '%s' at line %s:%s, message '%s'", t.getClass().getName(), element.context().getFileName(), element.getLineNumber(), element.getColumnNumber() > 0 ? element.getColumnNumber() : 0, super.getMessage() );
			}
			else
			{
				Throwable t = getCause() == null ? this : getCause();
				return String.format( "Exception %s thrown in file '%s' at line %s, message '%s'", t.getClass().getName(), t.getStackTrace()[0].getFileName(), t.getStackTrace()[0].getLineNumber(), super.getMessage() );
			}
		}

		public ScriptTraceElement[] getScriptTrace()
		{
			return scriptTrace == null ? null : scriptTrace.toArray( new ScriptTraceElement[0] );
		}

		@Override
		public ReportingLevel handle( ExceptionReport exceptionReport, ExceptionContext exceptionContext )
		{
			/* Forward this type of exception to the report */
			if ( exceptionContext instanceof ScriptingContext )
				populateScriptTrace( ( ( ScriptingContext ) exceptionContext ).getScriptingFactory().stack() );
			exceptionReport.addException( level, this );
			return level;
		}

		public boolean hasScriptTrace()
		{
			return scriptTrace != null && scriptTrace.size() > 0;
		}

		public boolean isScriptingException()
		{
			return getCause() != null && getCause().getStackTrace().length > 0 && getCause().getStackTrace()[0].getClassName().startsWith( "org.codehaus.groovy.runtime" );
		}

		public void populateScriptTrace( StackFactory factory )
		{
			scriptTrace = factory.examineStackTrace( getCause() == null ? getStackTrace() : getCause().getStackTrace() );
		}
	}

	public static class Runtime extends ApplicationException.Runtime
	{
		private List<ScriptTraceElement> scriptTrace = null;

		public Runtime()
		{
			super( ReportingLevel.E_USER_ERROR );
		}

		public Runtime( String message )
		{
			super( ReportingLevel.E_USER_ERROR, message );
		}

		public Runtime( String message, Throwable cause )
		{
			super( ReportingLevel.E_USER_ERROR, message, cause );
		}

		public Runtime( Throwable cause )
		{
			super( ReportingLevel.E_USER_ERROR, cause );
		}

		@Override
		public String getMessage()
		{
			if ( isScriptingException() )
			{
				ScriptTraceElement element = getScriptTrace()[0];
				Throwable t = getCause() == null ? this : getCause();
				return String.format( "Exception %s thrown in file '%s' at line %s:%s, message '%s'", t.getClass().getName(), element.context().getFileName(), element.getLineNumber(), element.getColumnNumber() > 0 ? element.getColumnNumber() : 0, super.getMessage() );
			}
			else
			{
				Throwable t = getCause() == null ? this : getCause();
				return String.format( "Exception %s thrown in file '%s' at line %s, message '%s'", t.getClass().getName(), t.getStackTrace()[0].getFileName(), t.getStackTrace()[0].getLineNumber(), super.getMessage() );
			}
		}

		public ScriptTraceElement[] getScriptTrace()
		{
			return scriptTrace == null ? null : scriptTrace.toArray( new ScriptTraceElement[0] );
		}

		@Override
		public ReportingLevel handle( ExceptionReport exceptionReport, ExceptionContext exceptionContext )
		{
			/* Forward this type of exception to the report */
			if ( exceptionContext instanceof ScriptingContext )
				populateScriptTrace( ( ( ScriptingContext ) exceptionContext ).getScriptingFactory().stack() );
			exceptionReport.addException( level, this );
			return level;
		}

		public boolean hasScriptTrace()
		{
			return scriptTrace != null && scriptTrace.size() > 0;
		}

		public boolean isScriptingException()
		{
			return getCause() != null && getCause().getStackTrace().length > 0 && getCause().getStackTrace()[0].getClassName().startsWith( "org.codehaus.groovy.runtime" );
		}

		public void populateScriptTrace( StackFactory factory )
		{
			scriptTrace = factory.examineStackTrace( getCause() == null ? getStackTrace() : getCause().getStackTrace() );
		}
	}
}
