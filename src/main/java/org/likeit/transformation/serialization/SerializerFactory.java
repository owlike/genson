package org.likeit.transformation.serialization;

import java.lang.reflect.Type;

public interface SerializerFactory<S extends Serializer<?>> {
	public S create(Type forType);
}
