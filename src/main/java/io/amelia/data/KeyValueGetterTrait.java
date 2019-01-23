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

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.support.Namespace;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;

public interface KeyValueGetterTrait<ValueType, ExceptionClass extends Exception>
{
	Set<Namespace> getKeys();

	default Voluntary<ValueType> getValue( String key, Function<ValueType, ValueType> computeFunction )
	{
		return getValue( Namespace.of( key ), computeFunction );
	}

	default Voluntary<ValueType> getValue( Namespace key, Function<ValueType, ValueType> computeFunction )
	{
		ValueType value = getValue( key ).orElse( null );
		ValueType newValue = computeFunction.apply( value );
		if ( value != newValue )
			try
			{
				if ( this instanceof KeyValueSetterTrait )
					( ( KeyValueSetterTrait ) this ).setValue( key, newValue );
			}
			catch ( Exception e )
			{
				return Voluntary.withException( ( ExceptionClass ) e );
			}
		return VoluntaryWithCause.ofNullableWithCause( newValue );
	}

	default Voluntary<ValueType> getValue( String key, Supplier<ValueType> supplier )
	{
		return getValue( Namespace.of( key ), supplier );
	}

	default Voluntary<ValueType> getValue( Namespace key, Supplier<ValueType> supplier )
	{
		if ( !hasValue( key ) )
			try
			{
				if ( this instanceof KeyValueSetterTrait )
					( ( KeyValueSetterTrait ) this ).setValue( key, supplier.get() );
			}
			catch ( Exception e )
			{
				return Voluntary.withException( ( ExceptionClass ) e );
			}
		return getValue( key );
	}

	Voluntary<ValueType> getValue( @Nonnull Namespace key );

	default Voluntary<ValueType> getValue( @Nonnull String key )
	{
		return getValue( Namespace.of( key ) );
	}

	default boolean hasValue( String key )
	{
		return hasValue( Namespace.of( key ) );
	}

	boolean hasValue( Namespace key );
}
