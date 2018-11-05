/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;
import java.util.List;

import io.amelia.support.Strs;

public class ScriptingOption
{
	private final String name;
	private final List<String> additionalKeyNames;

	public ScriptingOption( @NotNull String name, String... additionalKeyNames )
	{
		this.name = name;
		this.additionalKeyNames = Strs.toLowerCase( Arrays.asList( additionalKeyNames ) );
	}

	public boolean matches( String key )
	{
		key = key.toLowerCase();
		return name.equalsIgnoreCase( key ) || additionalKeyNames.contains( key );
	}

	public static class Bool extends ScriptingOption
	{
		private final boolean def;

		public Bool( String name, boolean def, String... additionalKeyNames )
		{
			super( name, additionalKeyNames );
			this.def = def;
		}

		public boolean getDefault()
		{
			return def;
		}
	}

	public static class Int extends ScriptingOption
	{
		private final int def;

		public Int( String name, int def, String... additionalKeyNames )
		{
			super( name, additionalKeyNames );
			this.def = def;
		}

		public int getDefault()
		{
			return def;
		}
	}
}
