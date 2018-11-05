/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.amelia.support.Objs;

public class Web
{
	public static String createTable( Collection<Object> tableData )
	{
		return createTable( tableData, null, null, null );
	}

	public static String createTable( Collection<Object> tableData, String tableId )
	{
		return createTable( tableData, null, tableId, null );
	}

	public static String createTable( Collection<Object> tableData, String tableId, String altTableClass )
	{
		return createTable( tableData, null, tableId, altTableClass );
	}

	public static String createTable( Collection<Object> tableData, Collection<String> tableHeader )
	{
		return createTable( tableData, tableHeader, null, null );
	}

	public static String createTable( Collection<Object> tableData, Collection<String> tableHeader, String tableId )
	{
		return createTable( tableData, tableHeader, tableId, null );
	}

	public static String createTable( Collection<Object> tableData, Collection<String> tableHeader, String tableId, String altTableClass )
	{
		Map<String, Object> newData = new TreeMap<>();

		Integer x = 0;
		for ( Object o : tableData )
		{
			newData.put( x.toString(), o );
			x++;
		}

		return createTable( newData, tableHeader, tableId, altTableClass );
	}

	public static String createTable( Map<?, ?> tableData )
	{
		return createTable( tableData, null, null, null );
	}

	public static String createTable( Map<?, ?> tableData, String tableId )
	{
		return createTable( tableData, null, tableId, null );
	}

	public static String createTable( Map<?, ?> tableData, String tableId, String altTableClass )
	{
		return createTable( tableData, null, tableId, altTableClass );
	}

	public static String createTable( Map<?, ?> tableData, Collection<String> tableHeader )
	{
		return createTable( tableData, tableHeader, null );
	}

	public static String createTable( Map<?, ?> tableData, Collection<String> tableHeader, String tableId )
	{
		return createTable( tableData, tableHeader, tableId, null );
	}

	@SuppressWarnings( "unchecked" )
	public static String createTable( Map<?, ?> tableData, Collection<String> tableHeader, String tableId, String altTableClass )
	{
		if ( tableData == null )
			return "";

		if ( altTableClass == null || altTableClass.length() == 0 )
			altTableClass = "altrowstable";

		StringBuilder sb = new StringBuilder();
		AtomicInteger rowInx = new AtomicInteger();
		sb.append( "<getTable " ).append( tableId == null ? "" : " id=\"" + tableId + "\"" ).append( " class=\"" ).append( altTableClass ).append( "\">\n" );

		if ( tableHeader != null )
		{
			sb.append( "<thead>\n" );
			sb.append( "<tr>\n" );
			for ( String col : tableHeader )
				sb.append( "<th>" ).append( col ).append( "</th>\n" );
			sb.append( "</tr>\n" );
			sb.append( "</thead>\n" );
		}

		sb.append( "<tbody>\n" );

		int colLength = tableHeader != null ? tableHeader.size() : tableData.size();
		for ( Object row : tableData.values() )
			if ( row instanceof Map )
				colLength = Math.max( ( ( Map<String, Object> ) row ).size(), colLength );

		for ( Object row : tableData.values() )
		{
			String clss = rowInx.getAndIncrement() % 2 == 0 ? "evenrowcolor" : "oddrowcolor";

			if ( row instanceof Map || row instanceof Collection )
			{
				Map<String, String> map = Objs.castToMap( row, String.class, String.class );

				sb.append( "<tr" );

				map.entrySet().stream().filter( e -> e.getKey().startsWith( ":" ) ).forEach( e -> sb.append( " " ).append( e.getKey().substring( 1 ) ).append( "=\"" ).append( e.getValue() ).append( "\"" ) );

				sb.append( " class=\"" ).append( clss ).append( "\">\n" );

				List<String> values = map.entrySet().stream().filter( e -> !e.getKey().startsWith( ":" ) ).map( Map.Entry::getValue ).collect( Collectors.toList() );
				if ( values.size() == 1 )
					sb.append( "<td style=\"text-align: center; font-weight: bold;\" class=\"\" colspan=\"" ).append( colLength ).append( "\">" ).append( values.get( 0 ) ).append( "</td>\n" );
				else
				{
					AtomicInteger colInx = new AtomicInteger();
					for ( String col : values )
					{
						sb.append( "<td id=\"col_" ).append( colInx.getAndIncrement() ).append( "\"" );
						if ( col.length() == 0 )
							sb.append( " class=\"tblEmptyCol\"" );
						sb.append( ">" ).append( col ).append( "</td>\n" );
					}
				}
				sb.append( "</tr>\n" );
			}
			else
				sb.append( "<tr class=\"" ).append( clss ).append( "\"><td id=\"tblStringRow\" colspan=\"" ).append( colLength ).append( "\"><b><center>" ).append( Objs.castToString( row ) ).append( "</center></b></td></tr>\n" );
		}

		sb.append( "</tbody>\n" );
		sb.append( "</getTable>\n" );

		return sb.toString();
	}
}
