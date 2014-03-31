package com.owlike.genson.reflect;

import java.lang.reflect.Type;

import com.owlike.genson.Genson;

/**
 * Interface implemented by classes who want to provide {@link BeanDescriptor} instances for the
 * specified type.
 * 
 * @author eugen
 */
public interface BeanDescriptorProvider {
	/**
	 * Provides a BeanDescriptor for type "ofType" using current Genson instance.
	 * @param ofType is the type for which we need a BeanDescriptor.
	 * @param genson is the current Genson instance.
	 * @return A BeanDescriptor instance able to serialize/deserialize objects of type ofType.
	 */
	public BeanDescriptor<?> provide(Type ofType, Genson genson);
	
	/**
	 * @see BeanDescriptorProvider#provide(Type, Genson)
	 */
	public <T> BeanDescriptor<T> provide(Class<T> ofClass, Genson genson);

	/**
	 * Provides a BeanDescriptor that can serialize/deserialize "ofClass" type, based on "type"
	 * argument. The arguments "ofClass" and "type" will be the same in most cases, but for example
	 * in BeanViews ofClass will correspond to the parameterized type and "type" to the BeanView
	 * implementation.
	 * 
	 * @param ofClass
	 *            is the Class for which we need a BeanDescriptor that will be able to
	 *            serialize/deserialize objects of that type;
	 * @param type
	 *            to use to build this descriptor (use its declared methods, fields, etc).
	 * @param genson
	 *            is the current Genson instance.
	 * @return A BeanDescriptor instance able to serialize/deserialize objects of type ofClass.
	 */
	public <T> BeanDescriptor<T> provide(Class<T> ofClass, Type type, Genson genson);
}
