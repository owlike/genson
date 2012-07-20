package org.genson.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.genson.Converter;
import org.genson.Genson;
import org.genson.reflect.BeanCreator.BeanCreatorProperty;
import static org.genson.Trilean.*;

/**
 * Standard implementation of AbstractBeanDescriptorProvider that uses
 * {@link BeanMutatorAccessorResolver} and {@link PropertyNameResolver}. If you want to change the
 * way BeanDescriptors are created you can subclass this class and override the needed methods. If
 * you only want to create instances of your own PropertyMutators/PropertyAccessors or BeanCreators
 * just override the corresponding createXXX methods.
 * 
 * @author eugen
 * 
 */
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
		if (!useFields && !useGettersAndSetters)
			throw new IllegalArgumentException(
					"You must allow at least one mode: with fields or methods.");
	}

	@Override
	public <T> List<BeanCreator<T>> provideBeanCreators(Class<?> ofClass, Genson genson) {
		List<BeanCreator<T>> creators = new ArrayList<BeanCreator<T>>();
		if (ofClass.isMemberClass() && (ofClass.getModifiers() & Modifier.STATIC) == 0)
			return creators;

		provideConstructorCreators(ofClass, creators, genson);
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			provideMethodCreators(clazz, creators, ofClass, genson);
		}
		return creators;
	}

	@Override
	public <T> void provideBeanPropertyAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap, Genson genson) {
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			// first lookup for fields
			if (useFields)
				provideFieldAccessors(clazz, accessorsMap, ofClass, genson);
			// and now search methods (getters)
			if (useGettersAndSetters)
				provideMethodAccessors(clazz, accessorsMap, ofClass, genson);
		}
	}

	@Override
	public <T> void provideBeanPropertyMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap, Genson genson) {
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			// first lookup for fields
			if (useFields)
				provideFieldMutators(clazz, mutatorsMap, ofClass, genson);
			// and now search methods (getters)
			if (useGettersAndSetters)
				provideMethodMutators(clazz, mutatorsMap, ofClass, genson);
		}
	}

	protected <T> void provideConstructorCreators(Class<?> ofClass, List<BeanCreator<T>> creators,
			Genson genson) {
		@SuppressWarnings("unchecked")
		Constructor<T>[] ctrs = (Constructor<T>[]) ofClass.getDeclaredConstructors();
		for (Constructor<T> ctr : ctrs) {
			if (TRUE == mutatorAccessorResolver.isCreator(ctr, ofClass)) {
				Type[] parameterTypes = ctr.getGenericParameterTypes();
				int paramCnt = parameterTypes.length;
				String[] parameterNames = new String[paramCnt];
				int idx = 0;
				for (; idx < paramCnt; idx++) {
					String name = nameResolver.resolve(idx, ctr);
					if (name == null)
						break;
					parameterNames[idx] = name;
				}

				if (idx == paramCnt) {
					@SuppressWarnings("unchecked")
					BeanCreator<T> creator = createCreator((Class<T>) ofClass, ctr, parameterNames,
							genson);
					creators.add(creator);
				}
			}
		}
	}

	protected <T> BeanCreator<T> createCreator(Class<T> ofClass, Constructor<T> ctr,
			String[] resolvedNames, Genson genson) {
		return new BeanCreator.ConstructorBeanCreator<T>(ofClass, ctr, resolvedNames,
				resolveConverters(ctr.getGenericParameterTypes(), genson));
	}

	/**
	 * Hum its not so nice as its quite identic to provideBeanCreators, but we can't use a single
	 * generic method for handling constructors and methods...even if they have a lot of things in
	 * common...
	 * 
	 * @param ofClass
	 * @param creators
	 */
	protected <T> void provideMethodCreators(Class<?> ofClass, List<BeanCreator<T>> creators,
			Class<?> baseClass, Genson genson) {
		Method[] ctrs = ofClass.getDeclaredMethods();
		for (Method ctr : ctrs) {
			if (TRUE == mutatorAccessorResolver.isCreator(ctr, baseClass)) {
				Type[] parameterTypes = ctr.getGenericParameterTypes();
				int paramCnt = parameterTypes.length;
				String[] parameterNames = new String[paramCnt];
				int idx = 0;
				for (; idx < paramCnt; idx++) {
					String name = nameResolver.resolve(idx, ctr);
					if (name == null)
						break;
					parameterNames[idx] = name;
				}

				if (idx == paramCnt) {
					BeanCreator<T> creator = createCreator(baseClass, ctr, parameterNames, genson);
					creators.add(creator);
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	// ofClass is not necessarily of same type as method return type, as ofClass corresponds to the
	// declaring class!
	protected <T> BeanCreator<T> createCreator(Class<?> ofClass, Method method,
			String[] resolvedNames, Genson genson) {
		return new BeanCreator.MethodBeanCreator(method, resolvedNames, resolveConverters(
				method.getGenericParameterTypes(), genson));
	}

	private Converter<?>[] resolveConverters(Type[] types, Genson genson) {
		Converter<?>[] converters = new Converter<?>[types.length];
		for (int i = 0; i < types.length; i++) {
			converters[i] = genson.provideConverter(types[i]);
		}
		return converters;
	}

	protected <T> void provideFieldAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap, Class<?> baseClass,
			Genson genson) {
		Field[] fields = ofClass.getDeclaredFields();
		for (Field field : fields) {
			if (TRUE == mutatorAccessorResolver.isAccessor(field, baseClass)) {
				String name = nameResolver.resolve(field);
				if (name == null) {
					throw new IllegalStateException("Field '" + field.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as accessor but its name couldn't be resolved!");
				}
				@SuppressWarnings("unchecked")
				PropertyAccessor<T, ?> accessor = createAccessor(name, field, (Class<T>) baseClass,
						genson);
				update(accessor, accessorsMap);
			}
		}
	}

	protected <T> PropertyAccessor<T, ?> createAccessor(String name, Field field,
			Class<T> baseClass, Genson genson) {
		return new PropertyAccessor.FieldAccessor<T, Object>(name, field, baseClass,
				genson.provideConverter(field.getGenericType()));
	}

	protected <T> void provideMethodAccessors(Class<?> ofClass,
			Map<String, LinkedList<PropertyAccessor<T, ?>>> accessorsMap, Class<?> baseClass,
			Genson genson) {
		Method[] methods = ofClass.getDeclaredMethods();
		for (Method method : methods) {
			if (TRUE == mutatorAccessorResolver.isAccessor(method, baseClass)) {
				String name = nameResolver.resolve(method);
				if (name == null) {
					throw new IllegalStateException("Method '" + method.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as accessor but its name couldn't be resolved!");
				}
				PropertyAccessor<T, ?> accessor = createAccessor(name, method, baseClass, genson);
				update(accessor, accessorsMap);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> PropertyAccessor<T, ?> createAccessor(String name, Method method,
			Class<?> baseClass, Genson genson) {
		return new PropertyAccessor.MethodAccessor(name, method, baseClass,
				genson.provideConverter(method.getGenericReturnType()));
	}

	protected <T> void provideFieldMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap, Class<?> baseClass,
			Genson genson) {
		Field[] fields = ofClass.getDeclaredFields();
		for (Field field : fields) {
			if (TRUE == mutatorAccessorResolver.isMutator(field, baseClass)) {
				String name = nameResolver.resolve(field);
				if (name == null) {
					throw new IllegalStateException("Field '" + field.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as mutator but its name couldn't be resolved!");
				}
				@SuppressWarnings("unchecked")
				PropertyMutator<T, ?> mutator = createMutator(name, field, (Class<T>) baseClass,
						genson);
				update(mutator, mutatorsMap);
			}
		}
	}

	protected <T> PropertyMutator<T, ?> createMutator(String name, Field field, Class<T> baseClass,
			Genson genson) {
		return new PropertyMutator.FieldMutator<T, Object>(name, field, baseClass,
				genson.provideConverter(field.getGenericType()));
	}

	protected <T> void provideMethodMutators(Class<?> ofClass,
			Map<String, LinkedList<PropertyMutator<T, ?>>> mutatorsMap, Class<?> baseClass,
			Genson genson) {
		Method[] methods = ofClass.getDeclaredMethods();
		for (Method method : methods) {
			if (TRUE == mutatorAccessorResolver.isMutator(method, baseClass)) {
				String name = nameResolver.resolve(method);
				if (name == null) {
					throw new IllegalStateException("Method '" + method.getName() + "' from class "
							+ ofClass.getName()
							+ " has been discovered as mutator but its name couldn't be resolved!");
				}
				PropertyMutator<T, ?> mutator = createMutator(name, method, baseClass, genson);
				update(mutator, mutatorsMap);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> PropertyMutator<T, ?> createMutator(String name, Method method,
			Class<?> baseClass, Genson genson) {
		return new PropertyMutator.MethodMutator(name, method, baseClass,
				genson.provideConverter(method.getGenericParameterTypes()[0]));
	}

	protected <T extends BeanProperty<?>> void update(T property, Map<String, LinkedList<T>> map) {
		LinkedList<T> accessors = map.get(property.name);
		if (accessors == null) {
			accessors = new LinkedList<T>();
			map.put(property.name, accessors);
		}
		accessors.add(property);
	}

	@Override
	protected <T> void checkAndUpdate(List<BeanCreator<T>> creators) {
		// if ( creators == null || creators.isEmpty() ) throw new
		// IllegalStateException("No valid bean creator found!");
	}

	@Override
	protected <T> PropertyAccessor<T, ?> checkAndMergeAccessors(String name,
			LinkedList<PropertyAccessor<T, ?>> accessors) {
		PropertyAccessor<T, ?> accessor = _mostSpecificPropertyDeclaringClass(name, accessors);
		return accessor;
	}

	@Override
	protected <T> PropertyMutator<T, ?> checkAndMergeMutators(String name,
			LinkedList<PropertyMutator<T, ?>> mutators) {
		PropertyMutator<T, ?> mutator = _mostSpecificPropertyDeclaringClass(name, mutators);
		return mutator;
	}

	protected <T extends BeanProperty<?>> T _mostSpecificPropertyDeclaringClass(String name,
			LinkedList<T> properties) {
		Iterator<T> it = properties.iterator();
		T property = it.next();
		for (; it.hasNext();) {
			T next = it.next();
			// 1 we search the most specialized class containing this property
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

	/*
	 * TODO must be rethinked? maybe not, but a solution could be to separate ctr properties from
	 * the others, and while deserializing to check first in normal mutator map and if null check
	 * for property in the ctr props...
	 */
	@Override
	protected <T> void mergeMutatorsWithCreatorProperties(
			Map<String, PropertyMutator<T, ?>> mutators, List<BeanCreator<T>> creators) {
		for (BeanCreator<?> creator : creators) {
			for (Map.Entry<String, ? extends BeanCreatorProperty<?, ?>> entry : creator.parameters
					.entrySet()) {
				PropertyMutator<T, ?> muta = mutators.get(entry.getKey());
				if (muta == null) {
					// add to mutators only creator properties that don't exist as standard
					// mutator (dont exist as field or method, but only as ctr arg)
					@SuppressWarnings("unchecked")
					BeanCreatorProperty<T, ?> ctrProperty = (BeanCreatorProperty<T, ?>) entry
							.getValue();
					mutators.put(entry.getKey(), ctrProperty);
				}
			}
		}
	}
}
