package com.owlike.genson.ext.javadatetime.annotation;

import com.owlike.genson.ext.javadatetime.TimestampFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JsonTimestampFormat {
	TimestampFormat value();
}
