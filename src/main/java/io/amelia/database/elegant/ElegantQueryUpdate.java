/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.database.Database;
import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.elegant.types.Limit;
import io.amelia.database.elegant.types.Values;
import io.amelia.database.elegant.types.Where;
import io.amelia.database.elegant.types.WhereItem;
import io.amelia.lang.DatabaseException;

/**
 * SQL Query for Update
 */
public final class ElegantQueryUpdate extends ElegantQueryResult<ElegantQueryUpdate> implements Values<ElegantQueryUpdate>, Where<ElegantQueryUpdate, ElegantQueryUpdate>, Limit<ElegantQueryUpdate>, Cloneable
{
	private final List<WhereItem> elements = new LinkedList<>();
	private final Map<String, Object> values = new HashMap<>();
	private WhereItem.Divider currentSeparator = WhereItem.Divider.NONE;
	private int limit = -1;
	private int offset = -1;

	public ElegantQueryUpdate( DatabaseDriver driver, String table )
	{
		super( driver, table, false );
	}

	public ElegantQueryUpdate( DatabaseDriver driver, String table, boolean autoExecute )
	{
		super( driver, table, autoExecute );
	}

	@Override
	public ElegantQueryUpdate and()
	{
		if ( elements.size() < 1 )
			currentSeparator = WhereItem.Divider.NONE;
		else
			currentSeparator = WhereItem.Divider.AND;
		return this;
	}

	@Override
	public ElegantQueryUpdate clone0()
	{
		ElegantQueryUpdate clone = new ElegantQueryUpdate( getDriver(), getTable() );

		clone.currentSeparator = this.currentSeparator;
		clone.elements.addAll( this.elements );
		clone.values.putAll( this.values );
		clone.offset = this.offset;
		clone.limit = this.limit;

		return clone;
	}

	@Override
	public List<WhereItem> getElements()
	{
		return Collections.unmodifiableList( elements );
	}

	@Override
	public int getLimit()
	{
		return limit;
	}

	@Override
	public int getOffset()
	{
		return offset;
	}

	public Map<String, Object> getValues()
	{
		return Collections.unmodifiableMap( values );
	}

	@Override
	public WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate> group()
	{
		WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate> group = new WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate>( this, this );
		group.seperator( currentSeparator );
		elements.add( group );
		or();
		notifyChanges();
		return group;
	}

	@Override
	public ElegantQueryUpdate limit( int limit )
	{
		this.limit = limit;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQueryUpdate limit( int limit, int offset )
	{
		this.limit = limit;
		this.offset = offset;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQueryUpdate offset( int offset )
	{
		this.offset = offset;
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQueryUpdate or()
	{
		if ( elements.size() < 1 )
			currentSeparator = WhereItem.Divider.NONE;
		else
			currentSeparator = WhereItem.Divider.OR;
		return this;
	}

	@Override
	protected void preExecute() throws DatabaseException
	{

	}

	@Override
	public WhereItem.Divider separator()
	{
		return currentSeparator;
	}

	@Override
	public ElegantQueryUpdate skip( int skip )
	{
		return this.offset( skip );
	}

	@Override
	public ElegantQueryUpdate take( int take )
	{
		return this.limit( take );
	}

	@Override
	public ElegantQueryUpdate value( String key, Object val )
	{
		values.put( key, val );
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQueryUpdate values( Map<String, Object> map )
	{
		for ( Entry<String, Object> e : map.entrySet() )
			values.put( e.getKey(), e.getValue() );
		notifyChanges();
		return this;
	}

	@Override
	public ElegantQueryUpdate values( String[] keys, Object[] valuesArray )
	{
		for ( int i = 0; i < Math.min( keys.length, valuesArray.length ); i++ )
			values.put( keys[i], valuesArray[i] );

		if ( keys.length != valuesArray.length )
			Database.L.warning( "keys/values were omitted due to length mismatch. (Keys: " + keys.length + ", Values: " + valuesArray.length + ")" );

		notifyChanges();
		return this;
	}

	@Override
	public ElegantQueryUpdate where( WhereItem element )
	{
		element.seperator( currentSeparator );
		elements.add( element );
		and();
		notifyChanges();
		return this;
	}

	@Override
	public WhereItem.KeyValue<ElegantQueryUpdate> where( String key )
	{
		return new WhereItem.KeyValue<ElegantQueryUpdate>( this, key );
	}

	@Override
	public ElegantQueryUpdate whereMatches( Collection<String> valueKeys, Collection<Object> valueValues )
	{
		WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate> group = new WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate>( this, this );

		List<String> listKeys = new ArrayList<>( valueKeys );
		List<Object> listValues = new ArrayList<>( valueValues );

		for ( int i = 0; i < Math.min( listKeys.size(), listValues.size() ); i++ )
		{
			WhereItem.KeyValue<WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate>> groupElement = group.where( listKeys.get( i ) );
			groupElement.seperator( WhereItem.Divider.AND );
			groupElement.matches( listValues.get( i ) );
		}

		group.parent();
		or();
		return this;
	}

	@Override
	public ElegantQueryUpdate whereMatches( Map<String, Object> values )
	{
		WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate> group = new WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate>( this, this );

		for ( Entry<String, Object> val : values.entrySet() )
		{
			WhereItem.KeyValue<WhereItem.Group<ElegantQueryUpdate, ElegantQueryUpdate>> groupElement = group.where( val.getKey() );
			groupElement.seperator( WhereItem.Divider.AND );
			groupElement.matches( val.getValue() );
		}

		group.parent();
		or();
		return this;
	}

	@Override
	public ElegantQueryUpdate whereMatches( String key, Object value )
	{
		return new WhereItem.KeyValue<ElegantQueryUpdate>( this, key ).matches( value );
	}
}
