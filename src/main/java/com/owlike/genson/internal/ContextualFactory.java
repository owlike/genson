package com.owlike.genson.internal;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.reflect.BeanProperty;

/** 
 * @author eugen
 *
 * @param <T> the type of objects handled by Converters built by this factory 
 */
public interface ContextualFactory<T> {
	public Converter<T> create(BeanProperty property, Genson genson);
}
