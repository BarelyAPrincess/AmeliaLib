/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.support.Objs;

public interface ScriptingProcessor
{
	@Nonnull
	default Stream<ScriptingOption> getOptions()
	{
		Optional<Class<?>> options = Arrays.stream( getClass().getDeclaredClasses() ).filter( cls -> "Options".equals( cls.getSimpleName() ) ).findAny();
		if ( !options.isPresent() )
			return Stream.empty();
		Class<?> cls = options.get();
		return Arrays.stream( cls.getDeclaredFields() ).filter( field -> ScriptingOption.class.isAssignableFrom( field.getDeclaringClass() ) ).map( field -> {
			try
			{
				return ( ScriptingOption ) field.get( null );
			}
			catch ( IllegalAccessException e )
			{
				return null;
			}
		} ).filter( Objs::isNotNull );
	}

	void postEvaluate( ScriptingContext scriptingContext );

	void preEvaluate( ScriptingContext scriptingContext );

	/**
	 * Default transformative actions for each contextual evaluation.
	 * Presently transforms processor options.
	 * Very little reason to implement this method unless you'd like to disable/override the default options transform.
	 *
	 * @param scriptingContext The ScriptingContext to transform
	 */
	default void transformScriptingContext( ScriptingContext scriptingContext )
	{
		scriptingContext.transformScriptingContext( this );
	}
}
