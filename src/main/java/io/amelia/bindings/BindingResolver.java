/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.bindings;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.amelia.support.Objs;
import io.amelia.support.StringLengthComparator;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;

/**
 * <pre>
 * public Object exampleMethod( @BindingClass( Logger.class ) Object obj )
 * {
 *
 * }
 * </pre>
 */
public abstract class BindingResolver
{
	private final Map<Class<?>, Class<?>> classToClassMappings = new HashMap<>();
	private final Map<Class<?>, String> classToNamespaceMappings = new HashMap<>();
	private final Map<String, Object> instances = new HashMap<>();
	private final Map<String, String> namespaceToNamespaceMappings = new HashMap<>();
	private final Map<String, Supplier<Object>> suppliers = new HashMap<>();

	@Nonnull
	String baseNamespace = "";
	private String defaultKey = null;

	protected final void addAlias( String sourceNamespace, String targetNamespace )
	{
		sourceNamespace = Bindings.normalizeNamespace( sourceNamespace );
		if ( sourceNamespace.startsWith( baseNamespace ) )
			sourceNamespace = Strs.trimAll( sourceNamespace.substring( baseNamespace.length() ), '.' );

		targetNamespace = Bindings.normalizeNamespace( targetNamespace );
		if ( targetNamespace.startsWith( baseNamespace ) )
			targetNamespace = Strs.trimAll( targetNamespace.substring( baseNamespace.length() ), '.' );

		namespaceToNamespaceMappings.put( sourceNamespace, targetNamespace );
	}

	protected final void addAlias( Class<?> sourceClass, String targetNamespace )
	{
		targetNamespace = Bindings.normalizeNamespace( targetNamespace );
		if ( targetNamespace.startsWith( baseNamespace ) )
			targetNamespace = Strs.trimAll( targetNamespace.substring( baseNamespace.length() ), '.' );

		classToNamespaceMappings.put( sourceClass, targetNamespace );
	}

	protected final void addAlias( Class<?> sourceClass, Class<?> targetClass )
	{
		classToClassMappings.put( sourceClass, targetClass );
	}

	protected <T> Voluntary<T> get( @Nonnull String namespace, @Nonnull final String key, @Nonnull Class<T> expectedClass, @Nonnull Object... args )
	{
		Voluntary<T> result = Voluntary.empty();

		// TODO WARNING! It's possible that a subclass could crash the application by making looping aliases. We should prevent this!
		if ( namespaceToNamespaceMappings.containsKey( key ) )
			result = get( namespace, namespaceToNamespaceMappings.get( key ), expectedClass, args );

		// Check for already instigated instances.
		if ( !result.isPresent() && instances.containsKey( key ) )
			try
			{
				result = Voluntary.of( ( T ) instances.get( key ) );
			}
			catch ( ClassCastException e )
			{
				// Ignore
			}

		if ( !result.isPresent() && suppliers.containsKey( key ) )
			try
			{
				result = Voluntary.of( ( T ) suppliers.get( key ) );
			}
			catch ( ClassCastException e )
			{
				// Ignore
			}

		if ( !result.isPresent() )
			result = Bindings.invokeMethods( this, method -> {
				if ( expectedClass.isAssignableFrom( method.getReturnType() ) )
				{
					if ( method.isAnnotationPresent( ProvidesBinding.class ) )
					{
						String provides = method.getAnnotation( ProvidesBinding.class ).value();
						String fullNamespace = Bindings.normalizeNamespace( provides );
						if ( fullNamespace.equals( namespace ) || Strs.trimStart( fullNamespace, baseNamespace ).equals( namespace ) || provides.equals( key ) )
							return true;
					}
					return method.getName().equals( key );
				}
				return false;
			}, namespace, args );

		if ( !result.isPresent() )
			result = Bindings.invokeFields( this, field -> {
				if ( expectedClass.isAssignableFrom( field.getType() ) )
				{
					if ( field.isAnnotationPresent( ProvidesBinding.class ) )
					{
						String provides = field.getAnnotation( ProvidesBinding.class ).value();
						String fullNamespace = Bindings.normalizeNamespace( provides );
						if ( fullNamespace.equals( namespace ) || Strs.trimStart( fullNamespace, baseNamespace ).equals( namespace ) || provides.equals( key ) )
							return true;
					}
					return field.getName().equals( key );
				}
				return false;
			}, namespace );

		return result;
	}

	protected <T> T get( @Nonnull String namespace, @Nonnull Class<T> expectedClass, @Nonnull Object... args )
	{
		// If the requested key is empty, we use the default
		if ( Objs.isEmpty( namespace ) )
			if ( Objs.isEmpty( defaultKey ) )
				return null;
			else
				namespace = defaultKey;

		String subNamespace;
		if ( namespace.startsWith( baseNamespace ) )
			subNamespace = Strs.trimAll( namespace.substring( baseNamespace.length() ), '.' );
		else
		{
			namespace = baseNamespace + "." + namespace;
			subNamespace = namespace;
		}

		// Convert namespaces to friendly keys
		Object obj = get( namespace, Strs.toCamelCase( subNamespace ), expectedClass, args );

		if ( obj == null )
			obj = get( namespace, Strs.toCamelCase( namespace ), expectedClass, args );

		return ( T ) obj;
	}

	/**
	 * Called when a class needs resolving.
	 * Each registered resolver will be called for this purpose, first to return non-null will succeed.
	 *
	 * @param expectedClass
	 * @param <T>
	 *
	 * @return
	 */
	protected <T> T get( @Nonnull Class<T> expectedClass, @Nonnull Object... args )
	{
		Object obj = null;

		if ( classToClassMappings.containsKey( expectedClass ) )
			obj = get( classToClassMappings.get( expectedClass ), args );

		if ( obj == null && classToNamespaceMappings.containsKey( expectedClass ) )
			obj = get( classToNamespaceMappings.get( expectedClass ), expectedClass, args );

		if ( obj == null )
			obj = Bindings.invokeMethods( this, method -> expectedClass.isAssignableFrom( method.getReturnType() ), args );

		if ( obj == null )
			obj = Bindings.invokeFields( this, field -> expectedClass.isAssignableFrom( field.getType() ) );

		return ( T ) obj;
	}

	public boolean isRegistered()
	{
		return Bindings.resolvers.contains( this );
	}

	protected void setDefault( String defaultKey )
	{
		this.defaultKey = defaultKey;
	}

	public static class Comparator implements java.util.Comparator<BindingResolver>
	{
		private StringLengthComparator comparator;

		public Comparator()
		{
			this( true );
		}

		public Comparator( boolean ascendingOrder )
		{
			comparator = new StringLengthComparator( ascendingOrder );
		}

		@Override
		public int compare( BindingResolver left, BindingResolver right )
		{
			return comparator.compare( left.baseNamespace, right.baseNamespace );
		}
	}
}
