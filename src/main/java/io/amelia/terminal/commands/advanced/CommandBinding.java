/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.terminal.commands.advanced;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.permissions.PermissibleEntity;
import io.amelia.permissions.PermissionNamespace;

public class CommandBinding
{
	protected Method method;
	protected Object object;
	protected Map<String, String> params = new HashMap<>();
	
	public CommandBinding( Object object, Method method )
	{
		this.object = object;
		this.method = method;
	}
	
	public void call( Object... args ) throws Exception
	{
		method.invoke( object, args );
	}
	
	public boolean checkPermissions( PermissibleEntity entity )
	{
		String permission = getMethodAnnotation().permission();
		
		if ( permission.contains( "<" ) )
			for ( Entry<String, String> entry : getParams().entrySet() )
				if ( entry.getValue() != null )
					permission = permission.replace( "<" + entry.getKey() + ">", entry.getValue().toLowerCase() );
		
		return entity.checkPermission( PermissionNamespace.of( permission ) ).isTrue();
	}
	
	public CommandHandler getMethodAnnotation()
	{
		return method.getAnnotation( CommandHandler.class );
	}
	
	public Map<String, String> getParams()
	{
		return params;
	}
	
	public void setParams( Map<String, String> params )
	{
		this.params = params;
	}
}
