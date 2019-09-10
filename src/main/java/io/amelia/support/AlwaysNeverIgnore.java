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

public enum AlwaysNeverIgnore
{
	Always,
	Never,
	Ignore;

	public static AlwaysNeverIgnore parse( String value, AlwaysNeverIgnore def )
	{
		try
		{
			return parse( value );
		}
		catch ( IllegalArgumentException e )
		{
			return def;
		}
	}

	public static AlwaysNeverIgnore parse( String value )
	{
		if ( value == null || value.length() == 0 || value.equalsIgnoreCase( "ignore" ) )
			return Ignore;
		if ( value.equalsIgnoreCase( "disallow" ) || value.equalsIgnoreCase( "deny" ) || value.equalsIgnoreCase( "disabled" ) || value.equalsIgnoreCase( "never" ) )
			return Never;
		if ( value.equalsIgnoreCase( "force" ) || value.equalsIgnoreCase( "allow" ) || value.equalsIgnoreCase( "enabled" ) || value.equalsIgnoreCase( "always" ) )
			return Always;
		throw new IllegalArgumentException( String.format( "Value %s is invalid, the available options are Ignore, Disallow, Deny, Disabled, Never, Force, Allow, Enabled, Always.", value ) );
	}
}
