package org.genson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBeanDescriptorProvider implements BeanDescriptorProvider {
	
	@Override
	public BeanDescriptor create(Type ofType) {
		Class<?> rawType = TypeUtil.getRawClass(ofType);
		return provideBeanDescriptor(rawType);
	}
	
	@Override
	public BeanDescriptor provideBeanDescriptor(Class<?> ofClass) {
		BeanDescriptor desc = null;
		Map<String, LinkedList<PropertyMutator>> mutatorsMap = new LinkedHashMap<String, LinkedList<PropertyMutator>>();
		Map<String, LinkedList<PropertyAccessor>> accessorsMap = new LinkedHashMap<String, LinkedList<PropertyAccessor>>();

		List<BeanCreator> creators = provideBeanCreators(ofClass);
		
		provideBeanPropertyAccessors(ofClass, accessorsMap);
		provideBeanPropertyMutators(ofClass, mutatorsMap);

		checkAndUpdate(creators);

		List<PropertyAccessor> accessors = new ArrayList<PropertyAccessor>(accessorsMap.size());
		for (Map.Entry<String, LinkedList<PropertyAccessor>> entry : accessorsMap.entrySet()) {
			PropertyAccessor accessor = checkAndMergeAccessors(entry.getKey(), entry.getValue());
			// in case of...
			if (accessor != null)
				accessors.add(accessor);
		}

		Map<String, PropertyMutator> mutators = new HashMap<String, PropertyMutator>(
				mutatorsMap.size());
		for (Map.Entry<String, LinkedList<PropertyMutator>> entry : mutatorsMap.entrySet()) {
			PropertyMutator mutator = checkAndMergeMutators(entry.getKey(), entry.getValue());
			if (mutator != null)
				mutators.put(mutator.name, mutator);
		}

		// TODO
		mergeMutatorsWithCreatorProperties(mutators, creators);

		desc = new BeanDescriptor(ofClass, accessors, mutators, creators);
		return desc;
	}

	protected BeanDescriptor create(Class<?> ofClass, List<BeanCreator> creators,
			List<PropertyAccessor> accessors, Map<String, PropertyMutator> mutators) {
		return new BeanDescriptor(ofClass, accessors, mutators, creators);
	}

	protected abstract List<BeanCreator> provideBeanCreators(Class<?> ofClass);

	protected abstract void provideBeanPropertyMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator>> mutatorsMap);

	protected abstract void provideBeanPropertyAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor>> accessorsMap);

	/**
	 * Implementations of this method can do some additional checks on the creators validity or do any other operations related to creators.
	 * 
	 * @param creators
	 * @return
	 */
	protected abstract void checkAndUpdate(List<BeanCreator> creators);

	protected abstract PropertyMutator checkAndMergeMutators(String name,
			LinkedList<PropertyMutator> mutators);

	protected abstract void mergeMutatorsWithCreatorProperties(
			Map<String, PropertyMutator> mutators, List<BeanCreator> creators);

	protected abstract PropertyAccessor checkAndMergeAccessors(String name,
			LinkedList<PropertyAccessor> accessors);
}
