package io.amelia.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotates an object that has yet not been implemented.
 * Such as a subclass of a major system or a system entirely.
 */
@Retention( RetentionPolicy.RUNTIME )
public @interface NotImplemented
{
	String value() default "";
}
