package org.genson.convert;

import java.io.IOException;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectWriter;

/**
 * Genson Serializers work like classic serializers from other libraries.
 * 
 * @see Converter
 * @author eugen
 * 
 * @param <T> the type of objects this Serializer can serialize.
 */
public interface Serializer<T> {
	/**
	 * @param object we want to serialize. The object is of type T or a subclass (if this serializer
	 *        has been registered for subclasses).
	 * @param writer to use to write data to the output stream.
	 * @param ctx the current context.
	 * @throws TransformationException
	 * @throws IOException
	 */
	public void serialize(T object, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException;
}
