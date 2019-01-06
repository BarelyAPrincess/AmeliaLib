/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.util.function.Function;
import java.util.function.Supplier;

import io.amelia.support.VoluntaryWithCause;

public interface ValueGetterTrait<ValueType>
{
	VoluntaryWithCause getValue( Function<ValueType, ValueType> computeFunction );

	VoluntaryWithCause getValue( Supplier<ValueType> supplier );

	VoluntaryWithCause getValue();

	boolean hasValue();
}
