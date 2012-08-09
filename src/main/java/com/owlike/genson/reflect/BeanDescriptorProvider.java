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
	public BeanDescriptor<?> provide(Type type, Genson genson);

	/**
	 * Creates a BeanDescriptor based on ofClass argument.
	 * @param ofClass is the class used to create the BeanDescriptor.
	 * @param genson
	 * @return an instance of a BeanDescriptor based on ofClass argument.
	 */
	// we should have Class<T> ofClass, but if we do that we could not apply the BeanView mecanism as it is...
	public <T> BeanDescriptor<T> provideBeanDescriptor(Class<?> ofClass, Genson genson);
}
