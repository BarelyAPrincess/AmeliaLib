package io.amelia.data;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.support.Voluntary;

public interface KeyValueGetterTrait<ValueType, ExceptionClass extends Exception>
{
	Set<String> getKeys();

	default Voluntary<ValueType, ExceptionClass> getValue( String key, Function<ValueType, ValueType> computeFunction )
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
		return Voluntary.ofNullable( newValue );
	}

	default Voluntary<ValueType, ExceptionClass> getValue( String key, Supplier<ValueType> supplier )
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

	Voluntary<ValueType, ExceptionClass> getValue( @Nonnull String key );

	boolean hasValue( String key );
}
