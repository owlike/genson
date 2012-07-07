package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.reflect.ChainedFactory;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

public class CircularClassReferenceConverterFactory extends ChainedFactory {
	private final static class CircularConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
		protected CircularConverter() {
			super();
		}

		private Converter<T> delegate;
		@Override
		public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			delegate.serialize(obj, type, writer, ctx);
		}

		@Override
		public T deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return delegate.deserialize(type, reader, ctx);
		}
		
		void setDelegateConverter(Converter<T> delegate) {
			this.delegate = delegate;
			wrap(delegate);
		}
	}
	
	private final static ThreadLocal<Map<Type, CircularConverter<?>>> _circularConverters = new ThreadLocal<Map<Type, CircularConverter<?>>>() {
		protected Map<Type, CircularConverter<?>> initialValue() {
			return new HashMap<Type, CircularConverter<?>>();
		};
	};
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Converter<?> create(Type type, Genson genson) {
    	if (_circularConverters.get().containsKey(type)) {
    		return _circularConverters.get().get(type);
    	} else {
    		try {
        		CircularConverter circularConverter = new CircularConverter();
        		_circularConverters.get().put(type, circularConverter);
        		Converter converter = next().create(type, genson);
        		circularConverter.setDelegateConverter(converter);
        		return converter;
    		} finally {
    			_circularConverters.get().remove(type);
    		}
    	}
	}
}
