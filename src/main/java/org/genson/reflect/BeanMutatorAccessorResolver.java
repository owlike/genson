package org.genson.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.genson.TransformationRuntimeException;
import org.genson.annotation.Creator;
import org.genson.annotation.JsonIgnore;
import org.genson.annotation.JsonProperty;

public interface BeanMutatorAccessorResolver {
	public boolean isCreator(Constructor<?> constructor, Class<?> fromClass);
	
	public boolean isCreator(Method method, Class<?> fromClass);

	public boolean isAccessor(Field field, Class<?> fromClass);

	public boolean isAccessor(Method method, Class<?> fromClass);

	public boolean isMutator(Field field, Class<?> fromClass);

	public boolean isMutator(Method method, Class<?> fromClass);

	public static class StandardMutaAccessorResolver implements BeanMutatorAccessorResolver {
		private final VisibilityFilter filedVisibilityFilter;
		private final VisibilityFilter methodVisibilityFilter;
		private final VisibilityFilter creatorVisibilityFilter;
		
		public StandardMutaAccessorResolver() {
			this(VisibilityFilter.DEFAULT, VisibilityFilter.PACKAGE_PUBLIC, VisibilityFilter.PACKAGE_PUBLIC);
		}
		
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
		@Override
		public boolean isAccessor(Field field, Class<?> fromClass) {
			// ok to look for this$ is ugly but it will do the job for the moment
			if (mustIgnore(field, true) || field.getName().startsWith("this$")) return false;
			if (mustInclude(field, true)) return true;
			return filedVisibilityFilter.isVisible(field);
		}

		/**
		 * Resolves all public methods starting with get/is (boolean) and parameter less as
		 * accessors.
		 */
		@Override
		public boolean isAccessor(Method method, Class<?> fromClass) {
			if (mustIgnore(method, true)) return false;
			if (mustInclude(method, true)) return true;
			
			String name = method.getName();
			int len = name.length();			
			if (methodVisibilityFilter.isVisible(method)
					&& ((len > 3 && name.startsWith("get")) || (len > 2 && name.startsWith("is") && (TypeUtil
							.match(TypeUtil.expandType(method.getGenericReturnType(), fromClass), Boolean.class, false) || TypeUtil
							.match(method.getGenericReturnType(), boolean.class, false))))
					&& method.getParameterTypes().length == 0)
				return true;
			
			return false;
		}

		@Override
		public boolean isCreator(Constructor<?> constructor, Class<?> fromClass) {
			/*
			 * hum... it depends on different things, such as parameters name resolution, types, etc
			 * but we are not supposed to handle it here... lets only check visibility and handle
			 * it in the provider implementations
			 */
			if (mustIgnore(constructor, false)) return false;
			return creatorVisibilityFilter.isVisible(constructor);
		}

		@Override
		public boolean isCreator(Method method, Class<?> fromClass) {
			if (method.getAnnotation(Creator.class) != null) {
				if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())) return true;
				throw new TransformationRuntimeException("Method " + method.toGenericString() + " annotated with @Creator must be static!");
			}
			return false;
		}

		@Override
		public boolean isMutator(Field field, Class<?> fromClass) {
			if (mustIgnore(field, false) || field.getName().startsWith("this$")) return false;
			if (mustInclude(field, false)) return true;
			int modifier = field.getModifiers();
			return !Modifier.isTransient(modifier) && !Modifier.isStatic(modifier);
		}

		@Override
		public boolean isMutator(Method method, Class<?> fromClass) {
			if (mustIgnore(method, false)) return false;
			if (mustInclude(method, false)) return true;
			
			if (methodVisibilityFilter.isVisible(method) && method.getName().length() > 3
					&& method.getName().startsWith("set") && method.getParameterTypes().length == 1
					&& method.getReturnType() == void.class)
				return true;

			return false;
		}
		
		protected boolean mustIgnore(AccessibleObject property, boolean forSerialization) {
			JsonIgnore ignore = property.getAnnotation(JsonIgnore.class);
			if (ignore != null) {
				if (forSerialization) return !ignore.serialize();
				else return !ignore.deserialize();
			}
			return false;
		}

		protected boolean mustInclude(AccessibleObject property, boolean forSerialization) {
			JsonProperty prop = property.getAnnotation(JsonProperty.class);
			if (prop != null) {
				if (forSerialization) return prop.serialize();
				else return prop.deserialize();
			}
			return false;
		}
	}

}
