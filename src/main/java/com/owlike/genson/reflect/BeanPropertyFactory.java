package com.owlike.genson.reflect;

import static com.owlike.genson.reflect.TypeUtil.getRawClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;

public interface BeanPropertyFactory {
	public PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson);

	public PropertyAccessor createAccessor(String name, Method method, Type ofType, Genson genson);

	public BeanCreator createCreator(Type ofType, Constructor<?> ctr, String[] resolvedNames,
			Genson genson);

	public BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
			Genson genson);

	public PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson);

	public PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson);

	public static class CompositeFactory implements BeanPropertyFactory {
		private final List<BeanPropertyFactory> factories;

		public CompositeFactory(List<? extends BeanPropertyFactory> factories) {
			this.factories = new ArrayList<BeanPropertyFactory>(factories);
		}

		@Override
		public PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson) {
			for (BeanPropertyFactory factory : factories) {
				PropertyAccessor accessor = factory.createAccessor(name, field, ofType, genson);
				if (accessor != null) return accessor;
			}
			throw new RuntimeException("Failed to create a accessor for field " + field);
		}

		@Override
		public PropertyAccessor createAccessor(String name, Method method, Type ofType,
				Genson genson) {
			for (BeanPropertyFactory factory : factories) {
				PropertyAccessor accessor = factory.createAccessor(name, method, ofType, genson);
				if (accessor != null) return accessor;
			}
			throw new RuntimeException("Failed to create a accessor for method " + method);
		}

		@Override
		public BeanCreator createCreator(Type ofType, Constructor<?> ctr,
				String[] resolvedNames, Genson genson) {
			for (BeanPropertyFactory factory : factories) {
				BeanCreator creator = factory.createCreator(ofType, ctr, resolvedNames, genson);
				if (creator != null) return creator;
			}
			throw new RuntimeException("Failed to create a BeanCreator for constructor " + ctr);
		}

		@Override
		public BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
				Genson genson) {
			for (BeanPropertyFactory factory : factories) {
				BeanCreator creator = factory.createCreator(ofType, method, resolvedNames,
						genson);
				if (creator != null) return creator;
			}
			throw new RuntimeException("Failed to create a BeanCreator for method " + method);
		}

		@Override
		public PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson) {
			for (BeanPropertyFactory factory : factories) {
				PropertyMutator mutator = factory.createMutator(name, field, ofType, genson);
				if (mutator != null) return mutator;
			}
			throw new RuntimeException("Failed to create a mutator for field " + field);
		}

		@Override
		public PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson) {
			for (BeanPropertyFactory factory : factories) {
				PropertyMutator mutator = factory.createMutator(name, method, ofType, genson);
				if (mutator != null) return mutator;
			}
			throw new RuntimeException("Failed to create a mutator for method " + method);
		}
	}

	public static class StandardFactory implements BeanPropertyFactory {
		public PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson) {
			Class<?> ofClass = getRawClass(ofType);
			Type expandedType = TypeUtil.expandType(field.getGenericType(), ofType);
			return new PropertyAccessor.FieldAccessor(name, field, expandedType, ofClass,
					genson.provideConverter(expandedType));
		}

		public PropertyAccessor createAccessor(String name, Method method, Type ofType,
				Genson genson) {
			Type expandedType = TypeUtil.expandType(method.getGenericReturnType(), ofType);
			return new PropertyAccessor.MethodAccessor(name, method, expandedType,
					getRawClass(ofType), genson.provideConverter(expandedType));
		}

		public PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson) {
			Class<?> ofClass = getRawClass(ofType);
			Type expandedType = TypeUtil.expandType(field.getGenericType(), ofType);
			return new PropertyMutator.FieldMutator(name, field, expandedType, ofClass,
					genson.provideConverter(expandedType));
		}

		public PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson) {
			Type expandedType = TypeUtil.expandType(method.getGenericParameterTypes()[0], ofType);
			return new PropertyMutator.MethodMutator(name, method, expandedType,
					getRawClass(ofType), genson.provideConverter(expandedType));
		}

		// ofClass is not necessarily of same type as method return type, as ofClass corresponds to
		// the declaring class!
		public BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
				Genson genson) {
			return new BeanCreator.MethodBeanCreator(method, resolvedNames,
					resolveConverters(method.getGenericParameterTypes(), ofType, genson));
		}

		public BeanCreator createCreator(Type ofType, Constructor<?> ctr,
				String[] resolvedNames, Genson genson) {
			return new BeanCreator.ConstructorBeanCreator(getRawClass(ofType), ctr, resolvedNames,
					resolveConverters(ctr.getGenericParameterTypes(), ofType, genson));
		}

		@SuppressWarnings("unchecked")
		private Converter<Object>[] resolveConverters(Type[] types, Type ofType, Genson genson) {
			Converter<?>[] converters = new Converter<?>[types.length];
			for (int i = 0; i < types.length; i++) {
				converters[i] = genson.provideConverter(TypeUtil.expandType(types[i], ofType));
			}
			return (Converter<Object>[]) converters;
		}
	}
}
