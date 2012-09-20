package com.owlike.genson.reflect;

import java.lang.reflect.Type;

import com.owlike.genson.Genson;

/**
 * Interface implemented by classes who want to provide {@link BeanDescriptor} instances
 * for the specified type.
 * 
 * @author eugen
 */
public interface BeanDescriptorProvider {
	/**
	 * Creates a BeanDescriptor based on type argument.
	 * @param type used to create the BeanDescriptor.
	 * @param genson
	 * @return an instance of a BeanDescriptor based on type argument.
	 */
	public <T> BeanDescriptor<T> provide(Class<T> ofClass, Type type, Genson genson);
}
