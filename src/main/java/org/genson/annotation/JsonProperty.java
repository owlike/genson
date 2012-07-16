package org.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JsonProperty annotation can be used to define the name of a property. You can apply it on fields
 * and methods. In that case this name will be used instead of the conventional one computed from
 * the signature. You can also use this annotation on parameters of creator methods and on
 * constructor parameters. In that case Genson during deserialization will try to use those names to
 * match the properties from the json stream. By default it is used in
 * {@link org.genson.reflect.PropertyNameResolver.AnnotationPropertyNameResolver
 * AnnotationPropertyNameResolver}.
 * 
 * @see org.genson.reflect.PropertyNameResolver.AnnotationPropertyNameResolver
 *      AnnotationPropertyNameResolver
 * @see org.genson.annotation.Creator Creator
 * @see org.genson.annotation.JsonIgnore JsonIgnore
 * 
 * @author eugen
 * 
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JsonProperty {
	/**
	 * The name of that property.
	 */
	String value() default "";

	/**
	 * Whether this property must be serialized. Default is true, the property will be serialized.
	 */
	boolean serialize() default true;

	/**
	 * Whether this property must be deserialized. Default is true, the property will be
	 * deserialized.
	 */
	boolean deserialize() default true;
}
