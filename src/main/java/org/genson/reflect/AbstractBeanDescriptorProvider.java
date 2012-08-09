package org.genson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genson.Genson;

/**
 * Abstract implementation of {@link BeanDescriptorProvider} applying the template pattern.
 * Subclasses are not expected to override {@link #provide(Type, Genson)} and
 * {@link #provideBeanDescriptor(Class, Genson)} methods. However they may do so if they really need
 * to. Thats why theee methods are not final.
 * 
 * If you wonder how to implement the different abstract methods defined in this class have a look
 * at the <a href=
 * "http://code.google.com/p/genson/source/browse/src/main/java/org/genson/reflect/BaseBeanDescriptorProvider.java"
 * >BaseBeanDescriptorProvider</a>.
 * 
 * @author eugen
 * 
 */
public abstract class AbstractBeanDescriptorProvider implements BeanDescriptorProvider {
	protected AbstractBeanDescriptorProvider() {
	}

	public BeanDescriptor<?> provide(Type type, Genson genson) {
		Class<?> rawType = TypeUtil.getRawClass(type);
		return provideBeanDescriptor(rawType, genson);
	}

	public <T> BeanDescriptor<T> provideBeanDescriptor(Class<?> ofClass, Genson genson) {
		Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap = new LinkedHashMap<String, LinkedList<PropertyMutator<T, ?>>>();
		Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap = new LinkedHashMap<String, LinkedList<PropertyAccessor<T, ?>>>();

		List<BeanCreator<T>> creators = provideBeanCreators(ofClass, genson);

		provideBeanPropertyAccessors(ofClass, accessorsMap, genson);
		provideBeanPropertyMutators(ofClass, mutatorsMap, genson);

		List<PropertyAccessor<T, ?>> accessors = new ArrayList<PropertyAccessor<T, ?>>(
				accessorsMap.size());
		for (Map.Entry<String, LinkedList<PropertyAccessor<T, ?>>> entry : accessorsMap.entrySet()) {
			PropertyAccessor<T, ?> accessor = checkAndMergeAccessors(entry.getKey(),
					entry.getValue());
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

		mergeMutatorsWithCreatorProperties(ofClass, mutators, creators);
		BeanCreator<T> ctr = checkAndMerge(ofClass, creators);

		return create(ofClass, ctr, accessors, mutators);
	}

	/**
	 * Creates an instance of BeanDescriptor based on the passed arguments. Subclasses can override
	 * this method to create their own BeanDescriptors.
	 * 
	 * @param ofClass
	 * @param creators
	 * @param accessors
	 * @param mutators
	 * @return an instance of a BeanDescriptor.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> BeanDescriptor<T> create(Class<?> ofClass, BeanCreator<T> creator,
			List<PropertyAccessor<T, ?>> accessors, Map<String, PropertyMutator<T, ?>> mutators) {
		return new BeanDescriptor(ofClass, accessors, mutators, creator);
	}

	/**
	 * Provides a list of {@link BeanCreator} for type ofClass.
	 * 
	 * @param ofClass
	 * @param genson
	 * @return a list of BeanCreators, may be empty.
	 */
	protected abstract <T> List<BeanCreator<T>> provideBeanCreators(Class<?> ofClass, Genson genson);

	/**
	 * Adds resolved {@link PropertyMutator} to mutatorsMap.
	 * 
	 * @param ofClass
	 * @param mutatorsMap
	 * @param genson
	 */
	protected abstract <T> void provideBeanPropertyMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap, Genson genson);

	/**
	 * Adds resolved {@link PropertyAccessor} to accessorsMap.
	 * 
	 * @param ofClass
	 * @param accessorsMap
	 * @param genson
	 */
	protected abstract <T> void provideBeanPropertyAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap, Genson genson);

	/**
	 * Implementations of this method can do some additional checks on the creators validity or do
	 * any other operations related to creators. This method must merge all creators into a single
	 * one.
	 * 
	 * @param creators
	 * @return the creator that will be used by the BeanDescriptor
	 */
	protected abstract <T> BeanCreator<T> checkAndMerge(Class<?> ofClass,
			List<BeanCreator<T>> creators);

	/**
	 * Implementations are supposed to merge the {@link PropertyMutator}s from mutators list into a
	 * single PropertyMutator.
	 * 
	 * @param name
	 * @param mutators
	 * @return a single PropertyMutator or null.
	 */
	protected abstract <T> PropertyMutator<T, ?> checkAndMergeMutators(String name,
			LinkedList<PropertyMutator<T, ?>> mutators);

	/**
	 * Implementations may do additional merge operations based on resolved creators and their
	 * properties and the resolved mutators.
	 * 
	 * @param mutators
	 * @param creators
	 */
	protected abstract <T> void mergeMutatorsWithCreatorProperties(Class<?> tClass,
			Map<String, PropertyMutator<T, ?>> mutators, List<BeanCreator<T>> creators);

	/**
	 * Implementations are supposed to merge the {@link PropertyAccessor}s from accessors list into
	 * a single PropertyAccessor.
	 * 
	 * @param name
	 * @param accessors
	 * @return
	 */
	protected abstract <T> PropertyAccessor<T, ?> checkAndMergeAccessors(String name,
			LinkedList<PropertyAccessor<T, ?>> accessors);
}
