/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant.types;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import io.amelia.database.Database;

/**
 * Provides a skeleton interface for SQL queries implementing the where methods
 */
public interface Where<B extends Where<?, ?>, P>
{
	B and();

	List<WhereItem> getElements();

	WhereItem.Group<B, P> group();

	B or();

	WhereItem.Divider separator();

	WhereItem.KeyValue<B> where( String key );

	B where( WhereItem element );

	@SuppressWarnings( "unchecked" )
	default B where( @Nonnull Map<String, Object> map )
	{
		for ( Map.Entry<String, Object> e : map.entrySet() )
		{
			String key = e.getKey();
			Object val = e.getValue();

			if ( key.startsWith( "|" ) )
			{
				key = key.substring( 1 );
				or();
			}
			else if ( key.startsWith( "&" ) )
			{
				key = key.substring( 1 );
				and();
			}

			if ( val instanceof Map )
				try
				{
					WhereItem.Group<?, ?> group = group();

					Map<String, Object> submap = ( Map<String, Object> ) val;
					for ( Map.Entry<String, Object> e2 : submap.entrySet() )
					{
						String key2 = e2.getKey();
						Object val2 = e2.getValue();

						if ( key2.startsWith( "|" ) )
						{
							key2 = key2.substring( 1 );
							group.or();
						}
						else if ( key2.startsWith( "&" ) )
						{
							key2 = key2.substring( 1 );
							group.and();
						}

						where( key2 ).matches( val2 );
					}
				}
				catch ( ClassCastException ee )
				{
					Database.L.severe( ee );
				}
			else
				where( key ).matches( val );
		}

		return ( B ) this;
	}

	B whereMatches( Map<String, Object> values );

	B whereMatches( String key, Object value );

	B whereMatches( Collection<String> valueKeys, Collection<Object> valueValues );
}
