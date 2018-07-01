/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.database.elegant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.amelia.database.drivers.DatabaseDriver;
import io.amelia.database.elegant.types.Limit;
import io.amelia.database.elegant.types.Where;
import io.amelia.database.elegant.types.WhereItem;
import io.amelia.lang.DatabaseException;

public final class ElegantQueryDelete extends ElegantQueryResult<ElegantQueryDelete> implements Where<ElegantQueryDelete, ElegantQueryDelete>, Limit<ElegantQueryDelete>
{
	private final List<WhereItem> elements = new LinkedList<>();
	private final List<Object> sqlValues = new LinkedList<>();
	private WhereItem.Divider currentSeparator = WhereItem.Divider.NONE;
	private int limit = -1;
	private boolean needsUpdate = true;
	private int offset = -1;

	public ElegantQueryDelete( DatabaseDriver sql, String table )
	{
		super( sql, table, false );
	}

	public ElegantQueryDelete( DatabaseDriver sql, String table, boolean autoExecute )
	{
		super( sql, table, autoExecute );
	}

	@Override
	public ElegantQueryDelete and()
	{
		if ( elements.size() < 1 )
			currentSeparator = WhereItem.Divider.NONE;
		else
			currentSeparator = WhereItem.Divider.AND;
		return this;
	}

	@Override
	public ElegantQueryDelete clone0()
	{
		ElegantQueryDelete clone = new ElegantQueryDelete( getDriver(), getTable() );

		clone.currentSeparator = this.currentSeparator;
		clone.elements.addAll( this.elements );
		clone.sqlValues.addAll( this.sqlValues );
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

	@Override
	public WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete> group()
	{
		WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete> group = new WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete>( this, this );
		group.seperator( currentSeparator );
		elements.add( group );
		needsUpdate = true;
		or();
		return group;
	}

	@Override
	public ElegantQueryDelete limit( int limit )
	{
		this.limit = limit;
		needsUpdate = true;
		return this;
	}

	@Override
	public ElegantQueryDelete limit( int limit, int offset )
	{
		this.limit = limit;
		this.offset = offset;
		needsUpdate = true;
		return this;
	}

	@Override
	public ElegantQueryDelete offset( int offset )
	{
		this.offset = offset;
		needsUpdate = true;
		return this;
	}

	@Override
	public ElegantQueryDelete or()
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
	public ElegantQueryDelete skip( int skip )
	{
		return this.offset( skip );
	}

	@Override
	public ElegantQueryDelete take( int take )
	{
		return this.limit( take );
	}

	@Override
	public ElegantQueryDelete where( WhereItem element )
	{
		element.seperator( currentSeparator );
		elements.add( element );
		needsUpdate = true;
		and();

		return this;
	}

	@Override
	public WhereItem.KeyValue<ElegantQueryDelete> where( String key )
	{
		return new WhereItem.KeyValue<>( this, key );
	}

	@Override
	public ElegantQueryDelete whereMatches( Collection<String> valueKeys, Collection<Object> valueValues )
	{
		WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete> group = new WhereItem.Group<>( this, this );

		List<String> listKeys = new ArrayList<>( valueKeys );
		List<Object> listValues = new ArrayList<>( valueValues );

		for ( int i = 0; i < Math.min( listKeys.size(), listValues.size() ); i++ )
		{
			WhereItem.KeyValue<WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete>> groupElement = group.where( listKeys.get( i ) );
			groupElement.seperator( WhereItem.Divider.AND );
			groupElement.matches( listValues.get( i ) );
		}

		group.parent();
		or();
		return this;
	}

	@Override
	public ElegantQueryDelete whereMatches( Map<String, Object> values )
	{
		WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete> group = new WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete>( this, this );

		for ( Entry<String, Object> val : values.entrySet() )
		{
			WhereItem.KeyValue<WhereItem.Group<ElegantQueryDelete, ElegantQueryDelete>> groupElement = group.where( val.getKey() );
			groupElement.seperator( WhereItem.Divider.AND );
			groupElement.matches( val.getValue() );
		}

		group.parent();
		or();
		return this;
	}

	@Override
	public ElegantQueryDelete whereMatches( String key, Object value )
	{
		return new WhereItem.KeyValue<>( this, key ).matches( value );
	}
}
