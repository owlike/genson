package org.genson;

import java.lang.reflect.Type;

/**
 * Factory interface implemented by classes who can act as factories
 * and create instances of objects of type T. 
 * 
 * You can have a look at factories from {@link org.genson.serialization.DefaultSerializers DefaultSerializers}.
 * 
 * @author eugen
 *
 * @param <T> the type of the objects this factory can create.
 */
public interface Factory<T> {
	/**
	 * Implementations of this method must try to create an instance of type T based
	 * on the parameter "type". If this factory can not create an object of type T for
	 * parameter type then it must return null. 
	 * IMPORTANT: type is not necessary of type T or a subclass, so you should always check if 
	 * you can really create an instance of T for that type.
	 * @param type used to build an instance of T.
	 * @return null if it doesn't support this type or an instance of T (or a subclass).
	 */
	public T create(Type type);
}
