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
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import io.amelia.support.HttpContext;
import io.amelia.support.HttpCookie;
import io.amelia.support.Voluntary;
import io.amelia.support.http.HttpVariableMap;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public interface HttpScriptingRequest
{
	static final Map<Thread, WeakReference<HttpScriptingRequest>> references = new ConcurrentHashMap<>();

	static HttpScriptingRequest getThreadReference()
	{
		if ( !references.containsKey( Thread.currentThread() ) || references.get( Thread.currentThread() ).get() == null )
			throw new IllegalStateException( "Thread '" + Thread.currentThread().getName() + "' does not seem to currently link to any existing HTTP requests, please try again or notify an administrator." );
		return references.get( Thread.currentThread() ).get();
	}

	static void putHttpScriptingRequest( HttpScriptingRequest httpScriptingRequest )
	{
		references.put( Thread.currentThread(), new WeakReference<>( httpScriptingRequest ) );
	}

	void enforceTrailingSlash( boolean enforce );

	String getArgument( String key );

	String getArgument( String key, String def );

	boolean getArgumentBoolean( String key );

	double getArgumentDouble( String key );

	int getArgumentInt( String key );

	Set<String> getArgumentKeys();

	long getArgumentLong( String key );

	Stream<Map.Entry<String, String>> getArguments();

	String getBaseUrl();

	ScriptBinding getBinding();

	String getChildDomain();

	int getContentLength();

	Voluntary<? extends HttpCookie> getCookie( String key );

	Stream<? extends HttpCookie> getHoneyCookies();

	String getFullDomain();

	String getFullDomain( boolean ssl );

	String getFullDomain( String subdomain );

	String getFullDomain( String subdomain, boolean ssl );

	String getFullDomain( String subdomain, String prefix );

	String getFullUrl();

	String getFullUrl( boolean ssl );

	String getFullUrl( String subdomain );

	String getFullUrl( String subdomain, boolean ssl );

	String getFullUrl( String subdomain, String prefix );

	Map<String, Object> getGetMap();

	Map<String, String> getGetMapRaw();

	String getHeader( CharSequence key );

	String getHost();

	String getHostDomain();

	HttpContext getHttpContext();

	HttpVersion getHttpVersion();

	InetAddress getInetAddr();

	InetAddress getInetAddr( boolean detectCDN );

	String getIpAddress();

	String getIpAddress( boolean detectCDN );

	String getLocalHostName();

	String getLocalIpAddress();

	int getLocalPort();

	String getParameter( String key );

	Map<String, Object> getPostMap();

	Map<String, String> getPostMapRaw();

	String getQuery();

	String getRemoteHostname();

	int getRemotePort();

	String getRequestHost();

	Map<String, Object> getRequestMap() throws Exception;

	Map<String, String> getRequestMapRaw() throws Exception;

	long getRequestTime();

	Map<String, String> getRewriteMap();

	String getRootDomain();

	ScriptingFactory getScriptingFactory();

	HttpVariableMap getServer();

	Voluntary<? extends HttpCookie> getServerCookie( String key );

	Stream<? extends HttpCookie> getServerCookies();

	String getTopDomain();

	String getTopDomain( boolean ssl );

	String getTopDomain( String subdomain );

	String getTopDomain( String subdomain, boolean ssl );

	String getTopDomain( String subdomain, String prefix );

	String getUri();

	String getUserAgent();

	boolean hasArgument( String key );

	boolean isAjaxRequest();

	boolean isCDN();

	boolean isSecure();

	boolean isWebsocketRequest();

	HttpMethod getHttpMethod();

	String getMethodString();

	// XXX Better Implement
	void requireLogin() throws IOException;

	void requireLogin( String permission ) throws IOException;
}
