package com.owlike.genson.ext.javadatetime.annotation;

import com.owlike.genson.GensonBuilder;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.ext.javadatetime.TimestampFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
/**
 * Annotation which can be used to specify the timestamp format to used when serializing/deserializing the
 * annotated property/field/parameter
 *
 * <p>This annotation only applies if {@link GensonBuilder#isDateAsTimestamp()} is set to true or if the property
 * is annotated with {@link JsonDateFormat#asTimeInMillis()} set to true</p>
 */
public @interface JsonTimestampFormat {
	TimestampFormat value();
}
