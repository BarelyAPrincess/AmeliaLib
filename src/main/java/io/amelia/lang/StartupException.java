/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 * Specifically only to be used as an exception thrown when a subset of code fails to start.
 * e.g., Users, Permissions, etc.
 */
public class StartupException extends UncaughtException
{
	private static final long serialVersionUID = 1L;

	public StartupException( String msg )
	{
		super( ReportingLevel.E_ERROR, msg );
	}

	public StartupException( String msg, Throwable e ) throws UncaughtException
	{
		super( ReportingLevel.E_ERROR, msg, e, true );
	}

	public StartupException( Throwable e ) throws UncaughtException
	{
		super( ReportingLevel.E_ERROR, e, true );
	}

	@Override
	public ReportingLevel handle( ExceptionReport exceptionReport, ExceptionContext exceptionContext )
	{
		exceptionReport.addException( ReportingLevel.E_ERROR, this );
		return ReportingLevel.E_ERROR;
	}
}
