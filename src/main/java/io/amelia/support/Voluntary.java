package io.amelia.support;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
public class Voluntary<Type>
{
	/**
	 * Common instance for {@code empty()}.
	 */
	private static final Voluntary<?> EMPTY = new Voluntary<>();

	/**
	 * Common instance for {@code empty()}.
	 */
	private static final VoluntaryWithCause<?, ?> EMPTY_WITH_CAUSE = new VoluntaryWithCause<>();

	/**
	 * Returns an empty {@code Voluntary} instance.  No value is present for this
	 * Voluntary.
	 *
	 * @param <T> Type of the non-existent value
	 *
	 * @return an empty {@code Voluntary}
	 *
	 * @apiNote Though it may be tempting to do so, avoid testing if an object
	 * is empty by comparing with {@code ==} against instances returned by
	 * {@code Option.empty()}. There is no guarantee that it is a singleton.
	 * Instead, use {@link #isPresent()}.
	 */
	public static <T> Voluntary<T> empty()
	{
		@SuppressWarnings( "unchecked" )
		Voluntary<T> t = ( Voluntary<T> ) EMPTY;
		return t;
	}

	/**
	 * Returns an empty {@code VoluntaryWithCause} instance.  No value is present for this
	 * VoluntaryWithCause.
	 *
	 * @param <T> Type of the non-existent value
	 * @param <C> Exception of the non-existent cause
	 *
	 * @return an empty {@code VoluntaryWithCause}
	 *
	 * @apiNote Though it may be tempting to do so, avoid testing if an object
	 * is empty by comparing with {@code ==} against instances returned by
	 * {@code Option.empty()}. There is no guarantee that it is a singleton.
	 * Instead, use {@link #isPresent()}.
	 */
	public static <T, C extends Exception> VoluntaryWithCause<T, C> emptyWithCause()
	{
		@SuppressWarnings( "unchecked" )
		VoluntaryWithCause<T, C> t = ( VoluntaryWithCause<T, C> ) EMPTY_WITH_CAUSE;
		return t;
	}

	/**
	 * Returns an {@code Voluntary} with the specified present non-null value.
	 *
	 * @param <T>   the class of the value
	 * @param value the value to be present, which must be non-null
	 *
	 * @return an {@code Voluntary} with the value present
	 *
	 * @throws NullPointerException if value is null
	 */
	public static <T> Voluntary<T> of( T value )
	{
		return new Voluntary<>( value );
	}

	public static <T> Voluntary<T> of( @Nonnull Optional<T> value )
	{
		return value.map( Voluntary::of ).orElseGet( Voluntary::empty );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofElseException( T value, Supplier<C> cause )
	{
		if ( value == null )
			return withException( cause );
		return ofWithCause( value );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofElseException( T value, C cause )
	{
		if ( value == null )
			return withException( cause );
		return ofWithCause( value );
	}

	/**
	 * Returns an {@code Voluntary} describing the specified value, if non-null,
	 * otherwise returns an empty {@code Voluntary}.
	 *
	 * @param <T>   the class of the value
	 * @param value the possibly-null value to describe
	 *
	 * @return an {@code Voluntary} with a present value if the specified value
	 * is non-null, otherwise an empty {@code Voluntary}
	 */
	public static <T> Voluntary<T> ofNullable( @Nullable T value )
	{
		return value == null ? empty() : of( value );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofNullableWithCause( @Nullable T value )
	{
		return value == null ? emptyWithCause() : ofWithCause( value );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofNullableWithCause( @Nullable T value, @Nullable C cause )
	{
		return value == null ? cause == null ? emptyWithCause() : withException( cause ) : ofWithCause( value, cause );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofNullableWithCause( @Nullable T value, @Nullable Supplier<C> cause )
	{
		return value == null ? cause == null ? emptyWithCause() : withException( cause ) : ofWithCause( value, cause.get() );
	}

	/**
	 * Returns an {@code VoluntaryWithCause} with the specified present non-null value.
	 *
	 * @param <T>   the class of the value
	 * @param value the value to be present, which must be non-null
	 *
	 * @return an {@code VoluntaryWithCause} with the value present
	 *
	 * @throws NullPointerException if value is null
	 */
	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofWithCause( T value )
	{
		return new VoluntaryWithCause<>( value );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofWithCause( T value, C cause )
	{
		return new VoluntaryWithCause<>( value, cause );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> ofWithCause( @Nonnull Optional<T> value )
	{
		return ( VoluntaryWithCause<T, C> ) value.map( VoluntaryWithCause::of ).orElseGet( VoluntaryWithCause::empty );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> withException( @Nullable C cause )
	{
		return new VoluntaryWithCause<>( cause );
	}

	public static <T, C extends Exception> VoluntaryWithCause<T, C> withException( @Nonnull Supplier<C> cause )
	{
		return new VoluntaryWithCause<>( cause.get() );
	}

	public static <T> VoluntaryWithCause<T, NullPointerException> withNullPointerException( @Nullable T value )
	{
		if ( value == null )
			return withException( new NullPointerException() );
		return ofWithCause( value );
	}

	/**
	 * If non-null, the value; if null, indicates no value is present
	 */
	protected final Type value;

	/**
	 * Constructs an empty instance.
	 *
	 * @implNote Generally only one empty instance, {@link Voluntary#EMPTY},
	 * should exist per VM.
	 */
	Voluntary()
	{
		this.value = null;
	}

	/**
	 * Constructs an instance with the value present.
	 *
	 * @param value the non-null value to be present
	 *
	 * @throws NullPointerException if value is null
	 */
	Voluntary( Type value )
	{
		this.value = Objs.notNull( value );
	}

	/**
	 * Indicates whether some other object is "equal to" this Voluntary. The
	 * other object is considered equal if:
	 * <ul>
	 * <li>it is also an {@code Voluntary} and;
	 * <li>both instances have no value present or;
	 * <li>the present values are "equal to" each other via {@code equals()}.
	 * </ul>
	 *
	 * @param obj an object to be tested for equality
	 *
	 * @return {code true} if the other object is "equal to" this object
	 * otherwise {@code false}
	 */
	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj )
		{
			return true;
		}

		if ( !( obj instanceof Voluntary ) )
		{
			return false;
		}

		Voluntary<?> other = ( Voluntary<?> ) obj;
		return Objects.equals( value, other.value );
	}

	/**
	 * If a value is present, and the value matches the given predicate,
	 * return an {@code Voluntary} describing the value, otherwise return an
	 * empty {@code Voluntary}.
	 *
	 * @param predicate a predicate to apply to the value, if present
	 *
	 * @return an {@code Voluntary} describing the value of this {@code Voluntary}
	 * if a value is present and the value matches the given predicate,
	 * otherwise an empty {@code Voluntary}
	 *
	 * @throws NullPointerException if the predicate is null
	 */
	public Voluntary<Type> filter( Predicate<? super Type> predicate )
	{
		Objs.notNull( predicate );
		if ( !isPresent() )
			return this;
		else
			return predicate.test( value ) ? this : empty();
	}

	/**
	 * If a value is present, apply the provided {@code Voluntary}-bearing
	 * mapping function to it, return that result, otherwise return an empty
	 * {@code Voluntary}.  This method is similar to {@link #map(Function)},
	 * but the provided mapper is one whose result is already an {@code Voluntary},
	 * and if invoked, {@code flatMap} does not wrap it with an additional
	 * {@code Voluntary}.
	 *
	 * @param <U>    The type parameter to the {@code Voluntary} returned by
	 * @param mapper a mapping function to apply to the value, if present
	 *               the mapping function
	 *
	 * @return the result of applying an {@code Voluntary}-bearing mapping
	 * function to the value of this {@code Voluntary}, if a value is present,
	 * otherwise an empty {@code Voluntary}
	 *
	 * @throws NullPointerException if the mapping function is null or returns
	 *                              a null result
	 */
	public <U> Voluntary<U> flatMap( @Nonnull Function<? super Type, Voluntary<U>> mapper )
	{
		Objs.notNull( mapper );
		if ( isPresent() )
			return Objs.notNull( mapper.apply( value ) );
		else
			return empty();
	}

	public <U> Voluntary<U> flatMapCompatible( @Nonnull Function<? super Type, Optional<U>> mapper )
	{
		Objs.notNull( mapper );
		if ( isPresent() )
			return Voluntary.of( Objs.notNull( mapper.apply( value ) ) );
		else
			return Voluntary.empty();
	}

	/**
	 * If a value is present in this {@code Voluntary}, returns the value,
	 * otherwise throws {@code NoSuchElementException}.
	 *
	 * @return the non-null value held by this {@code Voluntary}
	 *
	 * @throws NoSuchElementException if there is no value present
	 * @see Voluntary#isPresent()
	 */
	public Type get()
	{
		if ( value == null )
			throw new NoSuchElementException( "No value present" );
		return value;
	}

	/**
	 * Defines specification that a value is present and no error was thrown.
	 */
	public boolean hasSucceeded()
	{
		return value != null;
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
		return Objects.hashCode( value );
	}

	/**
	 * If Voluntary has no value, the value will be retrieved from the supplied voluntary.
	 * If the supplied Voluntary has not errored, this Voluntary's cause will be copied.
	 */
	public <T extends Type> Voluntary<T> ifAbsentGet( Supplier<Voluntary<T>> supplier )
	{
		if ( !isPresent() )
			return supplier.get();
		return ( Voluntary<T> ) this;
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
	public <X extends Exception> Voluntary<Type> ifPresent( ConsumerWithException<? super Type, X> consumer ) throws X
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
	 * Similar to {@link #ifPresent(ConsumerWithException)}, except returns a new {@code Voluntary}
	 * that will contain any thrown exceptions, otherwise a {@code Voluntary} that contains the present value.
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
	 * and if the result is non-null, return an {@code Voluntary} describing the
	 * result.  Otherwise return an empty {@code Voluntary}.
	 *
	 * @param <U>    The type of the result of the mapping function
	 * @param mapper a mapping function to apply to the value, if present
	 *
	 * @return an {@code Voluntary} describing the result of applying a mapping
	 * function to the value of this {@code Voluntary}, if a value is present,
	 * otherwise an empty {@code Voluntary}
	 *
	 * @throws NullPointerException if the mapping function is null
	 * @apiNote This method supports post-processing on optional values, without
	 * the need to explicitly check for a return status.  For example, the
	 * following code traverses a stream of file names, selects one that has
	 * not yet been processed, and then opens that file, returning an
	 * {@code Voluntary<FileInputStream>}:
	 *
	 * <pre>{@code
	 *     Voluntary<FileInputStream> fis =
	 *         names.stream().filter(name -> !isProcessedYet(name))
	 *                       .findFirst()
	 *                       .map(name -> new FileInputStream(name));
	 * }</pre>
	 *
	 * Here, {@code findFirst} returns an {@code Voluntary<String>}, and then
	 * {@code map} returns an {@code Voluntary<FileInputStream>} for the desired
	 * file if one exists.
	 */
	public <U> Voluntary<U> map( @Nonnull Function<? super Type, ? extends U> mapper )
	{
		if ( isPresent() )
			return Voluntary.ofNullable( mapper.apply( value ) );
		else
			return empty();
	}

	public <U, X extends Exception> VoluntaryWithCause<U, X> mapCatchException( @Nonnull FunctionWithException<? super Type, ? extends U, X> mapper )
	{
		if ( isPresent() )
			try
			{
				return ( VoluntaryWithCause<U, X> ) VoluntaryWithCause.ofNullable( mapper.apply( value ) );
			}
			catch ( Exception e )
			{
				return withException( ( X ) e );
			}
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
	 * Return the contained value, if present, otherwise throw an exception
	 * to be created by the provided supplier.
	 *
	 * @param <X>      Type of the exception to be thrown
	 * @param supplier The supplier which will return the exception to
	 *                 be thrown
	 *
	 * @return the present value
	 *
	 * @throws X                    if there is no value present
	 * @throws NullPointerException if no value is present and
	 *                              {@code exceptionSupplier} is null
	 * @apiNote A method reference to the exception constructor with an empty
	 * argument list can be used as the supplier. For example,
	 * {@code IllegalStateException::new}
	 */
	public <X extends Exception> Type orElseThrow( @Nonnull Supplier<X> supplier ) throws X
	{
		if ( value == null )
			throw supplier.get();
		else
			return value;
	}

	public <C extends Exception> Voluntary<Type> removeException()
	{
		return ofNullable( value );
	}

	/**
	 * Returns a non-empty string representation of this Voluntary suitable for
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
		return value != null ? String.format( "Voluntary[%s]", value ) : "Voluntary.empty";
	}

	public <X extends Exception> VoluntaryWithCause<Type, X> withCause( X cause )
	{
		return ofNullableWithCause( value, cause );
	}

	public <X extends Exception> VoluntaryWithCause<Type, X> withCause( Supplier<X> cause )
	{
		return ofNullableWithCause( value, cause.get() );
	}
}
