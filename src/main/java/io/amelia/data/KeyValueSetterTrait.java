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

public interface KeyValueSetterTrait<ValueType, ExceptionClass extends Exception>
{
	void setValue( String key, ValueType value ) throws ExceptionClass;

	default void setValue( TypeBase type, ValueType value ) throws ExceptionClass
	{
		setValue( type.getPath(), value );
	}

	default void setValueIfAbsent( TypeBase.TypeWithDefault<? extends ValueType> type ) throws ExceptionClass
	{
		setValueIfAbsent( type.getPath(), type.getDefaultSupplier() );
	}

	void setValueIfAbsent( String key, Supplier<? extends ValueType> value ) throws ExceptionClass;

	default void setValues( KeyValueGetterTrait<ValueType, ?> values ) throws ExceptionClass
	{
		for ( String key : values.getKeys() )
			values.getValue( key ).ifPresent( value -> setValue( key, value ) );
	}
}
