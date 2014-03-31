package com.owlike.genson;

import java.lang.reflect.Type;

/**
 * Factory interface must be implemented by classes who want to act as factories and create
 * instances of Converter/Serializer/Deserializer. Implementations will be used as Converter,
 * Serializer and Deserializer factories. So the type T will be something like
 * Converter&lt;Integer&gt; but the type argument of method create will correspond to Integer <u>or
 * a subclass of Integer</u>.
 * 
 * As an example you can have a look at factories from {@link com.owlike.genson.convert.DefaultConverters
 * DefaultConverters}. Here is an example with a custom converter and factory for enums.
 * 
 * <pre>
 * public static class EnumConverter&lt;T extends Enum&lt;T&gt;&gt; implements Converter&lt;T&gt; {
 * 	private final Class&lt;T&gt; eClass;
 * 
 * 	public EnumConverter(Class&lt;T&gt; eClass) {
 * 		this.eClass = eClass;
 * 	}
 * 
 * 	&#064;Override
 * 	public void serialize(T obj, ObjectWriter writer, Context ctx) {
 * 		writer.writeUnsafeValue(obj.name());
 * 	}
 * 
 * 	&#064;Override
 * 	public T deserialize(ObjectReader reader, Context ctx) {
 * 		return Enum.valueOf(eClass, reader.valueAsString());
 * 	}
 * }
 * 
 * public final static class EnumConverterFactory implements Factory&lt;Converter&lt;? extends Enum&lt;?&gt;&gt;&gt; {
 * 	public final static EnumConverterFactory instance = new EnumConverterFactory();
 * 
 * 	private EnumConverterFactory() {
 * 	}
 * 
 * 	&#064;SuppressWarnings({ &quot;rawtypes&quot;, &quot;unchecked&quot; })
 * 	&#064;Override
 * 	public Converter&lt;Enum&lt;?&gt;&gt; create(Type type, Genson genson) {
 * 		Class&lt;?&gt; rawClass = TypeUtil.getRawClass(type);
 * 		return rawClass.isEnum() || Enum.class.isAssignableFrom(rawClass) ? new EnumConverter(
 * 				rawClass) : null;
 * 	}
 * };
 * </pre>
 * 
 * Note the use of {@link com.owlike.genson.reflect.TypeUtil TypeUtil} class that provides operations to
 * work with generic types. However this class might change in the future, in order to provide a better API.
 * 
 * @see com.owlike.genson.Converter
 * @see com.owlike.genson.convert.ChainedFactory ChainedFactory
 * @see com.owlike.genson.Serializer
 * @see com.owlike.genson.Deserializer
 * 
 * @author eugen
 * 
 * @param <T> the base type of the objects this factory can create. T can be of type Converter,
 *        Serializer or Deserializer.
 */
public interface Factory<T> {
	/**
	 * Implementations of this method must try to create an instance of type T based on the
	 * parameter "type". If this factory can not create an object of type T for parameter type then
	 * it must return null.
	 * 
	 * @param type used to build an instance of T.
	 * @return null if it doesn't support this type or an instance of T (or a subclass).
	 */
	public T create(Type type, Genson genson);
}
