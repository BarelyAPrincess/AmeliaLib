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

import java.util.Arrays;

import io.amelia.support.Voluntary;
import io.netty.handler.codec.http.HttpResponseStatus;

public enum HttpCode
{
	// Reference: http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
	HTTP_CONTINUE( 100, "Continue" ),
	HTTP_SWITCHING_PROTOCOLS( 101 ),
	HTTP_PROCESSING( 102 ),
	HTTP_OK( 200, "OK" ),
	HTTP_CREATED( 201, "Created" ),
	HTTP_ACCEPTED( 202, "Accepted" ),
	HTTP_NOT_AUTHORITATIVE( 203, "Non-Authoritative Information" ),
	HTTP_NO_CONTENT( 204, "No Content" ),
	HTTP_RESET( 205, "Reset Content" ),
	HTTP_PARTIAL( 206, "Partial Content" ),
	HTTP_MULTI_STATUS( 207 ),
	HTTP_ALREADY_REPORTED( 208 ),
	HTTP_IM_USED( 226 ),
	HTTP_MULTI_CHOICE( 300, "Multiple Choices" ),
	HTTP_MOVED_PERM( 301, "Moved Permanently" ),
	HTTP_MOVED_TEMP( 302, "Temporary Redirect" ),
	HTTP_SEE_OTHER( 303, "See Other" ),
	HTTP_NOT_MODIFIED( 304, "Not Modified" ),
	HTTP_USE_PROXY( 305, "Use Proxy" ),
	HTTP_TEMPORARY_REDIRECT( 307, "Temporary Redirect" ),
	HTTP_BAD_REQUEST( 400, "Bad Request" ),
	HTTP_UNAUTHORIZED( 401, "Unauthorized" ),
	HTTP_PAYMENT_REQUIRED( 402, "Payment Required" ),
	HTTP_FORBIDDEN( 403, "Forbidden" ),
	HTTP_NOT_FOUND( 404, "Not Found" ),
	HTTP_BAD_METHOD( 405, "Method Not Allowed" ),
	HTTP_NOT_ACCEPTABLE( 406, "Not Acceptable" ),
	HTTP_PROXY_AUTH( 407, "Proxy Authentication Required" ),
	HTTP_CLIENT_TIMEOUT( 408, "Request Time-Out" ),
	HTTP_CONFLICT( 409, "Conflict" ),
	HTTP_GONE( 410, "Gone" ),
	HTTP_LENGTH_REQUIRED( 411, "Length Required" ),
	HTTP_PRECON_FAILED( 412, "Precondition Failed" ),
	HTTP_ENTITY_TOO_LARGE( 413, "Request Entity Too Large" ),
	HTTP_REQ_TOO_LONG( 414, "Request-URI Too Large" ),
	HTTP_UNSUPPORTED_TYPE( 415, "Unsupported Media Type" ),
	HTTP_RANGE_NOT_SATISFIABLE( 416 ),
	HTTP_EXPECTATION_FAILED( 417 ),
	HTTP_TEA_POT( 418 ),
	HTTP_THE_DOCTOR( 418 ),
	HTTP_BLUE_BOX( 418, "Time and Relative Dimensions in Space. Yes, that's it. Names are funny. It's me. I'm the TARDIS." ),
	HTTP_INSUFFICIENT_STORAGE_ON_RESOURCE( 419 ),
	HTTP_METHOD_FAILURE( 420 ),
	HTTP_DESTINATION_LOCKED( 421 ),
	HTTP_UNPROCESSABLE_ENTITY( 422 ),
	HTTP_LOCKED( 423 ),
	HTTP_FAILED_DEPENDENCY( 424 ),
	HTTP_UPGRADE_REQUIRED( 426 ),
	HTTP_TOO_MANY_REQUESTS( 429, "Too Many Requests" ),
	HTTP_UNAVAILABLE_FOR_LEGAL_REASONS( 451, "Unavailable for Legal Reasons" ),
	HTTP_INTERNAL_SERVER_ERROR( 500, "Internal Server Error" ),
	HTTP_NOT_IMPLEMENTED( 501, "Not Implemented" ),
	HTTP_BAD_GATEWAY( 502, "Bad Gateway" ),
	HTTP_UNAVAILABLE( 503, "Service Unavailable" ),
	HTTP_GATEWAY_TIMEOUT( 504, "Gateway Timeout" ),
	HTTP_VERSION( 505, "HTTP Version Not Supported" ),
	HTTP_VARIANT_ALSO_NEGOTIATES( 506 ),
	HTTP_INSUFFICIENT_STORAGE( 507 ),
	HTTP_LOOP_DETECTED( 508 ),
	HTTP_NOT_EXTENDED( 510 );

	public static Voluntary<HttpCode> getHttpCode( int code )
	{
		return Voluntary.of( Arrays.stream( values() ).filter( httpCode -> code == httpCode.code ).findFirst() );
	}

	public static Voluntary<HttpCode> getHttpCode( HttpResponseStatus httpResponseStatus )
	{
		return Voluntary.of( Arrays.stream( values() ).filter( httpCode -> httpResponseStatus.code() == httpCode.code ).findFirst() );
	}

	public static Voluntary<String> getReason( int code )
	{
		return Voluntary.of( Arrays.stream( values() ).filter( httpCode -> code == httpCode.code ).map( HttpCode::getReason ).findFirst() );
	}

	private final int code;
	private final String reason;

	HttpCode( int code, String reason )
	{
		this.code = code;
		this.reason = reason;
	}

	HttpCode( int code )
	{
		this( code, null );
	}

	public int getCode()
	{
		return code;
	}

	public String getReason()
	{
		return reason;
	}

	public String print()
	{
		return code + " - " + reason;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "{code=" + getCode() + ",reason=" + getReason() + "}";
	}
}
