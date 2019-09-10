/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

public interface HttpCookie<Self extends HttpCookie>
{
	String getComment();

	String getDomain();

	long getMaxAge();

	String getName();

	String getPath();

	String getValue();

	boolean getWrap();

	boolean isHttpOnly();

	boolean isSecure();

	boolean needsUpdating();

	Self setComment( String comment );

	Self setDomain( String domain );

	Self setHttpOnly( boolean httpOnly );

	Self setMaxAge( long maxAge );

	Self setPath( String path );

	Self setSecure( boolean secure );

	Self setValue( String value );

	Self setWrap( boolean wrap );

	Self unsetNeedsUpdating();
}
