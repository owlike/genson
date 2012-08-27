package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.owlike.genson.annotation.JsonProperty;

/**
 * This interface is intended to be implemented by classes who want to change the way genson does
 * name resolution. The resolved name will be used in the generated stream during serialization and
 * injected into constructors/or setters during deserialization. If you can not resolve the name
 * just return null. You can have a look at the <a href=
 * "http://code.google.com/p/genson/source/browse/src/main/java/com/owlike/genson/reflect/PropertyNameResolver.java"
 * >source code</a> for an example.
 * 
 * @see com.owlike.genson.annotation.JsonProperty JsonProperty
 * 
 * @author eugen
 */
public interface PropertyNameResolver {
	/**
	 * Resolve the parameter name on position parameterIdx in the constructor fromConstructor.
	 * 
	 * @param parameterIdx
	 * @param fromConstructor
	 * @return the resolved name of the parameter or null
	 */
	public String resolve(int parameterIdx, Constructor<?> fromConstructor);

	/**
	 * Resolve the name of the parameter with parameterIdx as index in fromMethod method.
	 * @param parameterIdx
	 * @param fromMethod
	 * @return the resolved name of the parameter or null
	 */
	public String resolve(int parameterIdx, Method fromMethod);

	/**
	 * Resolve the property name from this field.
	 * 
	 * @param fromField - the field to use for name resolution.
	 * @return the resolved name or null.
	 */
	public String resolve(Field fromField);

	/**
	 * Resolve the property name from this method.
	 * 
	 * @param fromMethod - the method to be used for name resolution.
	 * @return the resolved name or null.
	 */
	public String resolve(Method fromMethod);

	public static class CompositePropertyNameResolver implements PropertyNameResolver {
		private List<PropertyNameResolver> components;

		public CompositePropertyNameResolver(List<PropertyNameResolver> components) {
			if (components == null || components.isEmpty()) {
				throw new IllegalArgumentException(
						"The composite resolver must have at least one resolver as component!");
			}
			this.components = new LinkedList<PropertyNameResolver>(components);
		}

		public CompositePropertyNameResolver add(PropertyNameResolver... resolvers) {
			// should at the head position so custom resolvers a privileged
			components.addAll(0, Arrays.asList(resolvers));
			return this;
		}

		public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
			String resolvedName = null;
			for (Iterator<PropertyNameResolver> it = components.iterator(); resolvedName == null
					&& it.hasNext();) {
				resolvedName = it.next().resolve(parameterIdx, fromConstructor);
			}
			return resolvedName;
		}

		public String resolve(int parameterIdx, Method fromMethod) {
			String resolvedName = null;
			for (Iterator<PropertyNameResolver> it = components.iterator(); resolvedName == null
					&& it.hasNext();) {
				resolvedName = it.next().resolve(parameterIdx, fromMethod);
			}
			return resolvedName;
		}

		public String resolve(Field fromField) {
			String resolvedName = null;
			for (Iterator<PropertyNameResolver> it = components.iterator(); resolvedName == null
					&& it.hasNext();) {
				resolvedName = it.next().resolve(fromField);
			}
			return resolvedName;
		}

		public String resolve(Method fromMethod) {
			String resolvedName = null;
			for (Iterator<PropertyNameResolver> it = components.iterator(); resolvedName == null
					&& it.hasNext();) {
				resolvedName = it.next().resolve(fromMethod);
			}
			return resolvedName;
		}

	}

	public static class ConventionalBeanPropertyNameResolver implements PropertyNameResolver {

		public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
			return null;
		}

		public String resolve(Field fromField) {
			return fromField.getName();
		}

		public String resolve(Method fromMethod) {
			String name = fromMethod.getName();
			int length = -1;

			if (name.startsWith("get"))
				length = 3;
			else if (name.startsWith("is"))
				length = 2;
			else if (name.startsWith("set"))
				length = 3;

			if (length > -1) {
				return Character.toLowerCase(name.charAt(length)) + name.substring(length + 1);
			} else
				return null;
		}

		public String resolve(int parameterIdx, Method fromMethod) {
			return null;
		}

	}

	/**
	 * JsonProperty resolver based on @JsonProperty annotation. Can be used on fields, methods and
	 * constructor parameters.
	 */
	public static class AnnotationPropertyNameResolver implements PropertyNameResolver {
		public AnnotationPropertyNameResolver() {
		}

		public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
			Annotation[] paramAnns = fromConstructor.getParameterAnnotations()[parameterIdx];
			String name = null;
			for (int j = 0; j < paramAnns.length; j++) {
				if (paramAnns[j] instanceof JsonProperty) {
					name = ((JsonProperty) paramAnns[j]).value();
					break;
				}
			}
			return "".equals(name) ? null : name;
		}

		public String resolve(int parameterIdx, Method fromMethod) {
			Annotation[] anns = fromMethod.getParameterAnnotations()[parameterIdx];
			String name = null;
			for (Annotation ann : anns) {
				if (ann instanceof JsonProperty) {
					name = ((JsonProperty) ann).value();
					break;
				}
			}
			return "".equals(name) ? null : name;
		}

		public String resolve(Field fromField) {
			return getName(fromField);
		}

		public String resolve(Method fromMethod) {
			return getName(fromMethod);
		}

		protected String getName(AnnotatedElement annElement) {
			JsonProperty name = annElement.getAnnotation(JsonProperty.class);
			return name != null && name.value() != null && !name.value().isEmpty() ? name.value()
					: null;
		}
	}
}
