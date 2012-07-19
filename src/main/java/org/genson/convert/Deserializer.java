package org.genson.convert;

import java.io.IOException;

import org.genson.Context;
import org.genson.TransformationException;
import org.genson.stream.ObjectReader;

/**
 * Genson Deserializers work like classic deserializers from other libraries.
 * 
 * @see Converter
 * 
 * @author eugen
 * 
 * @param <T> the type of objects this deserializer can deserialize.
 */
public interface Deserializer<T> {
	/**
	 * 
	 * @param reader used to read data from.
	 * @param ctx the current context.
	 * @return an instance of T or a subclass of T.
	 * @throws TransformationException
	 * @throws IOException
	 */
	public T deserialize(ObjectReader reader, Context ctx) throws TransformationException,
			IOException;
}
