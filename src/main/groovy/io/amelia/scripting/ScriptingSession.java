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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.amelia.data.parcel.Parcel;
import io.amelia.foundation.Foundation;
import io.amelia.permissions.PermissibleEntity;
import io.amelia.permissions.Permission;
import io.amelia.permissions.PermissionNamespace;
import io.amelia.permissions.PermissionResult;
import io.amelia.permissions.References;
import io.amelia.permissions.lang.PermissionDeniedException;
import io.amelia.support.HttpCookie;
import io.amelia.support.http.HttpNonce;
import io.amelia.users.UserContext;
import io.amelia.users.UserResult;

public interface ScriptingSession
{
	PermissionResult checkPermission( String perm );

	PermissionResult checkPermission( String perm, References refs );

	PermissionResult checkPermission( Permission perm, References refs );

	PermissionResult checkPermission( String perm, String... refs );

	PermissionResult checkPermission( Permission perm, String... refs );

	PermissionResult checkPermission( Permission perm );

	void destroyNonce();

	HttpCookie getCookie( String key );

	Stream<? extends HttpCookie> getCookies();

	Parcel getDataMap();

	Object getGlobal( String key );

	Map<String, Object> getGlobals();

	List<String> getIpAddresses();

	String getName();

	HttpNonce getNonce();

	HttpCookie getSessionCookie();

	String getSessionId();

	long getTimeout();

	UserContext getUser();

	String getVariable( String key );

	String getVariable( String key, String def );

	boolean hasLogin();

	void initNonce();

	boolean isAdmin();

	boolean isInvalidated();

	boolean isNew();

	boolean isOp();

	boolean isSet( String key );

	UserResult kick( String reason );

	void noTimeout();

	void processSessionCookie( String domain );

	void putSessionCookie( String key, HttpCookie cookie );

	void rearmTimeout();

	void remember( boolean remember );

	PermissionResult requirePermission( String req, References refs ) throws PermissionDeniedException;

	PermissionResult requirePermission( String req, String... refs ) throws PermissionDeniedException;

	PermissionResult requirePermission( Permission req, String... refs ) throws PermissionDeniedException;

	PermissionResult requirePermission( Permission req, References refs ) throws PermissionDeniedException;

	void saveWithoutException();

	void setGlobal( String key, Object val );

	void setVariable( String key, String value );
}
