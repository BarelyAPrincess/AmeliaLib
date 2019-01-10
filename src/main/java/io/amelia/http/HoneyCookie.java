/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import io.amelia.support.HttpCookie;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

public class HoneyCookie implements HttpCookie<HoneyCookie>
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

	@Override
	public String getDomain()
	{
		return cookie.domain();
	}

	@Override
	public long getMaxAge()
	{
		return cookie.maxAge();
	}

	@Override
	public String getName()
	{
		return cookie.name();
	}

	@Override
	public String getPath()
	{
		return cookie.path();
	}

	@Override
	public String getValue()
	{
		return cookie.value();
	}

	@Override
	public boolean getWrap()
	{
		return cookie.wrap();
	}

	@Override
	public boolean isHttpOnly()
	{
		return cookie.isHttpOnly();
	}

	@Override
	public boolean isSecure()
	{
		return cookie.isSecure();
	}

	@Override
	public boolean needsUpdating()
	{
		return needsUpdating;
	}

	@Override
	public HoneyCookie setDomain( String domain )
	{
		cookie.setDomain( domain );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setHttpOnly( boolean httpOnly )
	{
		cookie.setHttpOnly( httpOnly );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setMaxAge( long maxAge )
	{
		cookie.setMaxAge( maxAge );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setPath( String path )
	{
		cookie.setPath( path );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setSecure( boolean secure )
	{
		cookie.setSecure( secure );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setValue( String value )
	{
		cookie.setValue( value );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie setWrap( boolean wrap )
	{
		cookie.setWrap( wrap );
		needsUpdating = true;
		return this;
	}

	@Override
	public HoneyCookie unsetNeedsUpdating()
	{
		needsUpdating = false;
		return this;
	}
}
