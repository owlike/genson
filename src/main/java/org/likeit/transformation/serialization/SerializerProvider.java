package org.likeit.transformation.serialization;

import java.lang.reflect.Type;
import java.util.List;

import org.likeit.transformation.ObjectProvider;
import org.likeit.transformation.TransformationException;

public class SerializerProvider extends ObjectProvider<Serializer<?>, SerializerFactory<? extends Serializer<?>>> {
	
	public SerializerProvider(List<Serializer<?>> serializers,
			List<SerializerFactory<? extends Serializer<?>>> serializersFactories,
			Serializer<?> dynamicSerializer) {
		super(Serializer.class, serializers, serializersFactories, dynamicSerializer);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R> Serializer<R> resolveObject(Type forType)
			throws TransformationException {
		Serializer<R> serializer  = (Serializer<R>) super.resolveObject(forType);
		if ( serializer == null )
			throw new TransformationException("No serializer found for type " + forType);
		return serializer;
	}
}
