package org.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.genson.BeanView;

/**
 * Annotation used actually only in spring web integration
 * {@link org.genson.spring.GensonMessageConverter GensonMessageConverter} to indicate
 * at runtime what BeanView must be used. Its intended to be used in conjuction
 * with springs @ResponseBody/@RequestBody and @RequestMapping annotations.
 * 
 * @author eugen
 * 
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface WithBeanView {
	Class<? extends BeanView<?>>[] views() default {};
}
