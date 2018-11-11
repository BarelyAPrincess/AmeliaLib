package io.amelia.data;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.support.Voluntary;

public interface KeyValueGetterTrait<ValueType>
{
	Voluntary getValue( String key, Function<ValueType, ValueType> computeFunction );

	Voluntary getValue( String key, Supplier<ValueType> supplier );

	Voluntary getValue( @Nonnull String key );

	boolean hasValue( String key );
}
