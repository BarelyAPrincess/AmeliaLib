/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.util.function.Supplier;

import io.amelia.support.Namespace;

public interface KeyValueSetterTrait<ValueType, ExceptionClass extends Exception>
{
	default void setValue( String key, ValueType value ) throws ExceptionClass
	{
		setValue( Namespace.of( key ), value );
	}

	void setValue( Namespace key, ValueType value ) throws ExceptionClass;

	default void setValue( TypeBase type, ValueType value ) throws ExceptionClass
	{
		setValue( type.getPath(), value );
	}

	default void setValueIfAbsent( TypeBase.TypeWithDefault<? extends ValueType> type ) throws ExceptionClass
	{
		setValueIfAbsent( type.getPath(), type.getDefaultSupplier() );
	}

	default void setValueIfAbsent( String key, Supplier<? extends ValueType> value ) throws ExceptionClass
	{
		setValueIfAbsent( Namespace.of( key ), value );
	}

	void setValueIfAbsent( Namespace key, Supplier<? extends ValueType> value ) throws ExceptionClass;

	default void setValues( KeyValueGetterTrait<ValueType, ?> values ) throws ExceptionClass
	{
		for ( Namespace key : values.getKeys() )
			values.getValue( key ).ifPresent( value -> setValue( key, value ) );
	}
}
