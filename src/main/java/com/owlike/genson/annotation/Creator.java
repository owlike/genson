package com.owlike.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated use JsonCreator instead
 */
@JsonCreator
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Creator {

}
