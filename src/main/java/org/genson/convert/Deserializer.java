package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectReader;


public interface Deserializer<T> {
	public T deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException;
}
