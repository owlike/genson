package com.owlike.genson.convert;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.reflect.BeanProperty;

/**
 * <b>Beta feature</b>
 * <br/>
 * Create method signature and BeanProperty might change in the future.
 * Allows to create a converter for some type T based on bean property available at compile time
 * (ex: you can not use it with map keys because they exist only at runtime). This feature works
 * only for POJO databinding, in could be improved implying some refactoring.
 *
 * @param <T> the type of objects handled by Converters built by this factory
 * @author eugen
 */
public interface ContextualFactory<T> {
  /**
   * Return an instance of a converter working with objects of type T based on property argument
   * or null.
   */
  public Converter<T> create(BeanProperty property, Genson genson);
}
