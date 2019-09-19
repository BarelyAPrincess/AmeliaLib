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

public class DatabaseException extends ApplicationException.Error
{
	private static final long serialVersionUID = 5522301956671473324L;

	public DatabaseException( String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	public DatabaseException( String message, Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	public DatabaseException( Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
