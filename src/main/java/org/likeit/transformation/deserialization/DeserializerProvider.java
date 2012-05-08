package org.likeit.transformation.deserialization;

import java.lang.reflect.Type;

import org.likeit.transformation.TransformationException;

public class DeserializerProvider {
	public <T> Deserializer<T> resolveDeserializer(Type forType) throws TransformationException {
		return null;
	}
}
