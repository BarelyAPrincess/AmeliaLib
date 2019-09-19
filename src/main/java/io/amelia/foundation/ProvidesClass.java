/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Miss Amelia Sara (Millie) <me@missameliasara.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.amelia.support.Priority;

/**
 * Annotates methods and fields from within binding provider class to indicate the full provided namespace.
 * This is recommended when possible to save the {@link Bindings} having to calculate the namespace through abstraction.
 */
@Target( {ElementType.FIELD, ElementType.METHOD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface ProvidesClass
{
	Priority priority() default Priority.NORMAL;

	Class<?> value();
}
