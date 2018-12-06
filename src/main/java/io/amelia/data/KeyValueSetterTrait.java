package io.amelia.data;

public interface KeyValueSetterTrait<ValueType, ExceptionClass extends Exception>
{
	void setValue( String key, ValueType value ) throws ExceptionClass;

	default void setValue( TypeBase type, ValueType value ) throws ExceptionClass
	{
		setValue( type.getPath(), value );
	}

	default void setValueIfAbsent( TypeBase.TypeWithDefault<? extends ValueType> type ) throws ExceptionClass
	{
		setValueIfAbsent( type.getPath(), type.getDefault() );
	}

	void setValueIfAbsent( String key, ValueType value ) throws ExceptionClass;

	default void setValues( KeyValueGetterTrait<ValueType, ?> values ) throws ExceptionClass
	{
		for ( String key : values.getKeys() )
			values.getValue( key ).ifPresent( value -> setValue( key, value ) );
	}
}
