/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.lang.DatabaseException;
import io.amelia.database.DatabaseManager;
import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.elegant.support.ElegantQueryResult;
import io.amelia.database.elegant.types.GroupBy;
import io.amelia.database.elegant.types.Limit;
import io.amelia.database.elegant.types.OrderBy;
import io.amelia.database.elegant.types.Where;
import io.amelia.database.elegant.types.WhereItem;
import io.amelia.support.Lists;

public final class ElegantQuerySelect extends ElegantQueryResult<ElegantQuerySelect> implements Where<ElegantQuerySelect, ElegantQuerySelect>, Limit<ElegantQuerySelect>, OrderBy<ElegantQuerySelect>, GroupBy<ElegantQuerySelect>, Cloneable
{
	private WhereItem.Divider currentSeparator = WhereItem.Divider.NONE;
	private final List<WhereItem> elements = new LinkedList<>();
	private final List<String> orderBy = new LinkedList<>();
	private final List<String> groupBy = new LinkedList<>();
	private final List<String> fields = new LinkedList<>();
	private boolean orderAscending = true;
	private boolean orderRand = false;
	private int offset = -1;
	private int limit = -1;

	public ElegantQuerySelect( DatabaseDriver driver, String table )
	{
		super( driver, table, true );
	}

	public ElegantQuerySelect( DatabaseDriver sql, String table, boolean autoExecute )
	{
		super( sql, table, autoExecute );
	}

	@Override
	public ElegantQuerySelect and()
	{
		if ( elements.size() < 1 )
			currentSeparator = WhereItem.Divider.NONE;
		else
			currentSeparator = WhereItem.Divider.AND;
		return this;
	}

	public ElegantQuerySelect fields( Collection<String> fields )
	{
		this.fields.addAll( fields );
		notifyChanges();
		return this;
	}

	public ElegantQuerySelect fields( String field )
	{
		fields.add( field );
		notifyChanges();
		return this;
	}

	public ElegantQuerySelect fields( String... fields )
	{
		this.fields.addAll( Arrays.asList( fields ) );
		notifyChanges();
		return this;
	}

	@Override
	public WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect> group()
	{
		WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect> group = new WhereItem.Group<>( this, this );
		group.seperator( currentSeparator );
		elements.add( group );
		or();
		notifyChanges();
		return group;
	}

	@Override
	public ElegantQuerySelect groupBy( Collection<String> columns )
	{
		groupBy.addAll( columns );
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect groupBy( String... columns )
	{
		groupBy.addAll( Arrays.asList( columns ) );
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect groupBy( String column )
	{
		groupBy.add( column );
		notifyChanges();
		return this;
	}

	@Override
	public int getLimit()
	{
		return limit;
	}

	@Override
	public ElegantQuerySelect limit( int limit )
	{
		this.limit = limit;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect take( int take )
	{
		return this.limit( take );
	}

	@Override
	public ElegantQuerySelect limit( int limit, int offset )
	{
		this.limit = limit;
		this.offset = offset;
		notifyChanges();
		return this;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	@Override
	public ElegantQuerySelect offset( int offset )
	{
		this.offset = offset;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect skip( int skip )
	{
		return this.offset( skip );
	}

	@Override
	public ElegantQuerySelect or()
	{
		if ( elements.size() < 1 )
			currentSeparator = WhereItem.Divider.NONE;
		else
			currentSeparator = WhereItem.Divider.OR;
		return this;
	}

	@Override
	public WhereItem.Divider separator()
	{
		return currentSeparator;
	}

	@Override
	public ElegantQuerySelect orderDesc()
	{
		orderAscending = false;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect orderAsc()
	{
		orderAscending = true;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect orderBy( String column )
	{
		return orderBy( Lists.newArrayList( column ) );
	}

	@Override
	public ElegantQuerySelect orderBy( String column, String dir )
	{
		return orderBy( Lists.newArrayList( column ), dir );
	}

	@Override
	public ElegantQuerySelect orderBy( Collection<String> columns )
	{
		orderBy.addAll( columns );
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect orderBy( Collection<String> columns, String dir )
	{
		orderBy.addAll( columns );

		if ( dir.trim().equalsIgnoreCase( "asc" ) )
			orderAsc();
		else if ( dir.trim().equalsIgnoreCase( "desc" ) )
			orderDesc();
		else
			throw new IllegalArgumentException( dir + " is not a valid sorting direction." );

		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect rand()
	{
		return rand( true );
	}

	@Override
	public ElegantQuerySelect rand( boolean rand )
	{
		if ( rand )
			orderBy.clear();
		orderRand = rand;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQuerySelect where( Map<String, Object> map )
	{
		for ( Entry<String, Object> e : map.entrySet() )
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

					@SuppressWarnings( "unchecked" )
					Map<String, Object> submap = ( Map<String, Object> ) val;
					for ( Entry<String, Object> e2 : submap.entrySet() )
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
					DatabaseManager.L.severe( ee );
				}
			else
				where( key ).matches( val );
		}

		return this;
	}

	@Override
	public ElegantQuerySelect where( WhereItem element )
	{
		element.seperator( currentSeparator );
		elements.add( element );
		and();
		notifyChanges();
		return this;
	}

	@Override
	public WhereItem.KeyValue<ElegantQuerySelect> where( String key )
	{
		return new WhereItem.KeyValue<>( this, key );
	}

	@Override
	public ElegantQuerySelect whereMatches( Collection<String> valueKeys, Collection<Object> valueValues )
	{
		WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect> group = new WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect>( this, this );

		List<String> listKeys = new ArrayList<>( valueKeys );
		List<Object> listValues = new ArrayList<>( valueValues );

		for ( int i = 0; i < Math.min( listKeys.size(), listValues.size() ); i++ )
		{
			WhereItem.KeyValue<WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect>> groupElement = group.where( listKeys.get( i ) );
			groupElement.seperator( WhereItem.Divider.AND );
			groupElement.matches( listValues.get( i ) );
		}

		group.parent();
		or();
		return this;
	}

	@Override
	public ElegantQuerySelect whereMatches( Map<String, Object> values )
	{
		WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect> group = new WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect>( this, this );

		for ( Entry<String, Object> val : values.entrySet() )
		{
			WhereItem.KeyValue<WhereItem.Group<ElegantQuerySelect, ElegantQuerySelect>> groupElement = group.where( val.getKey() );
			groupElement.seperator( WhereItem.Divider.AND );
			groupElement.matches( val.getValue() );
		}

		group.parent();
		or();
		return this;
	}

	@Override
	public ElegantQuerySelect whereMatches( String key, Object value )
	{
		return new WhereItem.KeyValue<ElegantQuerySelect>( this, key ).matches( value );
	}

	@Override
	protected void preExecute() throws DatabaseException
	{

	}

	@Override
	public boolean isOrderRand()
	{
		return orderRand;
	}

	@Override
	public boolean isOrderAscending()
	{
		return orderAscending;
	}

	@Override
	public List<String> getOrderBy()
	{
		return Collections.unmodifiableList( orderBy );
	}

	@Override
	public List<String> getGroupBy()
	{
		return Collections.unmodifiableList( groupBy );
	}

	@Override
	public List<WhereItem> getElements()
	{
		return Collections.unmodifiableList( elements );
	}

	public List<String> getFields()
	{
		return Collections.unmodifiableList( fields );
	}

	public boolean isUpdateQuery()
	{
		return false;
	}

	@Override
	public ElegantQuerySelect clone0()
	{
		ElegantQuerySelect clone = new ElegantQuerySelect( getDriver(), getTable() );

		clone.elements.addAll( this.elements );
		clone.currentSeparator = this.currentSeparator;
		clone.orderBy.addAll( this.orderBy );
		clone.orderAscending = this.orderAscending;
		clone.groupBy.addAll( this.groupBy );
		clone.fields.addAll( this.fields );
		clone.limit = this.limit;
		clone.offset = this.offset;

		return clone;
	}
}
