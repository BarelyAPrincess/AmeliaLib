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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * An expanded container object which may or may not contain a non-null value and/or exception, is similar to {@link Optional} but adds error handling.
 * If a value is present, {@code isPresent()} will return {@code true} and {@code get()} will return the value.
 * If a cause is present, {@code hasErrored()} will return {@code true} and {@code getException()} will return the cause.
 *
 * <p>Additional methods that depend on the presence or absence of a contained
 * value are provided, such as {@link #orElse(java.lang.Object) orElse()}
 * (return a default value if value not present) and
 * {@link #ifPresent(io.amelia.support.ConsumerWithException) ifPresent()} (execute a block
 * of code if the value is present).
 *
 * <p>Additional methods for interfacing with Java 8 features are also present, such as {@link #of(Optional)}.
 * Methods ending with "compatible" are intended in mimic the logic of similar methods found in {@link Optional}, e.g., {@link #flatMapCompatible(Function)}
 *
 * @apiNote This api feature is still in incubating status. Major changes to the API could happen at anytime.
 * @see Optional
 */
public final class VoluntaryWithCause<Type, Cause extends Exception> extends Voluntary<Type>
{

	private final Cause cause;

	VoluntaryWithCause()
	{
		super();
		this.cause = null;
	}

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 *
	 * @throws NullPointerException if value is null
	 */
	VoluntaryWithCause( Type value )
	{
		super( value );
		this.cause = null;
	}

	VoluntaryWithCause( Type value, Cause cause )
	{
		super( value );
		this.cause = cause;
	}

	VoluntaryWithCause( Cause cause )
	{
		super();
		this.cause = cause;
	}

	/**
	 * If a value is present, and the value matches the given predicate,
	 * return an {@code VoluntaryWithCause} describing the value, otherwise return an
	 * empty {@code VoluntaryWithCause}.
	 *
	 * @param predicate a predicate to apply to the value, if present
	 *
	 * @return an {@code VoluntaryWithCause} describing the value of this {@code VoluntaryWithCause}
	 * if a value is present and the value matches the given predicate,
	 * otherwise an empty {@code VoluntaryWithCause}
	 *
	 * @throws NullPointerException if the predicate is null
	 */
	public VoluntaryWithCause<Type, Cause> filter( Predicate<? super Type> predicate )
	{
		Objs.notNull( predicate );
		if ( !isPresent() )
			return this;
		else
			return predicate.test( value ) ? this : emptyWithCause();
	}

	public <U> VoluntaryWithCause<U, Cause> flatMapCompatible( @Nonnull Function<? super Type, Optional<U>> mapper )
	{
		Objs.notNull( mapper );
		if ( isPresent() )
			return ofWithCause( Objs.notNull( mapper.apply( value ) ) );
		else
			return emptyWithCause();
	}

	/**
	 * If a value is present, apply the provided {@code VoluntaryWithCause}-bearing
	 * mapping function to it, return that result, otherwise return an empty
	 * {@code VoluntaryWithCause}.  This method is similar to {@link #map(Function)},
	 * but the provided mapper is one whose result is already an {@code VoluntaryWithCause},
	 * and if invoked, {@code flatMap} does not wrap it with an additional
	 * {@code VoluntaryWithCause}.
	 *
	 * @param <U>    The type parameter to the {@code VoluntaryWithCause} returned by
	 * @param mapper a mapping function to apply to the value, if present
	 *               the mapping function
	 *
	 * @return the result of applying an {@code VoluntaryWithCause}-bearing mapping
	 * function to the value of this {@code VoluntaryWithCause}, if a value is present,
	 * otherwise an empty {@code VoluntaryWithCause}
	 *
	 * @throws NullPointerException if the mapping function is null or returns
	 *                              a null result
	 */
	public <U, C extends Exception> VoluntaryWithCause<U, C> flatMapWithCause( @Nonnull Function<? super Type, VoluntaryWithCause<U, C>> mapper )
	{
		Objs.notNull( mapper );
		if ( isPresent() )
			return Objs.notNull( mapper.apply( value ) );
		else
			return emptyWithCause();
	}

	public Cause getCause()
	{
		if ( cause == null )
			throw new NoSuchElementException( "No cause present" );
		return cause;
	}

	public <C extends Exception> VoluntaryWithCause<Type, C> hasErrored( @Nonnull BiFunction<? super Type, ? super Cause, VoluntaryWithCause<Type, C>> mapper )
	{
		if ( hasErrored() )
			return mapper.apply( value, cause );
		else
			return ( VoluntaryWithCause<Type, C> ) this;
	}

	public boolean hasErrored()
	{
		return cause != null;
	}

	public <X extends Exception> VoluntaryWithCause<Type, X> hasErroredMap( Function<Cause, X> causeMapFunction )
	{
		if ( hasErrored() )
			return ofNullableWithCause( value, causeMapFunction.apply( cause ) );
		return ( VoluntaryWithCause<Type, X> ) this;
	}

	public <T extends Type, X extends Exception> VoluntaryWithCause<T, X> hasNotErrored( FunctionWithException<Type, T, X> mapper )
	{
		if ( cause == null )
			try
			{
				return ofNullableWithCause( mapper.apply( value ) );
			}
			catch ( Exception e )
			{
				return withException( ( X ) e );
			}
		return ( VoluntaryWithCause<T, X> ) this;
	}

	public <X extends Exception> VoluntaryWithCause<Type, X> hasNotErrored( Supplier<X> causeSupplier )
	{
		if ( !hasErrored() )
			return ofNullableWithCause( value, causeSupplier.get() );
		return ( VoluntaryWithCause<Type, X> ) this;
	}

	public <T extends Type, X extends Exception> VoluntaryWithCause<T, X> hasNotErroredFlat( Function<Type, VoluntaryWithCause<T, X>> mapper )
	{
		if ( cause == null )
			return mapper.apply( value );
		return ( VoluntaryWithCause<T, X> ) this;
	}

	/**
	 * Defines specification that a value is present and no error was thrown.
	 */
	public boolean hasSucceeded()
	{
		return value != null && cause == null;
	}

	/**
	 * Returns the hash code value of the present value, if any, or 0 (zero) if
	 * no value is present.
	 *
	 * @return hash code value of the present value or 0 if no value is present
	 */
	@Override
	public int hashCode()
	{
		return Objects.hashCode( value ) & Objects.hashCode( cause );
	}

	/**
	 * If VoluntaryWithCause has no value, the value will be retrieved from the supplied voluntary.
	 * If the supplied VoluntaryWithCause has not errored, this VoluntaryWithCause's cause will be copied.
	 */
	public <T extends Type, X extends Exception> VoluntaryWithCause<T, X> ifAbsentGetWithCause( Supplier<VoluntaryWithCause<T, X>> supplier )
	{
		if ( !isPresent() )
			return supplier.get().hasNotErrored( () -> ( X ) cause );
		return ( VoluntaryWithCause<T, X> ) this;
	}

	/**
	 * If a value is present, invoke the specified consumer with the value,
	 * otherwise do nothing.
	 *
	 * @param consumer block to be executed if a value is present
	 *
	 * @throws NullPointerException if value is present and {@code consumer} is
	 *                              null
	 */
	public <X extends Exception> VoluntaryWithCause<Type, Cause> ifPresent( ConsumerWithException<? super Type, X> consumer ) throws X
	{
		if ( value != null )
		{
			try
			{
				consumer.accept( value );
			}
			catch ( Exception e )
			{
				throw ( X ) e;
			}
		}
		return this;
	}

	/**
	 * Similar to {@link #ifPresent(ConsumerWithException)}, except returns a new {@code VoluntaryWithCause}
	 * that will contain any thrown exceptions, otherwise a {@code VoluntaryWithCause} that contains the present value.
	 */
	public <X extends Exception> VoluntaryWithCause<Type, X> ifPresentCatchException( ConsumerWithException<? super Type, X> consumer )
	{
		if ( value != null )
			try
			{
				consumer.accept( value );
			}
			catch ( Exception e )
			{
				return withException( ( X ) e );
			}
		return ofNullableWithCause( value );
	}

	/**
	 * Return {@code true} if there is a value present, otherwise {@code false}.
	 *
	 * @return {@code true} if there is a value present, otherwise {@code false}
	 */
	public boolean isPresent()
	{
		return value != null;
	}

	/**
	 * If a value is present, apply the provided mapping function to it,
	 * and if the result is non-null, return an {@code VoluntaryWithCause} describing the
	 * result.  Otherwise return an empty {@code VoluntaryWithCause}.
	 *
	 * @param <U>    The type of the result of the mapping function
	 * @param mapper a mapping function to apply to the value, if present
	 *
	 * @return an {@code VoluntaryWithCause} describing the result of applying a mapping
	 * function to the value of this {@code VoluntaryWithCause}, if a value is present,
	 * otherwise an empty {@code VoluntaryWithCause}
	 *
	 * @throws NullPointerException if the mapping function is null
	 * @apiNote This method supports post-processing on optional values, without
	 * the need to explicitly check for a return status.  For example, the
	 * following code traverses a stream of file names, selects one that has
	 * not yet been processed, and then opens that file, returning an
	 * {@code VoluntaryWithCause<FileInputStream>}:
	 *
	 * <pre>{@code
	 *     VoluntaryWithCause<FileInputStream> fis =
	 *         names.stream().filter(name -> !isProcessedYet(name))
	 *                       .findFirst()
	 *                       .map(name -> new FileInputStream(name));
	 * }</pre>
	 *
	 * Here, {@code findFirst} returns an {@code VoluntaryWithCause<String>}, and then
	 * {@code map} returns an {@code VoluntaryWithCause<FileInputStream>} for the desired
	 * file if one exists.
	 */
	public <U> VoluntaryWithCause<U, Cause> map( @Nonnull Function<? super Type, ? extends U> mapper )
	{
		if ( isPresent() )
			return VoluntaryWithCause.ofNullableWithCause( mapper.apply( value ), cause );
		else
			return emptyWithCause();
	}

	/**
	 * Return the value if present, otherwise return {@code other}.
	 *
	 * @param other the value to be returned if there is no value present, may
	 *              be null
	 *
	 * @return the value, if present, otherwise {@code other}
	 */
	public Type orElse( Type other )
	{
		return value != null ? value : other;
	}

	/**
	 * Return the value if present, otherwise invoke {@code other} and return
	 * the result of that invocation.
	 *
	 * @param other a {@code Supplier} whose result is returned if no value
	 *              is present
	 *
	 * @return the value if present otherwise the result of {@code other.get()}
	 *
	 * @throws NullPointerException if value is not present and {@code other} is
	 *                              null
	 */
	public Type orElseGet( Supplier<? extends Type> other )
	{
		return value != null ? value : other.get();
	}

	/**
	 * Attempts to return a non-null value.
	 * If value is null, then we always call the provided function and return its returned value instead.
	 * Great for simply doing {@link Exception#printStackTrace()}.
	 * Caution cause is nullable.
	 */
	public Type orElseHandleCause( @Nonnull Function<Cause, Type> causeFunction )
	{
		if ( value != null )
			return value;
		return causeFunction.apply( cause );
	}

	public Type orElseHandleCause( @Nonnull Consumer<Cause> causeConsumer, Supplier<? extends Type> other )
	{
		if ( value != null )
			return value;
		causeConsumer.accept( cause );
		return other.get();
	}

	public Type orElseHandleCause( @Nonnull Consumer<Cause> causeConsumer, Type other )
	{
		if ( value != null )
			return value;
		causeConsumer.accept( cause );
		return other;
	}

	public Type orElseThrowCause() throws Cause
	{
		if ( value == null )
		{
			if ( cause == null )
				throw new NoSuchElementException( "value and cause were both null" );
			else
				throw cause;
		}
		else
			return value;
	}

	public <X extends Exception> Type orElseThrowCause( @Nonnull Function<Cause, X> function ) throws X
	{
		if ( value == null )
			throw function.apply( cause );
		else
			return value;
	}

	public Voluntary<Type> removeException()
	{
		return ofNullable( value );
	}

	/**
	 * Returns a non-empty string representation of this VoluntaryWithCause suitable for
	 * debugging. The exact presentation format is unspecified and may vary
	 * between implementations and versions.
	 *
	 * @return the string representation of this instance
	 *
	 * @implSpec If a value is present the result must include its string
	 * representation in the result. Empty and present OptionalChilds must be
	 * unambiguously differentiable.
	 */
	@Override
	public String toString()
	{
		return value != null ? String.format( "VoluntaryWithCause[%s]", value ) : "VoluntaryWithCause.empty";
	}
}
