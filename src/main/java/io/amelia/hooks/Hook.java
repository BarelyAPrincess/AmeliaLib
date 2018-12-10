/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.hooks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.amelia.support.Priority;

import static java.lang.annotation.ElementType.METHOD;

@Retention( RetentionPolicy.RUNTIME )
@Target( value = {METHOD} )
public @interface Hook
{
	String ns() default "";

	Priority priority() default Priority.NORMAL;
}
