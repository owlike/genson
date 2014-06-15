package com.owlike.genson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;

/**
 * Can be used on java.util.Date and java.util.Calendar to indicate the pattern or lang to
 * use when working with this date field. The pattern format are the standard ones from
 * {@link SimpleDateFormat}.
 *
 * @author eugen
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface JsonDateFormat {
  /**
   * The pattern to use.
   */
  String value() default "";

  boolean asTimeInMillis() default false;

  String lang() default "";
}
