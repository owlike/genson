package org.likeit.transformation.deserialization;

import java.lang.reflect.Type;
import java.util.List;

import org.likeit.transformation.ObjectProvider;
import org.likeit.transformation.TransformationException;

public class DeserializerProvider extends ObjectProvider<Deserializer<?>, DeserializerFactory<? extends Deserializer<?>>> {
	
	public DeserializerProvider(List<Deserializer<?>> objects,
			List<DeserializerFactory<? extends Deserializer<?>>> objectFactories,
			Deserializer<?> dynamicObject) {
		super(Deserializer.class, objects, objectFactories, dynamicObject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> Deserializer<R> resolveObject(Type forType)
			throws TransformationException {
		Deserializer<R> deserializer  = (Deserializer<R>) super.resolveObject(forType);
		if ( deserializer == null )
			throw new TransformationException("No deserializer found for type " + forType);
		return deserializer;
	}
}
