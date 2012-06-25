package org.genson.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genson.reflect.BeanCreator.BeanCreatorProperty;


public class BaseBeanDescriptorProvider extends AbstractBeanDescriptorProvider {
	protected final BeanMutatorAccessorResolver mutatorAccessorResolver;
	protected final PropertyNameResolver nameResolver;
	protected final boolean useGettersAndSetters;
	protected final boolean useFields;
	
	public BaseBeanDescriptorProvider(BeanMutatorAccessorResolver mutatorAccessorResolver,
			PropertyNameResolver nameResolver, boolean useGettersAndSetters, boolean useFields) {
		super();
		if (mutatorAccessorResolver == null)
			throw new IllegalArgumentException("mutatorAccessorResolver must be not null!");
		if (nameResolver == null)
			throw new IllegalArgumentException("nameResolver must be not null!");

		this.mutatorAccessorResolver = mutatorAccessorResolver;
		this.nameResolver = nameResolver;
		this.useFields = useFields;
		this.useGettersAndSetters = useGettersAndSetters;
		if (!useFields && !useGettersAndSetters) throw new IllegalArgumentException("You must allow at least one mode: with fields or methods.");
	}

	@Override
	public List<BeanCreator> provideBeanCreators(Class<?> ofClass) {
		List<BeanCreator> creators = new ArrayList<BeanCreator>();
		provideConstructorCreators(ofClass, creators);
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			provideMethodCreators(clazz, creators, ofClass);
		}
		return creators;
	}

	@Override
	public void provideBeanPropertyAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor>> accessorsMap) {
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			// first lookup for fields
			if (useFields) provideFieldAccessors(clazz, accessorsMap, ofClass);
			// and now search methods (getters)
			if (useGettersAndSetters) provideMethodAccessors(clazz, accessorsMap, ofClass);
		}
	}

	@Override
	public void provideBeanPropertyMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator>> mutatorsMap) {
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			// first lookup for fields
			if (useFields) provideFieldMutators(clazz, mutatorsMap, ofClass);
			// and now search methods (getters)
			if (useGettersAndSetters) provideMethodMutators(clazz, mutatorsMap, ofClass);
		}
	}

	protected void provideConstructorCreators(Class<?> ofClass, List<BeanCreator> creators) {
		Constructor<?>[] ctrs = ofClass.getDeclaredConstructors();
		for (Constructor<?> ctr : ctrs) {
			if (mutatorAccessorResolver.isCreator(ctr, ofClass)) {
				Type[] parameterTypes = ctr.getGenericParameterTypes();
				int paramCnt = parameterTypes.length;
				String[] parameterNames = new String[paramCnt];
				int idx = 0;
				for (; idx < paramCnt; idx++) {
					String name = nameResolver.resolve(idx, ctr);
					/*
					 * TODO must names be resolved? Or could we use types to inject if the name is
					 * missing... in this case add some method to beanCreator to check if a property
					 * is one of its methodparameters? For the moment just don't accept creators
					 * with missing names...
					 */
					if (name == null)
						break;
					parameterNames[idx] = name;
				}

				if (idx == paramCnt)
					creators.add(createCreator(ofClass, ctr, parameterNames));
			}
		}
	}
	
	protected BeanCreator createCreator(Class<?> ofClass, Constructor<?> ctr, String[] resolvedNames) {
		return new BeanCreator.ConstructorBeanCreator(ofClass, ctr,
				resolvedNames);
	}

	/**
	 * Hum its not so nice as its quite identic to provideBeanCreators, but we can't use a single
	 * generic method for handling constructors and methods...even if they have a lot of things in
	 * common...
	 * 
	 * @param ofClass
	 * @param creators
	 */
	protected void provideMethodCreators(Class<?> ofClass, List<BeanCreator> creators, Class<?> baseClass) {
		Method[] ctrs = ofClass.getDeclaredMethods();
		for (Method ctr : ctrs) {
			if (mutatorAccessorResolver.isCreator(ctr, baseClass)) {
				Type[] parameterTypes = ctr.getGenericParameterTypes();
				int paramCnt = parameterTypes.length;
				String[] parameterNames = new String[paramCnt];
				int idx = 0;
				for (; idx < paramCnt; idx++) {
					String name = nameResolver.resolve(idx, ctr);
					/*
					 * TODO must names be resolved? Or could we use types to inject if the name is
					 * missing... in this case add some method to beanCreator to check if a property
					 * is one of its methodparameters? For the moment just don't accept creators
					 * with missing names...
					 */
					if (name == null)
						break;
					parameterNames[idx] = name;
				}

				if (idx == paramCnt)
					creators.add(createCreator(baseClass, ctr, parameterNames));
			}
		}
	}
	
	protected BeanCreator createCreator(Class<?> ofClass, Method method, String[] resolvedNames) {
		return new BeanCreator.MethodBeanCreator(ofClass, method, resolvedNames);
	}

	protected void provideFieldAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor>> accessorsMap, Class<?> baseClass) {
		Field[] fields = ofClass.getDeclaredFields();
		for (Field field : fields) {
			if (mutatorAccessorResolver.isAccessor(field,  baseClass)) {
				String name = nameResolver.resolve(field);
				if (name == null) {
					throw new IllegalStateException("Field '" + field.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as accessor but its name couldn't be resolved!");
				}
				update(createAccessor(name, field, baseClass), accessorsMap);
			}
		}
	}
	
	protected PropertyAccessor createAccessor(String name, Field field, Class<?> baseClass) {
		return new PropertyAccessor.FieldAccessor(name, field, baseClass);
	}

	protected void provideMethodAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor>> accessorsMap, Class<?> baseClass) {
		Method[] methods = ofClass.getDeclaredMethods();
		for (Method method : methods) {
			if (mutatorAccessorResolver.isAccessor(method, baseClass)) {
				String name = nameResolver.resolve(method);
				if (name == null) {
					throw new IllegalStateException("Method '" + method.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as accessor but its name couldn't be resolved!");
				}
				update(createAccessor(name, method, baseClass), accessorsMap);
			}
		}
	}
	
	protected PropertyAccessor createAccessor(String name, Method method, Class<?> baseClass) {
		return new PropertyAccessor.MethodAccessor(name, method, baseClass);
	}

	protected void provideFieldMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator>> mutatorsMap, Class<?> baseClass) {
		Field[] fields = ofClass.getDeclaredFields();
		for (Field field : fields) {
			if (mutatorAccessorResolver.isMutator(field, baseClass)) {
				String name = nameResolver.resolve(field);
				if (name == null) {
					throw new IllegalStateException("Field '" + field.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as mutator but its name couldn't be resolved!");
				}
				update(createMutator(name, field, baseClass), mutatorsMap);
			}
		}
	}
	
	protected PropertyMutator createMutator(String name, Field field, Class<?> baseClass) {
		return new PropertyMutator.FieldMutator(name, field, baseClass);
	}
	
	protected void provideMethodMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator>> mutatorsMap, Class<?> baseClass) {
		Method[] methods = ofClass.getDeclaredMethods();
		for (Method method : methods) {
			if (mutatorAccessorResolver.isMutator(method, baseClass)) {
				String name = nameResolver.resolve(method);
				if (name == null) {
					throw new IllegalStateException("Method '" + method.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as mutator but its name couldn't be resolved!");
				}
				update(createMutator(name, method, baseClass), mutatorsMap);
			}
		}
	}

	protected PropertyMutator createMutator(String name, Method method, Class<?> baseClass) {
		return new PropertyMutator.MethodMutator(name, method, baseClass);
	}

	protected <T extends BeanProperty> void update(T property, Map<String, LinkedList<T>> map) {
		LinkedList<T> accessors = map.get(property.name);
		if (accessors == null) {
			accessors = new LinkedList<T>();
			map.put(property.name, accessors);
		}
		accessors.add(property);
	}

	@Override
	protected void checkAndUpdate(List<BeanCreator> creators) {
		// if ( creators == null || creators.isEmpty() ) throw new
		// IllegalStateException("No valid bean creator found!");
	}

	@Override
	protected PropertyAccessor checkAndMergeAccessors(String name,
			LinkedList<PropertyAccessor> accessors) {
		PropertyAccessor accessor = _mostSpecificPropertyDeclaringClass(name, accessors);
		return accessor;
	}

	@Override
	protected PropertyMutator checkAndMergeMutators(String name,
			LinkedList<PropertyMutator> mutators) {
		PropertyMutator mutator = _mostSpecificPropertyDeclaringClass(name, mutators);
		return mutator;
	}

	protected <T extends BeanProperty> T _mostSpecificPropertyDeclaringClass(String name,
			LinkedList<T> properties) {
		Iterator<T> it = properties.iterator();
		T property = it.next();
		for (; it.hasNext();) {
			T next = it.next();
			// 1 we search the most specialized class containing this property
			// 3 TODO should we use types in case of generics?

			// with highest priority
			if (property.declaringClass.equals(next.declaringClass)
					&& property.priority() < next.priority()) {
				property = next;
			} else if (property.declaringClass.isAssignableFrom(next.declaringClass)) {
				property = next;
			} else if (next.declaringClass.isAssignableFrom(property.declaringClass)) {
				continue;
			} else {
				throw new IllegalStateException("Property named '" + name
						+ "' has two properties with different types : " + property.signature()
						+ " and " + next.signature());
			}
		}

		return property;
	}

	// TODO must be rethinked
	@Override
	protected void mergeMutatorsWithCreatorProperties(Map<String, PropertyMutator> mutators,
			List<BeanCreator> creators) {
		for (BeanCreator creator : creators) {
			for (Map.Entry<String, BeanCreatorProperty> entry : creator.parameters.entrySet()) {
				PropertyMutator muta = mutators.get(entry.getKey());
				if (muta == null) {
					// add to mutators only creator properties that don't exist as standard
					// mutator (dont exist as field or method, but only as ctr arg)
					mutators.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}
}
