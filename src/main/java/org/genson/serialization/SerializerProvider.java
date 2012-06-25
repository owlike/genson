package org.genson.serialization;

import java.lang.reflect.Type;
import java.util.List;

import org.genson.Factory;
import org.genson.ObjectProvider;

public interface SerializerProvider {
	public <T> Serializer<T> provide(final Type type);
	
	@SuppressWarnings("unchecked")
	public static class BaseSerializerProvider extends ObjectProvider<Serializer<?>> implements SerializerProvider {
		
		public BaseSerializerProvider(List<Serializer<?>> serializers,
				List<Factory<? extends Serializer<?>>> serializersFactories,
				Serializer<?> dynamicSerializer) {
			super(Serializer.class, serializers, serializersFactories, dynamicSerializer);
		}

		@Override
		public <T> Serializer<T> provide(Type type) {
			return (Serializer<T>) findOrCreate(type);
		}
	}
}
