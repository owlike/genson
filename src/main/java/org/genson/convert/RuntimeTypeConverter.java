package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.ChainedFactory;
import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

public class RuntimeTypeConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
	public final static ChainedFactory runtimeTypeConverterFactory = new ChainedFactory() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
			if (nextConverter == null)
				throw new IllegalArgumentException(
						"RuntimeTypeConverter can not be last Converter in the chain.");
			return (Converter<?>) new RuntimeTypeConverter(TypeUtil.getRawClass(type), nextConverter);
		}
	};
	private final Converter<T> next;
	private final Class<T> tClass;

	public RuntimeTypeConverter(Class<T> tClass, Converter<T> next) {
		super(next);
		this.tClass = tClass;
		this.next = next;
	}

	@Override
	public void serialize(T obj, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		if (!tClass.equals(obj.getClass()))
			ctx.genson.serialize(obj, obj.getClass(), writer, ctx);
		else
			next.serialize(obj, writer, ctx);
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		return next.deserialize(reader, ctx);
	}

}
