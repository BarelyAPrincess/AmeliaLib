package io.amelia.data;

import java.util.function.Function;
import java.util.function.Supplier;

import io.amelia.support.Voluntary;

public interface ValueGetterTrait<ValueType>
{
	Voluntary getValue( Function<ValueType, ValueType> computeFunction );

	Voluntary getValue( Supplier<ValueType> supplier );

	Voluntary getValue();

	boolean hasValue();
}
