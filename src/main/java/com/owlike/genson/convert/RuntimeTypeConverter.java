package com.owlike.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.Wrapper;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

/**
 * This converter will use the runtime type of objects during serialization.
 * 
 * @author eugen
 * 
 * @param <T> the type this converter is handling.
 */
public class RuntimeTypeConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
	public static class RuntimeTypeConverterFactory extends ChainedFactory {
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

	public void serialize(T obj, ObjectWriter writer, Context ctx) throws IOException {
		if (obj != null && !tClass.equals(obj.getClass()))
			ctx.genson.serialize(obj, obj.getClass(), writer, ctx);
		else
			wrapped.serialize(obj, writer, ctx);
	}

	public T deserialize(ObjectReader reader, Context ctx) throws IOException {
		return wrapped.deserialize(reader, ctx);
	}

}
