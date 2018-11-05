/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.apache;

import java.util.ArrayList;
import java.util.List;

public class ApacheDirective
{
	protected final List<String> arguments;
	protected final String key;
	protected final ApacheSection parent;
	protected int lineNum;
	protected String source;

	public ApacheDirective()
	{
		parent = null;
		key = null;
		arguments = new ArrayList<>();
	}

	public ApacheDirective( ApacheSection parent, String key, List<String> arguments )
	{
		this.parent = parent;
		this.key = key;
		this.arguments = arguments;
	}

	public String[] getArguments()
	{
		return arguments.toArray( new String[0] );
	}

	public String getKey()
	{
		return key;
	}

	public void hasArguments( int required, String describ ) throws ApacheDirectiveException
	{
		if ( describ == null || describ.length() == 0 )
			for ( int i = 0; i < required; i++ )
				describ += "<arg" + i + "> ";

		if ( arguments.size() < required )
			throw new ApacheDirectiveException( "Directive '" + key + "' missing required number of arguments, e.g., " + key + " " + describ );
	}

	public void isSection() throws ApacheDirectiveException
	{
		if ( !( this instanceof ApacheSection ) )
			throw new ApacheDirectiveException( "Directive '" + key + "' must be surrounded by brackets." );
	}
}
