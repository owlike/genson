package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.HandleNull;
import org.genson.reflect.ChainedFactory;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.genson.stream.ValueType;

//TODO handle exceptions in addition!! would be nice
public class NullConverter<T> implements Converter<T> {
	public static class NullConverterFactory extends ChainedFactory {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Converter<?> create(Type type, Genson genson) {
			Converter<?> nextObject = next().create(type, genson);
			if (nextObject == null)
				throw new IllegalArgumentException(
						"nextObject must be not null for NullConverter! " +
						"NullConverter can not be the last converter in the chain!");

			if (!nextObject.getClass().isAnnotationPresent(HandleNull.class))
				return new NullConverter(nextObject);
			else
				return nextObject;
		}
	}

	private final Converter<T> converter;

	private NullConverter(Converter<T> converter) {
		this.converter = converter;
	}

	@Override
	// TODO class metadata will not work!
	public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		if (obj == null) {
			writer.writeNull();
		} else {
			converter.serialize(obj, type, writer, ctx);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		if (ValueType.NULL.equals(reader.getValueType()))
			return (T) handleNull(type);

		return converter.deserialize(type, reader, ctx);
	}

	protected Object handleNull(Type type) {
		if (!(type instanceof Class))
			return null;
		Class<?> clazz = (Class<?>) type;
		if (clazz.isPrimitive()) {
			if (clazz == int.class)
				return 0;
			if (clazz == double.class)
				return 0d;
			if (clazz == boolean.class)
				return false;
			if (clazz == long.class)
				return 0l;
			if (clazz == float.class)
				return 0f;
			if (clazz == short.class)
				return 0;
		}
		// its an object
		return null;
	}
}
