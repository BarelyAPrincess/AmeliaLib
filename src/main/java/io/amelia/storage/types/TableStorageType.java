/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.KeyValueGetterTrait;
import io.amelia.data.KeyValueSetterTrait;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.lang.StorageException;
import io.amelia.support.Voluntary;

public class TableStorageType implements StorageType
{
	List<Record> records = new ArrayList<>();

	public TableStorageType()
	{

	}

	public int count()
	{
		return records.size();
	}

	public Stream<Record> getRecords()
	{
		return records.stream();
	}

	public void commit()
	{

	}

	public class Record implements KeyValueTypesTrait, KeyValueGetterTrait<Object, StorageException.Error>, KeyValueSetterTrait<Object, StorageException.Error>
	{
		Map<String, Object> columns = new HashMap<>();

		@Override
		public Set<String> getKeys()
		{
			return columns.keySet();
		}

		@Override
		public Voluntary getValue( @Nonnull String key )
		{
			return Voluntary.ofNullable( columns.get( key ) );
		}

		@Override
		public Voluntary getValue()
		{
			return getValue( "id" );
		}

		@Override
		public boolean hasValue( String key )
		{
			return columns.containsKey( key );
		}

		@Override
		public void setValue( String key, Object value ) throws StorageException.Error
		{
			if ( !columns.containsKey( key ) )
				throw new StorageException.Error( key + " is not a key in the this record. Use the create column method first." );
			columns.put( key, value );
		}

		@Override
		public void setValueIfAbsent( String key, Object value ) throws StorageException.Error
		{
			if ( !hasValue( key ) )
				setValue( key, value );
		}
	}
}
