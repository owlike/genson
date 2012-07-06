package org.genson.reflect;

import java.lang.reflect.Type;

import org.genson.Factory;
import org.genson.Genson;


/*
 * TODO add the Context as parameter?
 */
public interface BeanDescriptorProvider extends Factory<BeanDescriptor<?>> {
	@Override
	public BeanDescriptor<?> create(Type type, Genson genson);

	// we should have Class<T> ofClass, but if we do that we could not apply the BeanView mecanism as it is...
	public <T> BeanDescriptor<T> provideBeanDescriptor(Class<?> ofClass, Genson genson);
}
