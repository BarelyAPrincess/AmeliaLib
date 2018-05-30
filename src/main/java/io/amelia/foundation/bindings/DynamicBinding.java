/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.bindings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to label a method as having a dynamic value inside BindingResolvers.
 * Meaning the returned value will not be saved in the bindings and will be resolved each time.
 */
@Target( {ElementType.METHOD, ElementType.FIELD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface DynamicBinding
{

}
