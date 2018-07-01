/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data.apache;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ApacheDirectiveComparator implements Comparator<ApacheDirective>
{
	private static final List<String> KEY_ORDER = new LinkedList<>();

	static
	{
		KEY_ORDER.add( "Directory" );
		KEY_ORDER.add( "DirectoryMatch" );
		KEY_ORDER.add( "Files" );
		KEY_ORDER.add( "FilesMatch" );
		KEY_ORDER.add( "Location" );
		KEY_ORDER.add( "LocationMatch" );
		KEY_ORDER.add( "If" );
	}

	@Override
	public int compare( ApacheDirective left, ApacheDirective right )
	{
		return Integer.compare( KEY_ORDER.indexOf( left.getKey() ), KEY_ORDER.indexOf( right.getKey() ) );
	}
}
