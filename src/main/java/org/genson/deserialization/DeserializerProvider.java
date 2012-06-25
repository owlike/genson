package org.genson.deserialization;

import java.lang.reflect.Type;
import java.util.List;

import org.genson.Factory;
import org.genson.ObjectProvider;


public interface DeserializerProvider {
	
	public <T> Deserializer<T> provide(Type type); 
	
	@SuppressWarnings("unchecked")
	public static class BaseDeserializerProvider extends ObjectProvider<Deserializer<?>> implements DeserializerProvider {
    	public BaseDeserializerProvider(List<Deserializer<?>> objects,
    			List<Factory<? extends Deserializer<?>>> objectFactories,
    			Deserializer<?> dynamicObject) {
    		super(Deserializer.class, objects, objectFactories, dynamicObject);
    	}

		@Override
		public <T> Deserializer<T> provide(Type type) {
			return (Deserializer<T>) findOrCreate(type);
		}
	}
}
