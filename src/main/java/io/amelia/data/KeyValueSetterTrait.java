package io.amelia.data;

public interface KeyValueSetterTrait<ValueType, ExceptionClass extends Exception>
{
	void setValue( String key, ValueType value ) throws ExceptionClass;

	void setValue( TypeBase type, ValueType value ) throws ExceptionClass;

	void setValueIfAbsent( TypeBase.TypeWithDefault type ) throws ExceptionClass;

	void setValueIfAbsent( String key, ValueType value ) throws ExceptionClass;
}
