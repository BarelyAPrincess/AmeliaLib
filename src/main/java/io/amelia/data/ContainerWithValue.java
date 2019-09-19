/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.amelia.data.parcel.Parcel;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ContainerException;
import io.amelia.lang.ParcelableException;
import io.amelia.support.BiFunctionWithException;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.Pair;
import io.amelia.support.Voluntary;
import io.amelia.support.VoluntaryWithCause;

@SuppressWarnings( "unchecked" )
public abstract class ContainerWithValue<BaseClass extends ContainerWithValue<BaseClass, ValueType, ExceptionClass>, ValueType, ExceptionClass extends ApplicationException.Error> extends ContainerBase<BaseClass, ExceptionClass> implements KeyValueSetterTrait<ValueType, ExceptionClass>, ValueSetterTrait<ValueType, ExceptionClass>, KeyValueGetterTrait<ValueType, ExceptionClass>, ValueGetterTrait<ValueType>
{
	public static final int LISTENER_VALUE_CHANGE = 0x02;
	public static final int LISTENER_VALUE_STORE = 0x03;
	public static final int LISTENER_VALUE_REMOVE = 0x04;
	protected volatile ValueType value;

	protected ContainerWithValue( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, @Nonnull String localName ) throws ExceptionClass
	{
		this( creator, null, localName, null );
	}

	protected ContainerWithValue( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, @Nullable BaseClass parent, @Nonnull String localName ) throws ExceptionClass
	{
		this( creator, parent, localName, null );
	}

	protected ContainerWithValue( @Nonnull BiFunctionWithException<BaseClass, String, BaseClass, ExceptionClass> creator, @Nullable BaseClass parent, @Nonnull String localName, @Nullable ValueType value ) throws ExceptionClass
	{
		super( creator, parent, localName );
		this.value = value;
	}

	public final int addValueChangeListener( ContainerListener.OnValueChange<BaseClass, ValueType> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_VALUE_CHANGE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1], ( ValueType ) objs[2] );
			}
		} );
	}

	public final int addValueRemoveListener( ContainerListener.OnValueRemove<BaseClass, ValueType> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_VALUE_REMOVE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1] );
			}
		} );
	}

	public final int addValueStoreListener( ContainerListener.OnValueStore<BaseClass, ValueType> function, ContainerListener.Flags... flags )
	{
		return listenerAdd( new ContainerListener.Container( LISTENER_VALUE_STORE, flags )
		{
			@Override
			public void call( Object[] objs )
			{
				function.listen( ( BaseClass ) objs[0], ( ValueType ) objs[1] );
			}
		} );
	}

	public <O> Voluntary<O> asObject( Class<O> cls )
	{
		try
		{
			Constructor<?> constructor = cls.getConstructor( ContainerBase.class );
			return Voluntary.of( ( O ) constructor.newInstance( this ) );
		}
		catch ( Exception ignore )
		{
		}

		try
		{
			Constructor<O> constructor = cls.getConstructor();
			O instance = constructor.newInstance();

			for ( Field field : cls.getFields() )
			{
				BaseClass child = childFind( field.getName() ).orElse( null );

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

			for ( Field field : cls.getFields() )
				if ( field.get( instance ) == null && !field.isSynthetic() )
					Kernel.L.warning( "The field \"" + field.getName() + "\" is unassigned for object \"" + cls + "\"." );

			return Voluntary.of( instance );
		}
		catch ( Exception ignore )
		{
		}

		return Voluntary.empty();
	}

	@Override
	protected void canCreateChild( BaseClass node, String key ) throws ExceptionClass
	{
		if ( node.getParent() == this && hasFlag( Flags.VALUES_ONLY ) )
			throw getException( "This node can only contain values.", null );
	}

	@Override
	public void destroy()
	{
		super.destroy();
		updateValue( null );
	}

	@Override
	public BaseClass duplicate()
	{
		BaseClass clone = super.duplicate();

		try
		{
			Method method = value.getClass().getMethod( "clone" );
			if ( Modifier.isPublic( method.getModifiers() ) ) // One sign that it was implemented.
				clone.value = ( ValueType ) method.invoke( value );
			else
				clone.value = value;
		}
		catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e )
		{
			clone.value = value;
		}

		return clone;
	}

	public Stream<ValueType> flatValues()
	{
		notDisposed();
		Stream<ValueType> stream = children.stream().flatMap( ContainerWithValue::flatValues );
		return Optional.ofNullable( value ).map( t -> Stream.concat( Stream.of( t ), stream ) ).orElse( stream );
	}

	public <LT extends ValueType> List<LT> getChildAsList( String key, Class<LT> type )
	{
		return childFind( key ).map( child -> child.getChildren().map( c -> Objs.castTo( c.value, type ) ).filter( Objects::nonNull ).collect( Collectors.toList() ) ).orElse( null );
	}

	public <ExpectedValueType extends ValueType> List<ExpectedValueType> getChildAsList( String key )
	{
		return childFind( key ).map( child -> child.getChildren().map( c -> ( ExpectedValueType ) c.value ).filter( Objects::nonNull ).collect( Collectors.toList() ) ).orElse( null );
	}

	public <T> Voluntary<T> getChildAsObject( @Nonnull String key, Class<T> cls )
	{
		return childFind( key ).flatMap( child -> child.asObject( cls ) );
	}

	public <ExpectedValueType extends ValueType> Map<String, ExpectedValueType> getChildrenAsMap()
	{
		return children.stream().collect( Collectors.toMap( ContainerBase::getLocalName, c -> ( ExpectedValueType ) c.value ) );
	}

	public <ExpectedValueType extends ValueType> Map<String, ExpectedValueType> getChildrenAsMap( String key )
	{
		return childFind( key ).map( child -> ( Map<String, ExpectedValueType> ) child.getChildrenAsMap() ).orElse( null );
	}

	public <ExpectedValueType extends ValueType> Stream<Pair<String, ExpectedValueType>> getChildrenWithKeys()
	{
		return children.stream().map( c -> new Pair( c.getLocalName(), c.value ) );
	}

	public Stream<BaseClass> getChildrenWithValue()
	{
		return getChildren().filter( ContainerWithValue::hasValue );
	}

	@Override
	public Voluntary<ValueType> getValue( Function<ValueType, ValueType> computeFunction )
	{
		ValueType value = getValue().orElse( null );
		ValueType newValue = computeFunction.apply( value );
		if ( value != newValue )
			try
			{
				setValue( newValue );
			}
			catch ( Exception e )
			{
				// Ignore
			}
		return Voluntary.ofNullable( newValue );
	}

	@Override
	public Voluntary<ValueType> getValue( Supplier<ValueType> supplier )
	{
		if ( !hasValue() )
			try
			{
				setValue( supplier.get() );
			}
			catch ( Exception e )
			{
				return Voluntary.withException( ( ExceptionClass ) e );
			}
		return getValue();
	}

	@Override
	public Voluntary<ValueType> getValue( @Nonnull Namespace key )
	{
		return childFind( key ).flatMap( ContainerWithValue::getValue );
	}

	@Override
	public Voluntary<ValueType> getValue()
	{
		notDisposed();
		return Voluntary.ofNullable( value );
	}

	@Override
	public final boolean hasValue( Namespace key )
	{
		return childFind( key ).map( ContainerWithValue::hasValue ).orElse( false );
	}

	@Override
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
	public void merge( @Nonnull BaseClass other )
	{
		super.merge( other );
		updateValue( other.value );
	}

	public Voluntary<ValueType> pollValue()
	{
		return VoluntaryWithCause.ofNullableWithCause( updateValue( null ) );
	}

	public Voluntary<ValueType> pollValue( String key )
	{
		return childFind( key ).flatMap( ContainerWithValue::pollValue );
	}

	@Override
	public void setValue( ValueType value )
	{
		notDisposed();
		notFlag( Flags.READ_ONLY );
		if ( hasFlag( Flags.NO_OVERRIDE ) && hasValue() )
			throw new ContainerException( getCurrentPath() + " has NO_OVERRIDE flag" );
		if ( value != null && ContainerBase.class.isAssignableFrom( value.getClass() ) )
			throw new ContainerException( "The value can't be of class ContainerBase, please use the appropriate methods instead." );
		updateValue( value );
	}

	@Override
	public void setValue( Namespace key, ValueType value )
	{
		getChildOrCreate( key ).setValue( value );
	}

	@Override
	public void setValue( TypeBase type, ValueType value ) throws ExceptionClass
	{
		getChildOrCreate( type ).setValue( value );
	}

	@Override
	public void setValueIfAbsent( Supplier<? extends ValueType> value ) throws ExceptionClass
	{
		if ( !hasValue() )
			setValue( value.get() );
	}

	@Override
	public void setValueIfAbsent( TypeBase.TypeWithDefault type ) throws ExceptionClass
	{
		try
		{
			getChildOrCreate( type ).setValueIfAbsent( type.getDefaultSupplier() );
		}
		catch ( Exception e )
		{
			if ( e instanceof RuntimeException )
				throw ( RuntimeException ) e;
			// XXX Why was I getting exception not declared compile errors?
			throw ( ExceptionClass ) e;
		}
	}

	@Override
	public void setValueIfAbsent( Namespace key, Supplier<? extends ValueType> value ) throws ExceptionClass
	{
		getChildOrCreate( key ).setValueIfAbsent( value );
	}

	public Parcel toParcel() throws ParcelableException.Error
	{
		Parcel result = new Parcel( getLocalName() );
		for ( BaseClass child : children )
			result.addChild( null, child.toParcel() );
		return result;
	}

	/**
	 * Used privately to update the value and call the proper listeners.
	 */
	protected <T extends ValueType> T updateValue( ValueType value )
	{
		if ( this.value == null && value != null )
			listenerFire( LISTENER_VALUE_STORE, value );
		if ( this.value != null && value == null )
			listenerFire( LISTENER_VALUE_REMOVE, this.value );
		listenerFire( LISTENER_VALUE_CHANGE, this.value, value );
		ValueType oldValue = this.value;
		this.value = value;
		setDirty( true );
		return ( T ) oldValue;
	}

	public Map<String, ValueType> values()
	{
		notDisposed();
		return children.stream().filter( ContainerWithValue::hasValue ).collect( Collectors.toMap( ContainerWithValue::getLocalName, c -> c.getValue().orElse( null ) ) );
	}

	public static class Flags extends ContainerBase.Flags
	{
		// Limits this container to containing only values
		public static final int VALUES_ONLY = getNextFlag();

		Flags()
		{
			// Static Access
		}
	}
}
