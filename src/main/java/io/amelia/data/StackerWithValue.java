/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.foundation.Kernel;
import io.amelia.support.Objs;
import io.amelia.support.Pair;

@SuppressWarnings( "unchecked" )
public abstract class StackerWithValue<BaseClass extends StackerWithValue<BaseClass, ValueType>, ValueType> extends StackerBase<BaseClass>
{
	public static final int LISTENER_VALUE_CHANGE = 0x02;
	public static final int LISTENER_VALUE_STORE = 0x03;
	public static final int LISTENER_VALUE_REMOVE = 0x04;
	protected volatile ValueType value;

	protected StackerWithValue( @Nonnull BiFunction<BaseClass, String, BaseClass> creator, @Nonnull String key )
	{
		this( creator, null, key, null );
	}

	protected StackerWithValue( @Nonnull BiFunction<BaseClass, String, BaseClass> creator, BaseClass parent, @Nonnull String key )
	{
		this( creator, parent, key, null );
	}

	protected StackerWithValue( @Nonnull BiFunction<BaseClass, String, BaseClass> creator, BaseClass parent, @Nonnull String key, ValueType value )
	{
		super( creator, parent, key );
		this.value = value;
	}

	public final int addValueChangeListener( StackerListener.OnValueChange<BaseClass, ValueType> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_CHANGE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1], ( ValueType ) objs[2] );
			}
		} );
	}

	public final int addValueRemoveListener( StackerListener.OnValueRemove<BaseClass, ValueType> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_REMOVE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1] );
			}
		} );
	}

	public final int addValueStoreListener( StackerListener.OnValueStore<BaseClass, ValueType> function, StackerListener.Flags... flags )
	{
		return addListener( new StackerListener.Container( LISTENER_VALUE_STORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1] );
			}
		} );
	}

	public <O> Optional<O> asObject( Class<O> cls )
	{
		try
		{
			Constructor<?> constructor = cls.getConstructor( StackerBase.class );
			return Optional.of( ( O ) constructor.newInstance( this ) );
		}
		catch ( Exception ignore )
		{
		}

		try
		{
			Constructor<?> constructor = cls.getConstructor();
			Object instance = constructor.newInstance();

			for ( Field field : cls.getFields() )
			{
				BaseClass child = findChild( field.getName(), false ).orElse( null );

				if ( child == null )
				{
					Kernel.L.warning( "Could not assign field " + field.getName() + " with type " + field.getType() + " within class " + cls.getSimpleName() + "." );
					continue;
				}

				field.setAccessible( true );

				Object obj = child.getValue().orElse( null );
				boolean assigned = true;

				if ( obj != null )
				{
					if ( obj.getClass().isAssignableFrom( field.getType() ) )
						field.set( instance, obj );
					else if ( String.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToString( value ) );
					else if ( Double.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToDouble( value ) );
					else if ( Integer.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToInt( value ) );
					else if ( Long.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToLong( value ) );
					else if ( Boolean.class.isAssignableFrom( field.getType() ) )
						field.set( instance, Objs.castToBoolean( value ) );
					else
						assigned = false;
				}

				if ( field.get( instance ) == null )
					assigned = false;

				if ( !assigned )
				{
					Object o = child.asObject( field.getType() );
					if ( o == null )
						Kernel.L.severe( "Could not cast field " + field.getName() + " with type " + field.getType() + " with value " + value.getClass().getSimpleName() + " within class" + cls.getSimpleName() + "." );
					else
						field.set( instance, o );
				}
			}

			//for ( Field field : cls.getFields() )
			//	if ( field.get( instance ) == null && !field.isSynthetic() )
			//		Kernel.L.warning( "The field " + field.getProductName() + " is unassigned for object " + cls );

			return ( Optional<O> ) instance;
		}
		catch ( Exception ignore )
		{
		}

		return Optional.empty();
	}

	@Override
	public void destroy()
	{
		super.destroy();
		updateValue( null );
	}

	public Stream<ValueType> flatValues()
	{
		disposeCheck();
		Stream<ValueType> stream = children.stream().flatMap( StackerWithValue::flatValues );
		return Optional.ofNullable( value ).map( t -> Stream.concat( Stream.of( t ), stream ) ).orElse( stream );
	}

	public <LT extends ValueType> List<LT> getChildAsList( String key, Class<LT> type )
	{
		return findChild( key, false ).map( child -> child.getChildren().map( c -> Objs.castTo( c.value, type ) ).filter( Objects::nonNull ).collect( Collectors.toList() ) ).orElse( null );
	}

	public <ExpectedValueType extends ValueType> List<ExpectedValueType> getChildAsList( String key )
	{
		return findChild( key, false ).map( child -> child.getChildren().map( c -> ( ExpectedValueType ) c.value ).filter( Objects::nonNull ).collect( Collectors.toList() ) ).orElse( null );
	}

	public <T> Optional<T> getChildAsObject( @Nonnull String key, Class<T> cls )
	{
		return findChild( key, false ).flatMap( child -> child.asObject( cls ) );
	}

	public <ExpectedValueType extends ValueType> Map<String, ExpectedValueType> getChildrenAsMap()
	{
		return children.stream().collect( Collectors.toMap( StackerBase::getName, c -> ( ExpectedValueType ) c.value ) );
	}

	public <ExpectedValueType extends ValueType> Map<String, ExpectedValueType> getChildrenAsMap( String key )
	{
		return findChild( key, false ).map( child -> ( Map<String, ExpectedValueType> ) child.getChildrenAsMap() ).orElse( null );
	}

	public <ExpectedValueType extends ValueType> Stream<Pair<String, ExpectedValueType>> getChildrenWithKeys()
	{
		return children.stream().map( c -> new Pair( c.getName(), c.value ) );
	}

	public Optional<ValueType> getValue( String key, Function<ValueType, ValueType> computeFunction )
	{
		return findChild( key, true ).flatMap( child -> child.getValue( computeFunction ) );
	}

	public Optional<ValueType> getValue( Function<ValueType, ValueType> computeFunction )
	{
		ValueType value = getValue().orElse( null );
		ValueType newValue = computeFunction.apply( value );
		if ( value != newValue )
			setValue( newValue );
		return Optional.ofNullable( newValue );
	}

	public Optional<ValueType> getValue( String key, Supplier<ValueType> supplier )
	{
		return findChild( key, true ).flatMap( child -> child.getValue( supplier ) );
	}

	public Optional<ValueType> getValue( Supplier<ValueType> supplier )
	{
		if ( !hasValue() )
			setValue( supplier.get() );
		return getValue();
	}

	public Optional<ValueType> getValue( String key )
	{
		return findChild( key, false ).flatMap( StackerWithValue::getValue );
	}

	public Optional<ValueType> getValue()
	{
		disposeCheck();
		return Optional.ofNullable( value );
	}

	public void setValue( ValueType value )
	{
		disposeCheck();
		notFlag( Flag.READ_ONLY );
		if ( hasFlag( Flag.NO_OVERRIDE ) && hasValue() )
			throwExceptionIgnorable( getCurrentPath() + " has NO_OVERRIDE flag" );
		if ( value instanceof StackerBase )
			throwExceptionIgnorable( "The value can't be of class StackerBase, please use the appropriate methods instead, e.g. SetChild(), MoveChild(), CopyChild(), etc." );
		updateValue( value );
	}

	public final boolean hasValue( String key )
	{
		return findChild( key, false ).map( StackerWithValue::hasValue ).orElse( false );
	}

	public boolean hasValue()
	{
		return value != null;
	}

	@Override
	protected boolean isTrimmable0()
	{
		return !hasValue();
	}

	@Override
	public void copyChild( @Nonnull BaseClass child )
	{
		super.copyChild( child );
		updateValue( child.value );
	}

	public Optional<ValueType> pollValue()
	{
		return Optional.ofNullable( updateValue( null ) );
	}

	public Optional<ValueType> pollValue( String key )
	{
		return findChild( key, false ).flatMap( StackerWithValue::pollValue );
	}

	public void setValue( String key, ValueType value )
	{
		getChildOrCreate( key ).setValue( value );
	}

	public void setValueIfAbsent( ValueType value )
	{
		if ( !hasValue() )
			setValue( value );
	}

	public void setValueIfAbsent( TypeBase.TypeWithDefault type )
	{
		getChildOrCreate( type.getPath() ).setValueIfAbsent( ( ValueType ) type.getDefault() );
	}

	public void setValueIfAbsent( String key, ValueType value )
	{
		getChildOrCreate( key ).setValueIfAbsent( value );
	}

	/**
	 * Used privately to update the value and call the proper value listener.
	 */
	protected ValueType updateValue( ValueType value )
	{
		if ( this.value == null && value != null )
			fireListener( LISTENER_VALUE_STORE, value );
		if ( this.value != null && value == null )
			fireListener( LISTENER_VALUE_REMOVE, this.value );
		fireListener( LISTENER_VALUE_CHANGE, this.value, value );
		ValueType oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public Map<String, ValueType> values()
	{
		disposeCheck();
		return children.stream().filter( StackerWithValue::hasValue ).collect( Collectors.toMap( StackerWithValue::getName, c -> c.getValue().orElse( null ) ) );
	}
}
