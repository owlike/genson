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

/*
 * TODO configure by visibility
 */
public interface BeanMutatorAccessorResolver {
	public boolean isCreator(Constructor<?> constructor, Class<?> fromClass);
	
	public boolean isCreator(Method method, Class<?> fromClass);

	public boolean isAccessor(Field field, Class<?> fromClass);

	public boolean isAccessor(Method method, Class<?> fromClass);

	public boolean isMutator(Field field, Class<?> fromClass);

	public boolean isMutator(Method method, Class<?> fromClass);

	public static class ConventionalBeanResolver implements BeanMutatorAccessorResolver {

		/**
		 * Will resolve all public/package and non transient/static fields as accesssors.
		 */
		@Override
		public boolean isAccessor(Field field, Class<?> fromClass) {
			if (mustIgnore(field)) return false;
			if (mustInclude(field)) return true;
			int modifier = field.getModifiers();
			return !Modifier.isTransient(modifier) && !Modifier.isStatic(modifier);
		}

		/**
		 * Resolves all public methods starting with get/is (boolean) and parameter less as
		 * accessors.
		 */
		@Override
		public boolean isAccessor(Method method, Class<?> fromClass) {
			if (mustIgnore(method)) return false;
			if (mustInclude(method)) return true;
			
			String name = method.getName();
			int len = name.length();			
			if (checkVisibility(method)
					&& ((len > 3 && name.startsWith("get")) || (len > 2 && name.startsWith("is") && (TypeUtil
							.match(method.getGenericReturnType(), Boolean.class, false) || TypeUtil
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
			if (mustIgnore(constructor)) return false;
			int modifier = constructor.getModifiers();
			return Modifier.isPublic(modifier) || 
					!(Modifier.isPrivate(modifier) || Modifier.isProtected(modifier));
		}

		@Override
		public boolean isCreator(Method method, Class<?> fromClass) {
			if (method.getAnnotation(Creator.class) != null) {
				if (Modifier.isStatic(method.getModifiers())) return true;
				throw new TransformationRuntimeException("Method " + method.toGenericString() + " annotated with @Creator must be static!");
			}
			return false;
		}

		@Override
		public boolean isMutator(Field field, Class<?> fromClass) {
			// default implementation will try to be symetric
			return isAccessor(field, fromClass);
		}

		@Override
		public boolean isMutator(Method method, Class<?> fromClass) {
			if (mustIgnore(method)) return false;
			if (mustInclude(method)) return true;
			
			if (checkVisibility(method) && method.getName().length() > 3
					&& method.getName().startsWith("set") && method.getParameterTypes().length == 1
					&& method.getReturnType() == void.class)
				return true;

			return false;
		}
		
		protected boolean mustIgnore(AccessibleObject property) {
			return property.isAnnotationPresent(JsonIgnore.class);
		}

		protected boolean mustInclude(AccessibleObject property) {
			return property.isAnnotationPresent(JsonProperty.class);
		}
		
		protected boolean checkVisibility(Method method) {
			int modifier = method.getModifiers();
			return Modifier.isPublic(modifier) && !Modifier.isNative(modifier)
					&& !Modifier.isAbstract(modifier) && !Modifier.isStatic(modifier);
		}
	}

}
