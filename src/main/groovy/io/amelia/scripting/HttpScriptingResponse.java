/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.Nullable;

import io.amelia.lang.HttpCode;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

public interface HttpScriptingResponse
{

	void close();

	void finishMultipart() throws IOException;

	String getAnnotation( String key );

	Charset getEncoding();

	int getHttpCode();

	String getHttpReason();

	ByteBuf getOutput();

	byte[] getOutputBytes();

	boolean isCommitted();

	@Deprecated
	void print( byte[] bytes ) throws IOException;

	/**
	 * Prints a single string of text to the buffered output
	 *
	 * @param var string of text.
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	void print( String var ) throws IOException;

	/**
	 * Prints a single string of text with a line return to the buffered output
	 *
	 * @param var string of text.
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	void println( String var ) throws IOException;

	void resetBuffer();

	void sendError( Exception e ) throws IOException;

	void sendError( int code ) throws IOException;

	void sendError( int code, String statusReason, String developerMessage ) throws IOException;

	void sendError( int code, String developerMessage ) throws IOException;

	void sendError( HttpResponseStatus status ) throws IOException;

	void sendError( HttpResponseStatus status, String statusReason, String developerMessage ) throws IOException;

	void sendError( HttpResponseStatus status, String developerMessage ) throws IOException;

	void sendError( HttpCode httpCode ) throws IOException;

	void sendError( HttpCode httpCode, String developerMessage ) throws IOException;

	void sendError( @Nullable HttpCode httpCode, @Nullable String statusReason, @Nullable String developerMessage ) throws IOException;

	void sendException( Throwable cause ) throws IOException;

	/**
	 * Sends the client to the webroot login page found in data and also sends a please login message along with it.
	 */
	void sendLoginPage();

	/**
	 * Sends the client to the webroot login page
	 *
	 * @param msg The message to pass to the login page
	 */
	void sendLoginPage( String msg );

	/**
	 * Sends the client to the webroot login page
	 *
	 * @param msg   The message to pass to the login page
	 * @param level The severity level of this login page redirect
	 */
	void sendLoginPage( String msg, String level );

	/**
	 * Sends the client to the webroot login page
	 *
	 * @param msg    The message to pass to the login page
	 * @param level  The severity level of this login page redirect
	 * @param target The target to redirect to once we receive a successful login
	 */
	void sendLoginPage( String msg, String level, String target );

	void sendMultipart( byte[] bytesToWrite ) throws IOException;

	/**
	 * Send the client to a specified page with http code 302 automatically.
	 *
	 * @param target The destination URL. Can either be relative or absolute.
	 */
	void sendRedirect( String target );

	/**
	 * Sends the client to a specified page with specified http code but with the option to not automatically go.
	 *
	 * @param target   The destination url. Can be relative or absolute.
	 * @param httpCode What http code to use.
	 */
	void sendRedirect( String target, HttpCode httpCode );

	void sendRedirect( String target, HttpCode httpCode, Map<String, String> nonceValues );

	void sendRedirect( String target, Map<String, String> nonceValues );

	void sendRedirectRepost( String target );

	/**
	 * Sends the data to the client. Internal Use.
	 *
	 * @throws IOException if there was a problem sending the data, like the connection was unexpectedly closed.
	 */
	void sendResponse() throws IOException;

	void setAnnotation( String key, String val );

	void setContentLength( long length );

	/**
	 * Sets the ContentType header.
	 *
	 * @param type, e.g., text/html or application/xml
	 */
	void setContentType( String type );

	void setEncoding( String encoding );

	void setEncoding( Charset encoding );

	void setHeader( String key, Object val );

	void setStatus( HttpCode httpCode );

	void setStatus( int code );

	/**
	 * Redirects the current page load to a secure HTTPS connection
	 */
	boolean switchToSecure();

	/**
	 * Redirects the current page load to an unsecure HTTP connection
	 */
	boolean switchToUnsecure();

	/**
	 * Writes a byte array to the buffered output.
	 *
	 * @param bytes byte array to print
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	void write( byte[] bytes ) throws IOException;

	/**
	 * Writes a ByteBuf to the buffered output
	 *
	 * @param buf byte buffer to print
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	void write( ByteBuf buf ) throws IOException;
}
