package org.genson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genson.Factory;
import org.genson.TransformationRuntimeException;


/*
 * TODO add the Context as parameter?
 */
public interface BeanDescriptorProvider extends Factory<BeanDescriptor> {
	public BeanDescriptor create(Type ofType);

	public BeanDescriptor provideBeanDescriptor(Class<?> ofClass);

	public static class CompositeBeanDescriptorProvider implements BeanDescriptorProvider {
		private final Map<Type, BeanDescriptor> _beanDescriptorsCache = new HashMap<Type, BeanDescriptor>();
		private final List<BeanDescriptorProvider> _components = new ArrayList<BeanDescriptorProvider>();

		public CompositeBeanDescriptorProvider(List<? extends BeanDescriptorProvider> components) {
			_components.addAll(components);
		}

		public BeanDescriptorProvider add(BeanDescriptorProvider component) {
			_components.add(component);
			return this;
		}

		@Override
		public BeanDescriptor create(Type ofType) {
			BeanDescriptor beanDesc = _beanDescriptorsCache.get(ofType);
			return beanDesc != null ? beanDesc : provideBeanDescriptor(TypeUtil.getRawClass(ofType));
		}

		@Override
		public BeanDescriptor provideBeanDescriptor(Class<?> ofClass) {
			BeanDescriptor beanDesc = _beanDescriptorsCache.get(ofClass);
			if (beanDesc == null) {
				for (BeanDescriptorProvider provider : _components) {
					beanDesc = provider.provideBeanDescriptor(ofClass);
					if (beanDesc != null) break;
				}
				
				_beanDescriptorsCache.put(ofClass, beanDesc);
			}

			if (beanDesc == null)
				throw new TransformationRuntimeException(
						"Could not resolve BeanDescriptor for class " + ofClass.getName());

			return beanDesc;
		}

	}
}
