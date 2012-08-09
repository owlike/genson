package com.owlike.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated Serializer/Deserializer/Converter will be excluded from the BeanView mechanism.
 * Most default converters are annotated with WithoutBeanView (IntegerConverter, BooleanConverter etc).
 * 
 * @see com.owlike.genson.convert.BeanViewConverter BeanViewConverter
 * 
 * @author eugen
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface WithoutBeanView {

}
