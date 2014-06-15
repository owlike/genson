package com.owlike.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to {@link HandleClassMetadata}, put this annotation on your Converters, Serializers and
 * Deserializers to disable Genson default null handling (
 * {@link com.owlike.genson.convert.NullConverter NullConverter}). In that case you will have to
 * write the code that handles nulls during serialization and deserialization of your type (and not
 * of its content). This feature is mainly for internal use.
 *
 * @author eugen
 * @see HandleClassMetadata
 * @see com.owlike.genson.convert.NullConverter NullConverter
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HandleNull {

}
