package org.genson.convert;

import java.io.IOException;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectWriter;

/**
 * Genson Serializers work like classic serializers from other libraries.
 * Here is an example of a custom serializer that handles BigIntegers
 * 
 * 
 * @author eugen
 *
 * @param <T> the type of objects this Serializer can serialize.
 */
public interface Serializer<T> {
	public void serialize(T object, ObjectWriter writer, Context ctx) throws TransformationException, IOException;
}
