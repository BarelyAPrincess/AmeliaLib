/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import javax.annotation.Nonnull;

/**
 * Used to track line and column numbers for SecurityExceptions thrown from GroovySandbox
 */
public class SandboxSecurityException extends SecurityException implements ExceptionContext
{
	private static final long serialVersionUID = -3520264898037710187L;
	String className = "";
	int colNum = -1;
	int lineNum = -1;
	String methodName = "";

	public SandboxSecurityException( String msg )
	{
		super( msg );
	}

	public SandboxSecurityException( String msg, Throwable cause )
	{
		super( msg, cause );
	}

	public String getClassName()
	{
		return className;
	}

	@Nonnull
	public ExceptionReport getExceptionReport()
	{
		return new ExceptionReport().addException( this );
	}

	public int getLineColumnNumber()
	{
		return colNum;
	}

	public int getLineNumber()
	{
		return lineNum;
	}

	public String getMethodName()
	{
		return methodName;
	}

	@Override
	public ReportingLevel getReportingLevel()
	{
		return ReportingLevel.E_ERROR;
	}

	@Nonnull
	@Override
	public Throwable getThrowable()
	{
		return this;
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}

	@Override
	public boolean isIgnorable()
	{
		return false;
	}

	public void setClassName( @Nonnull String className )
	{
		this.className = className;
	}

	public void setLineColumnNumber( int colNum )
	{
		this.colNum = colNum;
	}

	public void setLineNumber( int lineNum )
	{
		this.lineNum = lineNum;
	}

	public void setLineNumber( int lineNum, int colNum )
	{
		this.lineNum = lineNum;
		this.colNum = colNum;
	}

	public void setMethodName( @Nonnull String methodName )
	{
		this.methodName = methodName;
	}
}
