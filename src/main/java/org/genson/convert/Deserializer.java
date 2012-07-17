package org.genson.convert;

import java.io.IOException;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectReader;


public interface Deserializer<T> {
	public T deserialize(ObjectReader reader, Context ctx) throws TransformationException, IOException;
}
