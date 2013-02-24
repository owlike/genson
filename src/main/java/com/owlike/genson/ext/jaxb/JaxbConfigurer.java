package com.owlike.genson.ext.jaxb;

import static com.owlike.genson.reflect.TypeUtil.getRawClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.owlike.genson.Genson;
import com.owlike.genson.Trilean;
import com.owlike.genson.ext.ExtensionConfigurer;
import com.owlike.genson.reflect.BeanCreator;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import com.owlike.genson.reflect.BeanPropertyFactory;
import com.owlike.genson.reflect.PropertyAccessor;
import com.owlike.genson.reflect.PropertyMutator;
import com.owlike.genson.reflect.PropertyNameResolver;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver.BaseResolver;
import com.owlike.genson.reflect.VisibilityFilter;

public class JaxbConfigurer extends ExtensionConfigurer {
	private final Map<Class<? extends XmlAdapter<?, ?>>, XmlAdapter<?, ?>> _xmlAdaptersCache = new HashMap<Class<? extends XmlAdapter<?, ?>>, XmlAdapter<?, ?>>();

	@Override
	public void registerBeanMutatorAccessorResolvers(List<BeanMutatorAccessorResolver> resolvers) {
		resolvers.add(new JaxbAnnotationsResolver());
	}

	@Override
	public void registerPropertyNameResolvers(List<PropertyNameResolver> resolvers) {
		resolvers.add(new JaxbNameResolver());
	}

	@Override
	public void registerBeanPropertyFactories(List<BeanPropertyFactory> factories) {
		factories.add(0, new JaxbBeanPropertyFactory());
	}

	private class JaxbBeanPropertyFactory implements BeanPropertyFactory {

		@Override
		public <T> PropertyAccessor<T, ?> createAccessor(String name, Field field, Type ofType,
				Genson genson) {
			Type newType = getType(field, field.getGenericType(), ofType);
			if (newType != null) {
				@SuppressWarnings("unchecked")
				Class<T> ofClass = (Class<T>) getRawClass(ofType);
				Type expandedType = TypeUtil.expandType(newType, ofType);
				return new PropertyAccessor.FieldAccessor<T, Object>(name, field, expandedType,
						ofClass, genson.provideConverter(expandedType));
			}

			return null;
		}

		@Override
		public <T> PropertyAccessor<T, ?> createAccessor(String name, Method method, Type ofType,
				Genson genson) {
			Type newType = getType(method, method.getReturnType(), ofType);
			if (newType != null) {
				@SuppressWarnings("unchecked")
				Class<T> ofClass = (Class<T>) getRawClass(ofType);
				Type expandedType = TypeUtil.expandType(newType, ofType);
				return new PropertyAccessor.MethodAccessor<T, Object>(name, method, expandedType,
						ofClass, genson.provideConverter(expandedType));
			}
			return null;
		}

		@Override
		public <T> PropertyMutator<T, ?> createMutator(String name, Field field, Type ofType,
				Genson genson) {
			Type newType = getType(field, field.getGenericType(), ofType);
			if (newType != null) {
				@SuppressWarnings("unchecked")
				Class<T> ofClass = (Class<T>) getRawClass(ofType);
				Type expandedType = TypeUtil.expandType(newType, ofType);
				return new PropertyMutator.FieldMutator<T, Object>(name, field, expandedType,
						ofClass, genson.provideConverter(expandedType));
			}

			return null;
		}

		@Override
		public <T> PropertyMutator<T, ?> createMutator(String name, Method method, Type ofType,
				Genson genson) {
			if (method.getParameterTypes().length == 1) {
				Type newType = getType(method, method.getReturnType(), ofType);
				if (newType != null) {
					@SuppressWarnings("unchecked")
					Class<T> ofClass = (Class<T>) getRawClass(ofType);
					Type expandedType = TypeUtil.expandType(newType, ofType);
					return new PropertyMutator.MethodMutator<T, Object>(name, method, expandedType,
							ofClass, genson.provideConverter(expandedType));
				}
			}
			return null;
		}

		@Override
		public <T> BeanCreator<T> createCreator(Type ofType, Constructor<T> ctr,
				String[] resolvedNames, Genson genson) {
			return null;
		}

		@Override
		public <T> BeanCreator<T> createCreator(Type ofType, Method method, String[] resolvedNames,
				Genson genson) {
			return null;
		}

		private Type getType(AccessibleObject object, Type objectType, Type contextType) {
			XmlElement el = object.getAnnotation(XmlElement.class);
			if (el != null && el.type() != XmlElement.DEFAULT.class) {
				if (!TypeUtil.getRawClass(objectType).isAssignableFrom(el.type()))
					throw new ClassCastException("Inavlid XmlElement annotation, " + objectType
							+ " is not assignable from " + el.type());
				return el.type();
			} else
				return null;
		}
	}

	private class JaxbNameResolver implements PropertyNameResolver {
		private final static String DEFAULT_NAME = "##default";

		@Override
		public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
			return null;
		}

		@Override
		public String resolve(int parameterIdx, Method fromMethod) {
			return null;
		}

		@Override
		public String resolve(Field fromField) {
			return extractName(fromField);
		}

		@Override
		public String resolve(Method fromMethod) {
			return extractName(fromMethod);
		}

		private String extractName(AccessibleObject object) {
			String name = null;
			XmlAttribute attr = object.getAnnotation(XmlAttribute.class);
			if (attr != null)
				name = attr.name();
			else {
				XmlElement el = object.getAnnotation(XmlElement.class);
				if (el != null) name = el.name();
			}
			return DEFAULT_NAME.equals(name) ? null : name;
		}
	}

	private class JaxbAnnotationsResolver extends BaseResolver {
		@Override
		public Trilean isAccessor(Field field, Class<?> fromClass) {

			if (ignore(field, field.getType(), fromClass)) return Trilean.FALSE;
			if (include(field, field.getType(), fromClass)) return Trilean.TRUE;
			return analyzeAccessTypeInfo(field, field, XmlAccessType.FIELD, fromClass);
		}

		@Override
		public Trilean isMutator(Field field, Class<?> fromClass) {
			if (ignore(field, field.getType(), fromClass)) return Trilean.FALSE;
			if (include(field, field.getType(), fromClass)) return Trilean.TRUE;
			return analyzeAccessTypeInfo(field, field, XmlAccessType.FIELD, fromClass);
		}

		@Override
		public Trilean isAccessor(Method method, Class<?> fromClass) {
			if (ignore(method, method.getReturnType(), fromClass)) return Trilean.FALSE;

			String name = null;
			if (method.getName().startsWith("get") && method.getName().length() > 3)
				name = method.getName().substring(3);
			else if (method.getName().startsWith("is") && method.getName().length() > 2
					&& method.getReturnType() == boolean.class
					|| method.getReturnType() == Boolean.class)
				name = method.getName().substring(2);

			if (name != null) {
				if (include(method, method.getReturnType(), fromClass)) return Trilean.TRUE;
				if (find(XmlTransient.class, fromClass, "set" + name, method.getReturnType()) != null)
					return Trilean.FALSE;
			}

			return analyzeAccessTypeInfo(method, method, XmlAccessType.PROPERTY, fromClass);
		}

		@Override
		public Trilean isMutator(Method method, Class<?> fromClass) {
			Class<?> paramClass = method.getParameterTypes().length == 1 ? method
					.getParameterTypes()[0] : Object.class;
			if (ignore(method, paramClass, fromClass)) return Trilean.FALSE;

			if (method.getName().startsWith("set") && method.getName().length() > 3) {
				if (include(method, method.getReturnType(), fromClass)) return Trilean.TRUE;

				String name = method.getName().substring(3);
				if (find(XmlTransient.class, fromClass, "get" + name) != null)
					return Trilean.FALSE;
				if (paramClass.equals(boolean.class) || paramClass.equals(Boolean.class)) {
					if (find(XmlTransient.class, fromClass, "is" + name) != null)
						return Trilean.FALSE;
				}
			}

			return analyzeAccessTypeInfo(method, method, XmlAccessType.PROPERTY, fromClass);
		}

		public Trilean analyzeAccessTypeInfo(AccessibleObject property, Member member,
				XmlAccessType accessType, Class<?> fromClass) {
			XmlAccessorType xmlAccessTypeAnn = find(XmlAccessorType.class, property, fromClass);

			if (xmlAccessTypeAnn != null) {
				if (xmlAccessTypeAnn.value() == accessType
						&& VisibilityFilter.DEFAULT.isVisible(member)) return Trilean.TRUE;
				if (xmlAccessTypeAnn.value() != accessType
						&& xmlAccessTypeAnn.value() != XmlAccessType.PUBLIC_MEMBER)
					return Trilean.FALSE;
			}

			return Trilean.UNKNOWN;
		}

		private boolean ignore(AccessibleObject property, Class<?> ofType, Class<?> fromClass) {
			XmlTransient xmlTransientAnn = find(XmlTransient.class, property, ofType);
			if (xmlTransientAnn != null) return true;

			return false;
		}

		private boolean include(AccessibleObject property, Class<?> ofType, Class<?> fromClass) {
			if (find(XmlAttribute.class, property, ofType) != null
					|| find(XmlElement.class, property, ofType) != null) return true;

			return false;
		}
	}

	private <A extends Annotation> A find(Class<A> annotation, AccessibleObject onObject,
			Class<?> onClass) {
		A ann = onObject.getAnnotation(annotation);
		if (ann != null) return ann;
		return find(annotation, onClass);
	}

	private <A extends Annotation> A find(Class<A> annotation, Class<?> onClass) {
		A ann = onClass.getAnnotation(annotation);
		if (ann == null && onClass.getPackage() != null)
			ann = onClass.getPackage().getAnnotation(annotation);
		return ann;
	}

	private <A extends Annotation> A find(Class<A> annotation, Class<?> inClass, String methodName,
			Class<?>... parameterTypes) {
		A ann = null;
		for (Class<?> clazz = inClass; clazz != null; clazz = clazz.getSuperclass()) {
			try {
				for (Method m : clazz.getDeclaredMethods())
					if (m.getName().equals(methodName)
							&& Arrays.equals(m.getParameterTypes(), parameterTypes))
						if (m.isAnnotationPresent(annotation))
							return m.getAnnotation(annotation);
						else
							break;

			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return ann;
	}
}
