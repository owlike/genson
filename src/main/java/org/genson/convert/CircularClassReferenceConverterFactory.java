package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.genson.Context;
import org.genson.Converter;
import org.genson.Wrapper;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

/**
 * ChainedFactory that handles circular class references.
 * 
 * @author eugen
 *
 */
public class CircularClassReferenceConverterFactory extends ChainedFactory {
	private final static class CircularConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {
		protected CircularConverter() {
			super();
		}
		
		@Override
		public void serialize(T obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			wrapped.serialize(obj, writer, ctx);
		}

		@Override
		public T deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return wrapped.deserialize(reader, ctx);
		}
		
		void setDelegateConverter(Converter<T> delegate) {
			decorate(delegate);
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

	@Override
	protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
		throw new UnsupportedOperationException();
	}
}
