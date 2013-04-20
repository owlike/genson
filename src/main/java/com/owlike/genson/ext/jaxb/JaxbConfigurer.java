package com.owlike.genson.ext.jaxb;

import static com.owlike.genson.reflect.TypeUtil.*;

import java.io.IOException;
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
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.TransformationRuntimeException;
import com.owlike.genson.Trilean;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.annotation.WithoutBeanView;
import com.owlike.genson.ext.ExtensionConfigurer;
import com.owlike.genson.internal.ContextualFactory;
import com.owlike.genson.reflect.BeanCreator;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import com.owlike.genson.reflect.BeanProperty;
import com.owlike.genson.reflect.BeanPropertyFactory;
import com.owlike.genson.reflect.PropertyAccessor;
import com.owlike.genson.reflect.PropertyMutator;
import com.owlike.genson.reflect.PropertyNameResolver;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver.BaseResolver;
import com.owlike.genson.reflect.VisibilityFilter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

public class JaxbConfigurer extends ExtensionConfigurer {

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

	@Override
	public void registerConverterFactories(List<Factory<? extends Converter<?>>> factories) {
		factories.add(new EnumConverterFactory());
	}

	@Override
	public void registerContextualFactories(List<ContextualFactory<?>> factories) {
		factories.add(new XmlTypeAdapterFactory());
	}

	private class XmlTypeAdapterFactory implements ContextualFactory<Object> {
		@Override
		public Converter<Object> create(BeanProperty property, Genson genson) {
			XmlJavaTypeAdapter ann = property.getAnnotation(XmlJavaTypeAdapter.class);
			Converter<Object> converter = null;

			if (ann != null) {
				@SuppressWarnings("unchecked")
				Class<? extends XmlAdapter<Object, Object>> adapterClass = (Class<? extends XmlAdapter<Object, Object>>) ann
						.value();
				Type adapterExpandedType = expandType(lookupGenericType(XmlAdapter.class, adapterClass), adapterClass);
				Type adaptedType = typeOf(0, adapterExpandedType);
				Type originalType = typeOf(1, adapterExpandedType);
				
				Class<?> boundClass = getRawClass(property.getType());
				if (boundClass.isPrimitive())
					boundClass = wrap(boundClass);
				
				// checking type consistency
				if (!boundClass.isAssignableFrom(getRawClass(originalType)))
					throw new ClassCastException("The type of the property " + property.getName()
							+ " declared in " + property.getDeclaringClass() + " is not assignable from the BoundType of XmlAdpater " + adapterClass);

				try {
					XmlAdapter<Object, Object> adapter = adapterClass.newInstance();
					// we also need to find a converter for the adapted type
					Converter<Object> adaptedTypeConverter = genson.provideConverter(adaptedType);
					converter = new AdaptedConverter(adapter, adaptedTypeConverter);
				} catch (InstantiationException e) {
					throw new TransformationRuntimeException(
							"Could not instantiate XmlAdapter of type " + adapterClass, e);
				} catch (IllegalAccessException e) {
					throw new TransformationRuntimeException(
							"Could not instantiate XmlAdapter of type " + adapterClass, e);
				}
			}
			return converter;
		}
		
		private class AdaptedConverter implements Converter<Object> {
			private final XmlAdapter<Object, Object> adapter;
			private final Converter<Object> converter;
			
			public AdaptedConverter(XmlAdapter<Object, Object> adapter, Converter<Object> converter) {
				super();
				this.adapter = adapter;
				this.converter = converter;
			}
			
			@Override
			public Object deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				Object value = converter.deserialize(reader, ctx);
				try {
					return adapter.unmarshal(value);
				} catch (Exception e) {
					throw new TransformationException("Could not unmarshal object using adapter " + adapter.getClass());
				}
			}
			
			@Override
			public void serialize(Object object, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				Object adaptedValue = null;
				try {
					adaptedValue = adapter.marshal(object);
				} catch (Exception e) {
					throw new TransformationException("Could not marshal object using adapter " + adapter.getClass());
				}
				converter.serialize(adaptedValue, writer, ctx);
			}
		}
	}

	private class EnumConverterFactory implements Factory<Converter<Enum<?>>> {

		@Override
		public Converter<Enum<?>> create(Type type, Genson genson) {
			Class<?> rawClass = getRawClass(type);
			if (rawClass.isEnum() || Enum.class.isAssignableFrom(rawClass)) {
				@SuppressWarnings({ "unchecked" })
				Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) rawClass;

				try {
					Map<String, Enum<?>> valueToEnum = new HashMap<String, Enum<?>>();
					Map<Enum<?>, String> enumToValue = new HashMap<Enum<?>, String>();
					for (Enum<?> enumConstant : enumClass.getEnumConstants()) {
						XmlEnumValue ann = rawClass.getField(enumConstant.name()).getAnnotation(
								XmlEnumValue.class);

						if (ann != null) {
							valueToEnum.put(ann.value(), enumConstant);
							enumToValue.put(enumConstant, ann.value());
						} else {
							valueToEnum.put(enumConstant.name(), enumConstant);
							enumToValue.put(enumConstant, enumConstant.name());
						}
					}

					return new EnumConverter(valueToEnum, enumToValue);
				} catch (SecurityException e) {
					throw new TransformationRuntimeException("Unable to introspect enum "
							+ enumClass, e);
				} catch (NoSuchFieldException e) {
				}
			}

			// otherwise let genson standard converter handle the conversion
			return null;
		}

		@HandleClassMetadata
		@WithoutBeanView
		private class EnumConverter implements Converter<Enum<?>> {
			private final Map<String, Enum<?>> valueToEnum;
			private final Map<Enum<?>, String> enumToValue;

			public EnumConverter(Map<String, Enum<?>> valueToEnum, Map<Enum<?>, String> enumToValue) {
				super();
				this.valueToEnum = valueToEnum;
				this.enumToValue = enumToValue;
			}

			@Override
			public void serialize(Enum<?> object, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeUnsafeValue(enumToValue.get(object));
			}

			@Override
			public Enum<?> deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return valueToEnum.get(reader.valueAsString());
			}
		}
	}

	private class JaxbBeanPropertyFactory implements BeanPropertyFactory {

		@Override
		public PropertyAccessor createAccessor(String name, Field field, Type ofType, Genson genson) {
			Type newType = getType(field, field.getGenericType(), ofType);
			if (newType != null) {
				return new PropertyAccessor.FieldAccessor(name, field, newType, getRawClass(ofType));
			}

			return null;
		}

		@Override
		public PropertyAccessor createAccessor(String name, Method method, Type ofType,
				Genson genson) {
			Type newType = getType(method, method.getReturnType(), ofType);
			if (newType != null) {
				return new PropertyAccessor.MethodAccessor(name, method, newType,
						getRawClass(ofType));
			}
			return null;
		}

		@Override
		public PropertyMutator createMutator(String name, Field field, Type ofType, Genson genson) {
			Type newType = getType(field, field.getGenericType(), ofType);
			if (newType != null) {
				return new PropertyMutator.FieldMutator(name, field, newType, getRawClass(ofType));
			}

			return null;
		}

		@Override
		public PropertyMutator createMutator(String name, Method method, Type ofType, Genson genson) {
			if (method.getParameterTypes().length == 1) {
				Type newType = getType(method, method.getReturnType(), ofType);
				if (newType != null) {
					return new PropertyMutator.MethodMutator(name, method, newType,
							getRawClass(ofType));
				}
			}
			return null;
		}

		@Override
		public BeanCreator createCreator(Type ofType, Constructor<?> ctr, String[] resolvedNames,
				Genson genson) {
			return null;
		}

		@Override
		public BeanCreator createCreator(Type ofType, Method method, String[] resolvedNames,
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
