package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import org.genson.Context;
import org.genson.Factory;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptorProvider;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

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
					&& TypeUtil.match(wrappedParameterType, factoryParameter,
							false) && (object = factory.create(withParameterType, genson)) != null) {
				return forClass.cast(object);
			}
		}

		return (T) beanDescriptorProvider.create(TypeUtil.getRawClass(withParameterType), genson);
	}

	private class DelegatedConverter<T> implements Converter<T> {
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
	}
}
