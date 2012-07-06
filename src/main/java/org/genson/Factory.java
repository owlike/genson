package org.genson;

import java.lang.reflect.Type;

/**
 * Factory interface implemented by classes who can act as factories and create instances of objects
 * of type T and its subclasses. Implementations will be used as Converter, Serializer and
 * Deserializer factories. So the type T will be something like Converter&lt;Integer&gt; but the
 * type argument of method create will correspond to Integer.
 * 
 * You can have a look at factories from {@link org.genson.convert.DefaultConverters
 * DefaultConverters}.
 * 
 * @author eugen
 * 
 * @param <T> the base type of the objects this factory can create.
 */
public interface Factory<T> {
	/**
	 * Implementations of this method must try to create an instance of type T based on the
	 * parameter "type". If this factory can not create an object of type T for parameter type then
	 * it must return null. IMPORTANT: You should always check if it is the expected type.
	 * 
	 * @param type used to build an instance of T.
	 * @return null if it doesn't support this type or an instance of T (or a subclass).
	 */
	public T create(Type type, Genson genson);
}
