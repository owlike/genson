package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.reflect.ChainedFactory;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

public class RuntimeTypeConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
	public final static ChainedFactory runtimeTypeConverterFactory = new ChainedFactory() {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Converter<?> create(Type type, Genson genson) {
			Converter<?> nextConverter = next().create(type, genson);
			if (nextConverter==null) throw new IllegalArgumentException("RuntimeTypeConverter can not be last Converter in the chain.");
			return new RuntimeTypeConverter(nextConverter);
		}
	};
	private final Converter<T> next;
	
	public RuntimeTypeConverter(Converter<T> next) {
		super(next);
		this.next = next;
	}
	
	@Override
	public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		if (!type.equals(obj.getClass())) ctx.genson.serialize(obj, obj.getClass(), writer, ctx);
		else next.serialize(obj, type, writer, ctx);
	}

	@Override
	public T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		return next.deserialize(type, reader, ctx);
	}

}
