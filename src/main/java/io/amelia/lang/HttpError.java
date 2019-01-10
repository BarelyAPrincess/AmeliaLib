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

public class HttpError extends NetworkException.Error
{
	private static final long serialVersionUID = 8116947267974772489L;

	int statusCode;
	String statusReason;

	public HttpError( HttpCode status )
	{
		this( status, null );
	}

	public HttpError( HttpCode status, String developerMessage )
	{
		super( developerMessage == null ? status.getReason() : developerMessage );
		statusCode = status.getCode();
		statusReason = status.getReason();
	}

	public HttpError( int statusCode )
	{
		this( statusCode, null );
	}

	public HttpError( int statusCode, String statusReason )
	{
		this( statusCode, statusReason, null );
	}

	public HttpError( int statusCode, String statusReason, String developerMessage )
	{
		super( developerMessage == null ? statusReason : developerMessage );

		this.statusCode = statusCode;
		this.statusReason = statusReason;
	}

	public HttpError( Throwable cause, String developerMessage )
	{
		super( developerMessage == null ? HttpCode.HTTP_INTERNAL_SERVER_ERROR.getReason() : developerMessage, cause );

		statusCode = 500;
		statusReason = HttpCode.HTTP_INTERNAL_SERVER_ERROR.getReason();
	}

	public int getHttpCode()
	{
		return statusCode < 100 ? 500 : statusCode;
	}

	public String getReason()
	{
		return statusReason;
	}
}
