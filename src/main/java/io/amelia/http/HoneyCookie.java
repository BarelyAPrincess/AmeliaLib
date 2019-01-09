/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

public class HoneyCookie
{
	private Cookie cookie;
	private boolean needsUpdating;

	public HoneyCookie( String name, String value )
	{
		cookie = new DefaultCookie( name, value );
		needsUpdating = true;
	}

	public HoneyCookie( Cookie cookie )
	{
		this.cookie = cookie;
	}

	public Cookie getCookie()
	{
		return cookie;
	}

	public String getDomain()
	{
		return cookie.domain();
	}

	public long getMaxAge()
	{
		return cookie.maxAge();
	}

	public String getName()
	{
		return cookie.name();
	}

	public String getPath()
	{
		return cookie.path();
	}

	public String getValue()
	{
		return cookie.value();
	}

	public boolean getWrap()
	{
		return cookie.wrap();
	}

	public boolean isHttpOnly()
	{
		return cookie.isHttpOnly();
	}

	public boolean isSecure()
	{
		return cookie.isSecure();
	}

	public boolean needsUpdating()
	{
		return needsUpdating;
	}

	public HoneyCookie setDomain( String domain )
	{
		cookie.setDomain( domain );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie setHttpOnly( boolean httpOnly )
	{
		cookie.setHttpOnly( httpOnly );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie setMaxAge( long maxAge )
	{
		cookie.setMaxAge( maxAge );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie setPath( String path )
	{
		cookie.setPath( path );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie setSecure( boolean secure )
	{
		cookie.setSecure( secure );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie setValue( String value )
	{
		cookie.setValue( value );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie setWrap( boolean wrap )
	{
		cookie.setWrap( wrap );
		needsUpdating = true;
		return this;
	}

	public HoneyCookie unsetNeedsUpdating()
	{
		needsUpdating = false;
		return this;
	}
}
