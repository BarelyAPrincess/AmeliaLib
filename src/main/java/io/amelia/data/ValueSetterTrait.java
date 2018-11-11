package io.amelia.data;

public interface ValueSetterTrait<ValueType, ExceptionClass extends Exception>
{
	void setValue( ValueType value ) throws ExceptionClass;

	void setValueIfAbsent( ValueType value ) throws ExceptionClass;
}
