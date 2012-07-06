package org.genson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genson.Genson;

public abstract class AbstractBeanDescriptorProvider implements BeanDescriptorProvider {	
	protected AbstractBeanDescriptorProvider() {
	}
	
	@Override
	public BeanDescriptor<?> create(Type type, Genson genson) {
		Class<?> rawType = TypeUtil.getRawClass(type);
		return provideBeanDescriptor(rawType, genson);
	}
	
	@Override
	public <T> BeanDescriptor<T> provideBeanDescriptor(Class<?> ofClass, Genson genson) {
		Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap = new LinkedHashMap<String, LinkedList<PropertyMutator<T, ?>>>();
		Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap = new LinkedHashMap<String, LinkedList<PropertyAccessor<T, ?>>>();

		List<BeanCreator<T>> creators = provideBeanCreators(ofClass, genson);
		
		provideBeanPropertyAccessors(ofClass, accessorsMap, genson);
		provideBeanPropertyMutators(ofClass, mutatorsMap, genson);

		checkAndUpdate(creators);

		List<PropertyAccessor<T, ?>> accessors = new ArrayList<PropertyAccessor<T, ?>>(accessorsMap.size());
		for (Map.Entry<String, LinkedList<PropertyAccessor<T, ?>>> entry : accessorsMap.entrySet()) {
			PropertyAccessor<T, ?> accessor = checkAndMergeAccessors(entry.getKey(), entry.getValue());
			// in case of...
			if (accessor != null)
				accessors.add(accessor);
		}

		Map<String, PropertyMutator<T, ?>> mutators = new HashMap<String, PropertyMutator<T, ?>>(
				mutatorsMap.size());
		for (Map.Entry<String, LinkedList<PropertyMutator<T, ?>>> entry : mutatorsMap.entrySet()) {
			PropertyMutator<T, ?> mutator = checkAndMergeMutators(entry.getKey(), entry.getValue());
			if (mutator != null)
				mutators.put(mutator.name, mutator);
		}

		// TODO
		mergeMutatorsWithCreatorProperties(mutators, creators);

		return create(ofClass, creators, accessors, mutators);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> BeanDescriptor<T> create(Class<?> ofClass, List<BeanCreator<T>> creators,
			List<PropertyAccessor<T, ?>> accessors, Map<String, PropertyMutator<T, ?>> mutators) {
		return new BeanDescriptor(ofClass, accessors, mutators, creators);
	}

	protected abstract <T> List<BeanCreator<T>> provideBeanCreators(Class<?> ofClass, Genson genson);

	protected abstract <T> void provideBeanPropertyMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap, Genson genson);

	protected abstract <T> void provideBeanPropertyAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap, Genson genson);

	/**
	 * Implementations of this method can do some additional checks on the creators validity or do any other operations related to creators.
	 * 
	 * @param creators
	 * @return
	 */
	protected abstract <T> void checkAndUpdate(List<BeanCreator<T>> creators);

	protected abstract <T> PropertyMutator<T, ?> checkAndMergeMutators(String name,
			LinkedList<PropertyMutator<T, ?>> mutators);

	protected abstract <T> void mergeMutatorsWithCreatorProperties(
			Map<String, PropertyMutator<T, ?>> mutators, List<BeanCreator<T>> creators);

	protected abstract <T> PropertyAccessor<T, ?> checkAndMergeAccessors(String name,
			LinkedList<PropertyAccessor<T, ?>> accessors);
}
