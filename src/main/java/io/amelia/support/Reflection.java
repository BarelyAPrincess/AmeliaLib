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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * WORK IN PROGRESS!!!
 *
 * Do not use, API will likely change.
 */
public class Reflection
{
	private static final ThreadLocal<List<StackTraceElement>> methodCallEnforcements = new ThreadLocal<>();

	public static Method getMethod( Class<?> aClass, String methodName )
	{
		try
		{
			return aClass.getDeclaredMethod( methodName );
		}
		catch ( NoSuchMethodException e )
		{
			return null;
		}
	}

	public static Method getMethod( String aClass, String methodName )
	{
		try
		{
			return Class.forName( aClass ).getDeclaredMethod( methodName );
		}
		catch ( ClassNotFoundException | NoSuchMethodException e )
		{
			return null;
		}
	}

	public static boolean hasAnnotation( Class<?> classToCheck, Class<? extends Annotation> annotation )
	{
		return classToCheck.getAnnotation( annotation ) != null;
	}

	/**
	 * Used to signal method calls and record a max count of 10.
	 *
	 * @param enclosingMethod
	 */
	public static void methodCall( Method enclosingMethod )
	{
		Objs.notNull( enclosingMethod );

		List<StackTraceElement> callHistory = methodCallEnforcements.get();

		if ( callHistory == null )
		{
			callHistory = new ArrayList<>();
			methodCallEnforcements.set( callHistory );
		}

		if ( callHistory.size() > 10 )
		{
			callHistory = Lists.subList( callHistory, 0, 9 );
			methodCallEnforcements.set( callHistory );
		}

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if ( stackTrace == null || stackTrace.length < 3 )
			throw new IllegalStateException( "Could not retrieve the Thread StackTrace." );

		callHistory.add( stackTrace[2] );
	}

	private static void printModifiers( int modifiers, StringBuilder result )
	{
		if ( Modifier.isPublic( modifiers ) )
			result.append( "public " );
		else if ( Modifier.isPrivate( modifiers ) )
			result.append( "private " );
		else if ( Modifier.isProtected( modifiers ) )
			result.append( "protected " );
		if ( Modifier.isStatic( modifiers ) )
			result.append( "static " );
		if ( Modifier.isFinal( modifiers ) )
			result.append( "final " );
	}

	// TODO Can be expanded
	public static String readoutField( Field field )
	{
		StringBuilder result = new StringBuilder();
		for ( Annotation annotation : field.getAnnotations() )
			result.append( "@" ).append( annotation.annotationType().getSimpleName() ).append( " " );
		printModifiers( field.getModifiers(), result );
		result.append( field.getType().getSimpleName() ).append( field.getName() ).append( " " ).append( " = (value?)" );
		return result.toString();
	}

	// TODO I think this method could have even more added to it.
	public static String readoutMethod( Method method )
	{
		StringBuilder result = new StringBuilder();
		for ( Annotation annotation : method.getAnnotations() )
			result.append( "@" ).append( annotation.annotationType().getSimpleName() ).append( " " );
		printModifiers( method.getModifiers(), result );
		result.append( method.getReturnType().getSimpleName() ).append( " " ).append( method.getName() ).append( "(" );
		for ( Parameter parameter : method.getParameters() )
		{
			for ( Annotation annotation : parameter.getAnnotations() )
				result.append( " @" ).append( annotation.annotationType().getSimpleName() );
			result.append( " " ).append( parameter.getType().getSimpleName() ).append( " " ).append( parameter.getName() );
		}
		if ( method.getParameterCount() > 0 )
			result.append( " " );
		return result.append( ")" ).toString();
	}

	public static void wasSuperCalled( Method method )
	{
		wasSuperCalled( method, 1 );
	}

	public static void wasSuperCalled( Method method, int maxDepth )
	{
		Objs.notNegative( maxDepth );
		Objs.notZero( maxDepth );

		List<StackTraceElement> callHistory = methodCallEnforcements.get();

		if ( callHistory == null )
			return;

		// callHistory.stream().limit( maxDepth ).forEach( s -> s. );
	}

	public static void wasSuperCalledAny( Method method )
	{
		wasSuperCalled( method, 999 );
	}

	private Reflection()
	{

	}
}
