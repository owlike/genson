package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.HandleClassMetadata;
import org.genson.reflect.ChainedFactory;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.genson.stream.ValueType;

public class ClassMetadataConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
	public static class ClassMetadataConverterFactory extends ChainedFactory {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Converter<?> create(Type type, Genson genson) {
			Converter<?> nextObject = next().create(type, genson);
			if (nextObject == null)
				throw new IllegalArgumentException(
						"nextObject must be not null for ClassMetadataConverter, "
								+ "as ClassMetadataConverter can not be the last converter in the chain!");

			if (genson.isWithClassMetadata()
					&& !Wrapper.toAnnotatedElement(nextObject).isAnnotationPresent(HandleClassMetadata.class))
				return new ClassMetadataConverter(nextObject);
			else
				return nextObject;
		}
	}

	private final Converter<T> converter;

	public ClassMetadataConverter(Converter<T> delegate) {
		super(delegate);
		this.converter = delegate;
	}

	@Override
	public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		writer.metadata("class", ctx.genson.aliasFor(obj.getClass()));
		converter.serialize(obj, type, writer, ctx);
	}

	@Override
	public T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		if (ValueType.OBJECT.equals(reader.getValueType())) {
			String className = reader.nextObjectMetadata().metadata("class");
			if (className != null) {
				try {
					Class<?> classFromMetadata = ctx.genson.classFor(className);
					if (!classFromMetadata.equals(type)) {
						Deserializer<T> deser = ctx.genson.provideConverter(classFromMetadata);
						return deser.deserialize(classFromMetadata, reader, ctx);
					}
				} catch (ClassNotFoundException e) {
					throw new TransformationException(
							"Could not use @class metadata, no such class: " + className);
				}
			}
		}
		return converter.deserialize(type, reader, ctx);
	}

}
