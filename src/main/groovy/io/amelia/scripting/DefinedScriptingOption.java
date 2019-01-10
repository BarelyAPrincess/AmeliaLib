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

import io.amelia.support.Voluntary;

public class DefinedScriptingOption
{
	String value;

	public Voluntary<String> getValue()
	{
		return Voluntary.ofNullable( value );
	}

	public static class KeyValue extends DefinedScriptingOption
	{
		private final String key;

		KeyValue( String key, String value )
		{
			this.key = key;
			this.value = value;
		}

		public String getKey()
		{
			return key;
		}
	}

	public static class Scripting extends DefinedScriptingOption
	{
		ScriptingOption option;

		Scripting( ScriptingOption option, String value )
		{
			this.option = option;
			this.value = value;
		}

		public io.amelia.scripting.ScriptingOption getOption()
		{
			return option;
		}
	}
}
