package com.owlike.genson.ext.javadatetime.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
/**
 * Annotation used to override the {@link java.time.ZoneId} to use when deserializing the annotated
 * field/property/parameter
 */
public @interface JsonZoneId {
	String value() default "";
}
