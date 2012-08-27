package com.owlike.genson.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.owlike.genson.Trilean.FALSE;
import static com.owlike.genson.Trilean.TRUE;

import com.owlike.genson.TransformationRuntimeException;
import com.owlike.genson.Trilean;
import com.owlike.genson.annotation.Creator;
import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;

/**
 * BeanMutatorAccessorResolver interface must be implemented by class who want to resolve mutators
 * (fields or methods that allow you to modify a property), accessors (fields or methods that allow
 * you to retrieve the value of a property) and creators (constructors or static methods that allow
 * you to create objects).
 * 
 * All methods return a {@link com.owlike.genson.Trilean Trilean}, so it may be TRUE, FALSE or UNKNOWN.
 * This will allow us to separate the kind of information each implementation works on (one for
 * annotations, another for visibility, etc) and to chain them. It also allows an easier addition of
 * new features without modifying the existing code. Have a look at <a href=
 * "http://code.google.com/p/genson/source/browse/src/main/java/com/owlike/genson/reflect/BeanMutatorAccessorResolver.java"
 * >StandardMutaAccessorResolver</a> for an example of BeanMutatorAccessorResolver implementation.
 * 
 * To register your own implementation instead of the one by default use the genson builder.
 * 
 * <pre>
 * new Genson.Builder().set(yourImplementation).create();
 * </pre>
 * 
 * @see com.owlike.genson.Trilean Trilean
 * @see StandardMutaAccessorResolver
 * @see AbstractBeanDescriptorProvider
 * @see BaseBeanDescriptorProvider
 * @see BeanViewDescriptorProvider
 * 
 * @author eugen
 * 
 */
public interface BeanMutatorAccessorResolver {
	public Trilean isCreator(Constructor<?> constructor, Class<?> fromClass);

	public Trilean isCreator(Method method, Class<?> fromClass);

	public Trilean isAccessor(Field field, Class<?> fromClass);

	public Trilean isAccessor(Method method, Class<?> fromClass);

	public Trilean isMutator(Field field, Class<?> fromClass);

	public Trilean isMutator(Method method, Class<?> fromClass);

	public static class BaseResolver implements BeanMutatorAccessorResolver {
		@Override
		public Trilean isAccessor(Field field, Class<?> fromClass) {
			return Trilean.UNKNOWN;
		}

		@Override
		public Trilean isAccessor(Method method, Class<?> fromClass) {
			return Trilean.UNKNOWN;
		}

		@Override
		public Trilean isCreator(Constructor<?> constructor, Class<?> fromClass) {
			return Trilean.UNKNOWN;
		}

		@Override
		public Trilean isCreator(Method method, Class<?> fromClass) {
			return Trilean.UNKNOWN;
		}

		@Override
		public Trilean isMutator(Field field, Class<?> fromClass) {
			return Trilean.UNKNOWN;
		}

		@Override
		public Trilean isMutator(Method method, Class<?> fromClass) {
			return Trilean.UNKNOWN;
		}
	}
	
	public static class CompositeResolver implements BeanMutatorAccessorResolver {
		private List<BeanMutatorAccessorResolver> components;

		public CompositeResolver(List<BeanMutatorAccessorResolver> components) {
			if (components == null || components.isEmpty()) {
				throw new IllegalArgumentException(
						"The composite resolver must have at least one resolver as component!");
			}
			this.components = new LinkedList<BeanMutatorAccessorResolver>(components);
		}

		public CompositeResolver add(BeanMutatorAccessorResolver... resolvers) {
			components.addAll(0, Arrays.asList(resolvers));
			return this;
		}

		@Override
		public Trilean isAccessor(Field field, Class<?> fromClass) {
			Trilean resolved = Trilean.UNKNOWN;
			for (Iterator<BeanMutatorAccessorResolver> it = components.iterator(); resolved == null || resolved.equals(Trilean.UNKNOWN)
					&& it.hasNext();) {
				resolved = it.next().isAccessor(field, fromClass);
			}
			return resolved;
		}

		@Override
		public Trilean isAccessor(Method method, Class<?> fromClass) {
			Trilean resolved = Trilean.UNKNOWN;
			for (Iterator<BeanMutatorAccessorResolver> it = components.iterator(); resolved == null || resolved.equals(Trilean.UNKNOWN)
					&& it.hasNext();) {
				resolved = it.next().isAccessor(method, fromClass);
			}
			return resolved;
		}

		@Override
		public Trilean isCreator(Constructor<?> constructor, Class<?> fromClass) {
			Trilean resolved = Trilean.UNKNOWN;
			for (Iterator<BeanMutatorAccessorResolver> it = components.iterator(); resolved == null || resolved.equals(Trilean.UNKNOWN)
					&& it.hasNext();) {
				resolved = it.next().isCreator(constructor, fromClass);
			}
			return resolved;
		}

		@Override
		public Trilean isCreator(Method method, Class<?> fromClass) {
			Trilean resolved = Trilean.UNKNOWN;
			for (Iterator<BeanMutatorAccessorResolver> it = components.iterator(); resolved == null || resolved.equals(Trilean.UNKNOWN)
					&& it.hasNext();) {
				resolved = it.next().isCreator(method, fromClass);
			}
			return resolved;
		}

		@Override
		public Trilean isMutator(Field field, Class<?> fromClass) {
			Trilean resolved = Trilean.UNKNOWN;
			for (Iterator<BeanMutatorAccessorResolver> it = components.iterator(); resolved == null || resolved.equals(Trilean.UNKNOWN)
					&& it.hasNext();) {
				resolved = it.next().isMutator(field, fromClass);
			}
			return resolved;
		}

		@Override
		public Trilean isMutator(Method method, Class<?> fromClass) {
			Trilean resolved = Trilean.UNKNOWN;
			for (Iterator<BeanMutatorAccessorResolver> it = components.iterator(); resolved == null || resolved.equals(Trilean.UNKNOWN)
					&& it.hasNext();) {
				resolved = it.next().isMutator(method, fromClass);
			}
			return resolved;
		}
	}
	
	/**
	 * Standard implementation of BeanMutatorAccessorResolver. In the future it will probably be
	 * split into multiple implementations combined into a composite BeanMutatorAccessorResolver.
	 * Actually this implementation handles filtering by signature convention, visibility and
	 * annotations.
	 * 
	 * @author eugen
	 * 
	 */
	public static class StandardMutaAccessorResolver implements BeanMutatorAccessorResolver {
		private final VisibilityFilter filedVisibilityFilter;
		private final VisibilityFilter methodVisibilityFilter;
		private final VisibilityFilter creatorVisibilityFilter;

		/**
		 * Creates a new instance of StandardMutaAccessorResolver with
		 * {@link VisibilityFilter#DEFAULT} visibility for fields,
		 * {@link VisibilityFilter#PACKAGE_PUBLIC} visibility for methods and creators.
		 */
		public StandardMutaAccessorResolver() {
			this(VisibilityFilter.PACKAGE_PUBLIC, VisibilityFilter.PACKAGE_PUBLIC,
					VisibilityFilter.PACKAGE_PUBLIC);
		}

		/**
		 * Use this constructor if you want to customize the visibility filtering.
		 * 
		 * @param filedVisibilityFilter
		 * @param methodVisibilityFilter
		 * @param creatorVisibilityFilter
		 */
		public StandardMutaAccessorResolver(VisibilityFilter filedVisibilityFilter,
				VisibilityFilter methodVisibilityFilter, VisibilityFilter creatorVisibilityFilter) {
			super();
			this.filedVisibilityFilter = filedVisibilityFilter;
			this.methodVisibilityFilter = methodVisibilityFilter;
			this.creatorVisibilityFilter = creatorVisibilityFilter;
		}

		/**
		 * Will resolve all public/package and non transient/static fields as accesssors.
		 */
		public Trilean isAccessor(Field field, Class<?> fromClass) {
			// ok to look for this$ is ugly but it will do the job for the moment
			if (mustIgnore(field, true) || field.getName().startsWith("this$"))
				return FALSE;
			if (mustInclude(field, true))
				return TRUE;
			return Trilean.valueOf(filedVisibilityFilter.isVisible(field));
		}

		/**
		 * Resolves all public methods starting with get/is (boolean) and parameter less as
		 * accessors.
		 */
		public Trilean isAccessor(Method method, Class<?> fromClass) {
			if (mustIgnore(method, true))
				return FALSE;
			if (mustInclude(method, true) && method.getParameterTypes().length == 0)
				return TRUE;

			String name = method.getName();
			int len = name.length();
			if (methodVisibilityFilter.isVisible(method)
					&& ((len > 3 && name.startsWith("get")) || (len > 2 && name.startsWith("is") && (TypeUtil
							.match(TypeUtil.expandType(method.getGenericReturnType(), fromClass),
									Boolean.class, false) || TypeUtil.match(
							method.getGenericReturnType(), boolean.class, false))))
					&& method.getParameterTypes().length == 0)
				return TRUE;

			return FALSE;
		}

		public Trilean isCreator(Constructor<?> constructor, Class<?> fromClass) {
			/*
			 * hum... it depends on different things, such as parameters name resolution, types, etc
			 * but we are not supposed to handle it here... lets only check visibility and handle it
			 * in the provider implementations
			 */
			if (mustIgnore(constructor, false))
				return FALSE;
			return Trilean.valueOf(creatorVisibilityFilter.isVisible(constructor));
		}

		public Trilean isCreator(Method method, Class<?> fromClass) {
			if (method.getAnnotation(Creator.class) != null) {
				if (Modifier.isPublic(method.getModifiers())
						&& Modifier.isStatic(method.getModifiers()))
					return TRUE;
				throw new TransformationRuntimeException("Method " + method.toGenericString()
						+ " annotated with @Creator must be static!");
			}
			return FALSE;
		}

		public Trilean isMutator(Field field, Class<?> fromClass) {
			if (mustIgnore(field, false) || field.getName().startsWith("this$"))
				return FALSE;
			if (mustInclude(field, false))
				return TRUE;
			int modifier = field.getModifiers();
			return Trilean.valueOf(!Modifier.isTransient(modifier) && !Modifier.isStatic(modifier));
		}

		public Trilean isMutator(Method method, Class<?> fromClass) {
			if (mustIgnore(method, false))
				return FALSE;
			if (mustInclude(method, false) && method.getParameterTypes().length == 1)
				return TRUE;

			if (methodVisibilityFilter.isVisible(method) && method.getName().length() > 3
					&& method.getName().startsWith("set") && method.getParameterTypes().length == 1
					&& method.getReturnType() == void.class)
				return TRUE;

			return FALSE;
		}

		protected boolean mustIgnore(AccessibleObject property, boolean forSerialization) {
			JsonIgnore ignore = property.getAnnotation(JsonIgnore.class);
			if (ignore != null) {
				if (forSerialization)
					return !ignore.serialize();
				else
					return !ignore.deserialize();
			}
			return false;
		}

		protected boolean mustInclude(AccessibleObject property, boolean forSerialization) {
			JsonProperty prop = property.getAnnotation(JsonProperty.class);
			if (prop != null) {
				if (forSerialization)
					return prop.serialize();
				else
					return prop.deserialize();
			}
			return false;
		}
	}

}
