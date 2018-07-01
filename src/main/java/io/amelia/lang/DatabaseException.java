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

public class DatabaseException extends ApplicationException.Error
{
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
}
