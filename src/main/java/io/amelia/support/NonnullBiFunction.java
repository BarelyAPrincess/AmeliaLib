/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a function that accepts two arguments and produces a result.
 * Will throw {@link NullPointerException} if either the supplied argument or returned result are null.
 * This is the two-arity specialization of {@link java.util.function.Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <InputTypeA> the type of the first argument to the function
 * @param <InputTypeB> the type of the second argument to the function
 * @param <ResultType> the type of the result of the function
 *
 * @see java.util.function.Function
 * @since 1.8
 */
@FunctionalInterface
public interface NonnullBiFunction<InputTypeA, InputTypeB, ResultType>
{

	/**
	 * Applies this function to the given arguments.
	 *
	 * @param varA the first function argument
	 * @param varB the second function argument
	 *
	 * @return the function result
	 */
	@Nonnull
	default ResultType apply( @Nullable InputTypeA varA, @Nullable InputTypeB varB )
	{
		return Objs.notNull( apply0( Objs.notNull( varA ), Objs.notNull( varB ) ) );
	}

	/**
	 * Applies this function to the given argument.
	 * DO NOT CALL! Will circumvent null-check.
	 *
	 * @param varA the first function argument
	 * @param varB the second function argument
	 *
	 * @return the function result
	 */
	@Nonnull
	ResultType apply0( @Nonnull InputTypeA varA, @Nonnull InputTypeB varB );

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
	default <V> NonnullBiFunction<InputTypeA, InputTypeB, V> andThen( Function<? super ResultType, ? extends V> after )
	{
		Objs.notNull( after );
		return ( InputTypeA t, InputTypeB u ) -> after.apply( apply( t, u ) );
	}
}