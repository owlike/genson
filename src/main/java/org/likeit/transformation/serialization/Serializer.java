package org.likeit.transformation.serialization;

import java.io.IOException;
import java.lang.reflect.Type;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.stream.ObjectWriter;

/*
 *  TODO est-ce que le parametre Type est vraiment utile ... ? 
 *  hum cela permet d'utiliser une type T generique et en fonction du type concret Type 
 *  faire une autre operation
 */
public interface Serializer<T> {
	public void serialize(T obj, Type type, ObjectWriter writer, Context ctx) throws TransformationException, IOException;
}
