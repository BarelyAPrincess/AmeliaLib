/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.storage.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.KeyValueGetterTrait;
import io.amelia.data.KeyValueSetterTrait;
import io.amelia.data.KeyValueTypesTrait;
import io.amelia.lang.StorageException;
import io.amelia.support.Namespace;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;

public class TableStorageType implements StorageType
{
	List<Record> records = new ArrayList<>();

	public TableStorageType()
	{

	}

	public void commit()
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

	public class Record implements KeyValueTypesTrait, KeyValueGetterTrait<Object, StorageException.Error>, KeyValueSetterTrait<Object, StorageException.Error>
	{
		Map<String, Object> columns = new HashMap<>();

		@Override
		public Set<Namespace> getKeys()
		{
			return columns.keySet().stream().map( Namespace::of ).collect( Collectors.toSet() );
		}

		@Override
		public Voluntary<Object> getValue( @Nonnull Namespace key )
		{
			if ( key.getNodeCount() > 1 )
				throw new StorageException.Ignorable( "Keys can only be one level deep." );
			return getValue( key.getString() );
		}

		@Override
		public Voluntary<Object> getValue( @Nonnull String key )
		{
			return VoluntaryWithCause.ofNullableWithCause( columns.get( key ) );
		}

		@Override
		public Voluntary<Object> getValue()
		{
			return getValue( "id" );
		}

		@Override
		public boolean hasValue( String key )
		{
			return columns.containsKey( key );
		}

		@Override
		public boolean hasValue( Namespace key )
		{
			if ( key.getNodeCount() > 1 )
				throw new StorageException.Ignorable( "Keys can only be one level deep." );
			return columns.containsKey( key.getString() );
		}

		@Override
		public void setValue( String key, Object value ) throws StorageException.Error
		{
			if ( !columns.containsKey( key ) )
				throw new StorageException.Error( key + " is not a key in the this record. Use the create column method first." );
			columns.put( key, value );
		}

		@Override
		public void setValue( Namespace key, Object value ) throws StorageException.Error
		{
			if ( key.getNodeCount() > 1 )
				throw new StorageException.Error( "Keys can only be one level deep." );
			setValue( key.getString(), value );
		}

		@Override
		public void setValueIfAbsent( String key, Supplier<?> value ) throws StorageException.Error
		{
			if ( !hasValue( key ) )
				setValue( key, value );
		}

		@Override
		public void setValueIfAbsent( Namespace key, Supplier<?> value ) throws StorageException.Error
		{
			if ( key.getNodeCount() > 1 )
				throw new StorageException.Error( "Keys can only be one level deep." );
			setValueIfAbsent( key.getString(), value );
		}
	}
}
