package com.owlike.genson.internal;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.reflect.BeanProperty;

/**
 * Beta feature, will be moved to another package in the future, create signature might change.
 * Allows to create a converter for some type T based on bean property available at compile time
 * (ex: you can not use it with map keys because they exist only at runtime). This feature does not
 * work for arrays/collections/maps, in the future it might be improved implying some refactoring.
 * 
 * @author eugen
 * 
 * @param <T>
 *            the type of objects handled by Converters built by this factory
 */
public interface ContextualFactory<T> {
	/**
	 * Return an instance of a converter working with objects of type T based on property argument
	 * or null.
	 */
	public Converter<T> create(BeanProperty property, Genson genson);
}
