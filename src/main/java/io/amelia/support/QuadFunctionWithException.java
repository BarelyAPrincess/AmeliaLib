/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.Objects;

@FunctionalInterface
public interface QuadFunctionWithException<T, Y, U, W, R, E extends Exception>
{
	/**
	 * Returns a composed function that first applies this function to
	 * its input, and then applies the {@code after} function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <V>   the type of output of the {@code after} function, and of the
	 *              composed function
	 * @param after the function to apply after this function is applied
	 *
	 * @return a composed function that first applies this function and then
	 * applies the {@code after} function
	 *
	 * @throws NullPointerException if after is null
	 */
	default <V> QuadFunctionWithException<T, Y, U, W, V, E> andThen( FunctionWithException<? super R, ? extends V, E> after ) throws E
	{
		Objs.isNotNull( after );
		return ( T t, Y y, U u, W w ) -> after.apply( apply( t, y, u, w ) );
	}

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param t the first function argument
	 * @param y the second function argument
	 * @param u the third function argument
	 * @param w the fourth function argument
	 *
	 * @return the function result
	 */
	R apply( T t, Y y, U u, W w ) throws E;
}