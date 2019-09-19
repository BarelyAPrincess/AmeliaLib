/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * Our own extended binding so we can better track if and when a binding variable is changed
 */
public interface ScriptBinding
{
	static ScriptBinding newInstance()
	{
		return null;
	}

	static <K, V> ScriptBinding newInstance( HashMap<K, V> kvHashMap )
	{
		return null;
	}

	Object getVariable( String key );

	Map<String, Object> getVariables();

	void setVariable( String key, Object val );
}