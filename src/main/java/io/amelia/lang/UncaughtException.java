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

public class UncaughtException extends RuntimeException implements ExceptionContext
{
	private static final long serialVersionUID = 6854413013575591783L;

	private ReportingLevel level;

	public UncaughtException()
	{
		this( ReportingLevel.E_UNHANDLED );
	}

	public UncaughtException( ReportingLevel level )
	{
		this.level = level;
	}

	public UncaughtException( ReportingLevel level, String message )
	{
		super( message );
		this.level = level;
	}

	public UncaughtException( ReportingLevel level, String msg, Throwable cause )
	{
		super( msg, cause );
		this.level = level;
		if ( cause instanceof UncaughtException )
			throwCauseException();
	}

	public UncaughtException( ReportingLevel level, String msg, Throwable cause, boolean throwDuplicate ) throws UncaughtException
	{
		super( msg, cause );
		this.level = level;
		if ( cause instanceof UncaughtException )
			if ( throwDuplicate )
				throw ( UncaughtException ) cause;
			else
				throwCauseException();
	}

	public UncaughtException( ReportingLevel level, Throwable cause )
	{
		super( cause );
		this.level = level;
		if ( cause instanceof UncaughtException )
			throwCauseException();
	}

	public UncaughtException( ReportingLevel level, Throwable cause, boolean throwDuplicate ) throws UncaughtException
	{
		super( cause );
		this.level = level;
		if ( cause instanceof UncaughtException )
			if ( throwDuplicate )
				throw ( UncaughtException ) cause;
			else
				throwCauseException();
	}

	public UncaughtException( String message )
	{
		this( ReportingLevel.E_UNHANDLED, message );
	}

	public UncaughtException( String msg, Throwable cause )
	{
		this( ReportingLevel.E_UNHANDLED, msg, cause );
	}

	public UncaughtException( Throwable cause )
	{
		this( ReportingLevel.E_UNHANDLED, cause );
	}

	@Nonnull
	@Override
	public ExceptionReport getExceptionReport()
	{
		return new ExceptionReport().addException( this );
	}

	@Override
	public ReportingLevel getReportingLevel()
	{
		return level;
	}

	@Nonnull
	@Override
	public Throwable getThrowable()
	{
		return this;
	}

	@Override
	public ReportingLevel handle( ExceptionReport exceptionReport, ExceptionContext exceptionContext )
	{
		exceptionReport.handleException( getCause(), exceptionContext );
		return level;
	}

	@Override
	public boolean isIgnorable()
	{
		return level.isIgnorable();
	}

	public void setReportingLevel( ReportingLevel level )
	{
		this.level = level;
	}

	private void throwCauseException()
	{
		throw new IllegalArgumentException( "The cause argument can't be of it's own type." );
	}
}
