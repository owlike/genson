package org.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Static methods annotated with @Creator annotation will act as method factories.
 * These methods can take arguments matching properties from the json stream.
 * You can annotate them with {@link org.genson.annotation.JsonProperty JsonProperty} if you want to match a property
 * with another name than the one of your argument.
 * 
 * By default if a object contains constructors and methods annotated with @Creator the
 * factory methods will be privileged (however that does not mean that the constructor will not be used
 * if the instance can't be created using the factory method).
 * 
 * {@see org.genson.annotation.JsonProperty JsonProperty}
 * {@see org.genson.reflect.BeanMutatorAccessorResolver.StandardMutaAccessorResolver StandardMutaAccessorResolver}
 * 
 * @author eugen
 *
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Creator {

}
