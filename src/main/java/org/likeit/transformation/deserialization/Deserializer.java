package org.likeit.transformation.deserialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.stream.ObjectReader;

public interface Deserializer<T> {
	public T deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException;
}
