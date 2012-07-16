package org.genson.convert;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import org.genson.Context;
import org.genson.Factory;
import org.genson.Genson;
import org.genson.Operations;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptorProvider;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

/**
 * This is the base factory that will create converters based on the default ones and on custom
 * Serializer, Deserializer and Converter. But it also uses factories (default and custom) and
 * {@link org.genson.reflect.BeanDescriptorProvider BeanDescriptorProvider} that is responsible of
 * creating bean converters.
 * <p>
 * When you ask for a Converter it will
 * <ul>
 * <li>Search for a Serializer {@link BasicConvertersFactory#provide(Class, Type, Genson)
 * provide(Serializer.class, type, genson)}.
 * <ol>
 * <li>Lookup in the registered Serializers for one that is parameterized with the current type or
 * one of its supertypes, if found we finished (it takes the first one, so the order is very
 * important).</li>
 * <li>Else we will try the factories by searching the ones that can create and instance of
 * Serializer&lt;CurrentType&gt; (again the order is very important). We continue while they return
 * null.</li>
 * <li>If no factory could create an instance we will use BeanDescriptorProvider.</li>
 * </ol>
 * </li>
 * <li>If the returned serializer is an instance of Converter we finished.</li>
 * <li>Else it will search for a Deserializer (same mechanism as for Serializer). If the
 * deserializer is a converter we finished.</li>
 * <li>Else we will wrap both into a {@link DelegatedConverter} instance.</li>
 * </ul>
 * 
 * Note that the create method from the registered factories will only be called if the type with
 * which they are parameterized is assignable from the current type. For example, if we look for a
 * serializer of Integer then Factory<Converter<Integer>> and Factory<Serializer<Object>> match
 * both, the first registered will be used.
 * 
 * @author eugen
 * 
 */
public class BasicConvertersFactory implements Factory<Converter<?>> {
	private final List<? super Converter<?>> converters;
	private final List<Factory<?>> factories;
	private final BeanDescriptorProvider beanDescriptorProvider;

	public BasicConvertersFactory(List<? super Converter<?>> converters,
			List<Factory<?>> factories, BeanDescriptorProvider beanDescriptorProvider) {
		this.converters = converters;
		this.factories = factories;
		this.beanDescriptorProvider = beanDescriptorProvider;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Converter<?> create(Type type, Genson genson) {
		Converter<?> converter = null;
		Serializer<?> serializer = provide(Serializer.class, type, genson);
		if (serializer instanceof Converter)
			converter = (Converter<?>) serializer;
		else {
			Deserializer<?> deserializer = provide(Deserializer.class, type, genson);
			if (deserializer instanceof Converter)
				converter = (Converter<?>) deserializer;
			else {
				if (serializer != null || deserializer != null)
					converter = new DelegatedConverter(serializer, deserializer);
			}
		}

		return converter;
	}

	/**
	 * Will provide an instance of forClass parameterized with withParameterType.
	 * @param forClass is meant to be Serializer, Deserializer or Converter.
	 * @param withParameterType the parameterized of forClass
	 * @param genson
	 * @return should never return null
	 */
	@SuppressWarnings("unchecked")
	protected <T> T provide(Class<T> forClass, Type withParameterType, Genson genson) {
		for (Object s : converters) {
			if (TypeUtil.lookupWithGenerics(forClass, withParameterType, s.getClass(), false) != null) {
				return forClass.cast(s);
			}
		}

		Type wrappedParameterType = withParameterType;
		if (withParameterType instanceof Class<?> && ((Class<?>) withParameterType).isPrimitive())
			wrappedParameterType = TypeUtil.wrap((Class<?>) withParameterType);

		for (Iterator<Factory<?>> it = factories.iterator(); it.hasNext();) {
			Factory<?> factory = it.next();
			Object object = null;
			Type factoryType = TypeUtil.lookupGenericType(Factory.class, factory.getClass());
			factoryType = TypeUtil.expandType(factoryType, factory.getClass());
			// it is a parameterized type and we want the parameter corresponding to Serializer from
			// Factory<Serializer<?>>
			factoryType = TypeUtil.typeOf(0, factoryType);
			Type factoryParameter = TypeUtil.typeOf(0, factoryType);
			if (forClass.isAssignableFrom(TypeUtil.getRawClass(factoryType))
					&& TypeUtil.match(wrappedParameterType, factoryParameter, false)
					&& (object = factory.create(withParameterType, genson)) != null) {
				return forClass.cast(object);
			}
		}

		return (T) beanDescriptorProvider.create(TypeUtil.getRawClass(withParameterType), genson);
	}

	private class DelegatedConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
		private final Serializer<T> serializer;
		private final Deserializer<T> deserializer;

		public DelegatedConverter(Serializer<T> serializer, Deserializer<T> deserializer) {
			this.serializer = serializer;
			this.deserializer = deserializer;
		}

		@Override
		public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			serializer.serialize(obj, type, writer, ctx);
		}

		@Override
		public T deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return deserializer.deserialize(type, reader, ctx);
		}

		@Override
		public <A extends Annotation> A getAnnotation(Class<A> aClass) {
			A a = null;
			if (serializer != null)
				a = toAnnotatedElement(serializer).getAnnotation(aClass);
			if (deserializer != null && a == null)
				a = toAnnotatedElement(deserializer).getAnnotation(aClass);
			return a;
		}

		@Override
		public Annotation[] getAnnotations() {
			if (serializer != null && deserializer != null)
				return Operations.union(Annotation[].class, toAnnotatedElement(serializer)
						.getAnnotations(), toAnnotatedElement(deserializer).getAnnotations());
			if (serializer != null)
				return toAnnotatedElement(serializer).getAnnotations();
			if (deserializer != null)
				return toAnnotatedElement(deserializer).getAnnotations();

			return new Annotation[0];
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			if (serializer != null && deserializer != null)
				return Operations.union(Annotation[].class, toAnnotatedElement(serializer)
						.getDeclaredAnnotations(), toAnnotatedElement(deserializer)
						.getDeclaredAnnotations());
			if (serializer != null)
				return toAnnotatedElement(serializer).getDeclaredAnnotations();
			if (deserializer != null)
				return toAnnotatedElement(deserializer).getDeclaredAnnotations();

			return new Annotation[0];
		}

		@Override
		public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
			if (serializer != null)
				return toAnnotatedElement(serializer).isAnnotationPresent(annotationClass);
			if (deserializer != null)
				return toAnnotatedElement(deserializer).isAnnotationPresent(annotationClass);
			return false;
		}
	}
}
