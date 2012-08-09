package com.owlike.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Static methods annotated with @Creator annotation will act as method factories. These methods can
 * take arguments that match properties from the json stream. If you use default configuration you
 * must annotate each argument with {@link com.owlike.genson.annotation.JsonProperty JsonProperty} and
 * define a name. However Genson is also able to use the names from the method signature, but by
 * default it is disabled. To enable this feature use
 * 
 * <pre>
 * new Genson.Builder().setWithDebugInfoPropertyNameResolver(true).create();
 * </pre>
 * 
 * It will register {@link com.owlike.genson.reflect.ASMCreatorParameterNameResolver
 * ASMCreatorParameterNameResolver} name resolver, that will use the debug symbols generated during
 * compilation to resolve the names.
 * 
 * By default if a object contains constructors and methods annotated with @Creator the factory
 * methods will be privileged.
 * 
 * @see com.owlike.genson.annotation.JsonProperty JsonProperty
 * @see com.owlike.genson.reflect.BeanMutatorAccessorResolver.StandardMutaAccessorResolver
 *      StandardMutaAccessorResolver
 * 
 * @author eugen
 * 
 */
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Creator {

}
