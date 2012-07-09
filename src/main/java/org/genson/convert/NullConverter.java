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

public class NullConverter implements Converter<Object> {
	public static class NullConverterFactory extends ChainedFactory {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Converter<?> create(Type type, Genson genson) {
			Converter<?> nextObject = next().create(type, genson);
			if (!Wrapper.toAnnotatedElement(nextObject).isAnnotationPresent(HandleNull.class))
				return new NullConverterWrapper(genson.getNullConverter(), nextObject);
			else
				return nextObject;
		}
	}
	
	public static class NullConverterWrapper<T> extends Wrapper<Converter<T>> implements Converter<T> {
		private final Converter<Object> nullConverter;
		private final Converter<T> converter;

		public NullConverterWrapper(Converter<Object> nullConverter, Converter<T> converter) {
			super(converter);
			this.nullConverter = nullConverter;
			this.converter = converter;
		}

		@Override
		public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			if (obj == null) {
				nullConverter.serialize(obj, type, writer, ctx);
			} else {
				converter.serialize(obj, type, writer, ctx);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public T deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			if (ValueType.NULL.equals(reader.getValueType()))
				return (T) nullConverter.deserialize(type, reader, ctx);

			return converter.deserialize(type, reader, ctx);
		}
	}
	
	public NullConverter() {
	}

	@Override
	public void serialize(Object obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		writer.writeNull();
	}

	@Override
	public Object deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
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
