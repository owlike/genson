package com.owlike.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.owlike.genson.Converter;

/**
 * This annotation is useful when you want to use a specific Converter for a property in a class,
 * but do not want to use it for all properties of that type. When you put this annotation on a
 * field, constructor parameter or setter/getter, Genson will use this Converter instead of any other.
 * <b>The Converter must have a default no arg constructor.</b>
 * 
 * @author eugen
 * 
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JsonConverter {
	Class<? extends Converter<?>> value();
}
