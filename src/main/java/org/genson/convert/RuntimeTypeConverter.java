package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Converter;
import org.genson.Wrapper;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

/**
 * This converter will use the runtime type of objects during serialization.
 * 
 * @author eugen
 * 
 * @param <T> the type this converter is handling.
 */
public class RuntimeTypeConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
	public final static ChainedFactory runtimeTypeConverterFactory = new ChainedFactory() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
			if (nextConverter == null)
				throw new IllegalArgumentException(
						"RuntimeTypeConverter can not be last Converter in the chain.");
			return (Converter<?>) new RuntimeTypeConverter(TypeUtil.getRawClass(type),
					nextConverter);
		}
	};
	private final Class<T> tClass;

	public RuntimeTypeConverter(Class<T> tClass, Converter<T> next) {
		super(next);
		this.tClass = tClass;
	}

	public void serialize(T obj, ObjectWriter writer, Context ctx) throws TransformationException,
			IOException {
		if (!tClass.equals(obj.getClass()))
			ctx.genson.serialize(obj, obj.getClass(), writer, ctx);
		else
			wrapped.serialize(obj, writer, ctx);
	}

	public T deserialize(ObjectReader reader, Context ctx) throws TransformationException,
			IOException {
		return wrapped.deserialize(reader, ctx);
	}

}
