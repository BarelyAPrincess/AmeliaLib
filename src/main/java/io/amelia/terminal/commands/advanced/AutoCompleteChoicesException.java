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

public class AutoCompleteChoicesException extends RuntimeException
{
	private static final long serialVersionUID = -8163025621439288595L;

	protected String argName;
	protected String[] choices;

	public AutoCompleteChoicesException( String[] choices, String argName )
	{
		super();
		this.choices = choices;
		this.argName = argName;
	}

	public String getArgName()
	{
		return argName;
	}

	public String[] getChoices()
	{
		return choices;
	}
}
