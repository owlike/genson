package org.genson;

import org.genson.deserialization.Deserializer;
import org.genson.serialization.Serializer;

/**
 * Converter interface is a shorthand for classes who want to implement both serialization and
 * deserialization. Here is an example of a Converter for URL type.
 * 
 * <pre>
 * Genson genson = new Genson.Builder().with(new Converter&lt;URL&gt;() {
 * 
 * 	&#064;Override
 * 	public void serialize(URL obj, Type type, ObjectWriter writer, Context ctx)
 * 			throws TransformationException, IOException {
 * 		// you don't have to worry about null objects, as the library will handle them.
 * 		writer.writeValue(obj.toExternalForm());
 * 	}
 * 
 * 	&#064;Override
 * 	public URL deserialize(Type type, ObjectReader reader, Context ctx)
 * 			throws TransformationException, IOException {
 * 		return new URL(reader.valueAsString());
 * 	}
 * 
 * }).create();
 * 
 * String serializedUrl = genson.serialize(new URL(&quot;http://www.google.com&quot;));
 * System.out.println(serializedUrl);
 * System.out.println(genson.deserialize(serializedUrl, URL.class));
 * </pre>
 * 
 * As you can see it is quite straightforward to create and register new Converters :).
 * Here is an example dealing with more complex objects.
 * 
 * @author eugen
 * 
 * @param <T>
 */
public interface Converter<T> extends Serializer<T>, Deserializer<T> {

}
