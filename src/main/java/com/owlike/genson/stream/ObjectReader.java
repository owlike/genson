package com.owlike.genson.stream;

import java.io.Closeable;
import java.io.IOException;

/**
 * ObjectReader is part of the streaming api, it's implementations allow you to read data from the
 * stream. The root of the input should always be a object, an array or a literal. There may be some
 * differences between implementations if they try to be compliant with their format specification.
 * 
 * <ul>
 * <li>To read an array call {@link #beginArray()} then use {@link #hasNext()} to check if there is
 * a next element and then call {@link #next()} to advance. When you call next in an array it will
 * read the next value and return its type {@link ValueType}. Use it to check values type and to
 * retrieve its value (if it is a literal) use one of valueAsXXX methods, otherwise it is an array
 * or object. When hasNext returns false terminate the array by calling {@link #endArray()}.
 * <li>To read a object call {@link #beginObject()} then use {@link #hasNext()} to check if there is
 * a next property and then call {@link #next()} to read the name/value pair and {@link #name()} to
 * retrieve its name. If the value is a literal retrieve its value with valueAsXXX methods,
 * otherwise it is an array or object. When you finished reading all properties call
 * {@link #endObject()}.
 * <li>Objects can also contain metadata as their first properties. To read object metadata you have
 * two options:
 * <ol>
 * <li>Just begin your object with beginObject and then retrieve the metadata that you want with
 * {@link #metadata(String) metadata(nameOfTheMetadataProperty)}.
 * <li>Use {@link #nextObjectMetadata()} to read the next objects metadata without calling
 * beginObject. This is useful when you want to handle some metadata in a converter and then
 * delegate the rest to another converter (that will call beginObject or again nextObjectMetadata,
 * so for him it will be transparent that you retrieved already some metadata and he will still be
 * able to retrieve the same data).
 * </ol>
 * <li>To read a literal use valueAsXXX methods. Actual implementation allows literals as root and
 * is relatively tolerant to wrong types, for example if the stream contains the string "123" but
 * you want to retrieve it as a int, {@link JsonReader#valueAsInt()} will parse it and return 123.
 * It does also conversion between numeric types (double <-> int etc).
 * <li>To skip a value use {@link #skipValue()}. If the value is an object or an array it will skip
 * all its content.
 * </ul>
 * 
 * Here is an example if you want to use directly the streaming api instead of the databind api (or
 * if you write a custom converter or deserializer).
 * 
 * <pre>
 * public static void main(String[] args) {
 * 	// we will read from json to Person
 * 	Person.read(new JsonReader(&quot;{\&quot;name\&quot;:\&quot;eugen\&quot;,\&quot;age\&quot;:26, \&quot;childrenYearOfBirth\&quot;:[]}&quot;));
 * }
 * 
 * class Person {
 * 	String name;
 * 	int age;
 * 	List&lt;Integer&gt; childrenYearOfBirth;
 * 
 * 	public static Person read(ObjectReader reader) {
 * 		Person p = new Person();
 * 		for (; reader.hasNext();) {
 * 			if (&quot;name&quot;.equals(reader.name()))
 * 				p.name = reader.valueAsString();
 * 			else if (&quot;age&quot;.equals(reader.name()))
 * 				p.age = reader.valueAsInt();
 * 			else if (&quot;childrenYearOfBirth&quot;.equals(reader.name())) {
 * 				if (reader.getValueType() == TypeValue.NULL)
 * 					p.childrenYearOfBirth = null;
 * 				else {
 * 					reader.beginArray();
 * 					p.childrenYearOfBirth = new ArrayList&lt;Integer&gt;();
 * 					for (int i = 0; reader.hasNext(); i++)
 * 						p.childrenYearOfBirth.add(reader.valueAsInt());
 * 					reader.endArray();
 * 				}
 * 			}
 * 		}
 * 		return p;
 * 	}
 * }
 * </pre>
 * 
 * @see ValueType
 * @see JsonReader
 * @see ObjectWriter
 * @see JsonWriter
 * 
 * @author eugen
 * 
 */
public interface ObjectReader extends Closeable {
	/**
	 * Starts reading a object. Objects contain name/value pairs. Call {@link #endObject()} when the
	 * objects contains no more properties.
	 * 
	 * @return a reference to the reader.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ObjectReader beginObject() throws IOException;

	/**
	 * Ends the object. If you were not in an object or the object contains more data, an exception
	 * will be thrown.
	 * 
	 * @return a reference to the reader.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ObjectReader endObject() throws IOException;

	/**
	 * Starts reading an array. Arrays contain only values. Call {@link #endArray()} when the array
	 * contains no more values.
	 * 
	 * @return a reference to the reader.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ObjectReader beginArray() throws IOException;

	/**
	 * Ends the array. If you were not in an array or the array contains more data, an exception
	 * will be thrown.
	 * 
	 * @return a reference to the reader.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ObjectReader endArray() throws IOException;

	/**
	 * Will read nexts object metadata. You can call this method as many times as you want, with the
	 * condition that you use only {@link #metadata(String)} method. For example if you call
	 * {@link #beginObject()} you wont be able to do it anymore (however you still can retrieve the
	 * metadata!).
	 * 
	 * @return a reference to the reader.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ObjectReader nextObjectMetadata() throws IOException;

	/**
	 * If we are in a object it will read the next name/value pair and if we are in an array it will
	 * read the next value (except if value is of complex type, in that case after the call to
	 * next() you must use one of beginXXX methods).
	 * 
	 * @return the type of the value, see {@link ValueType} for possible types.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ValueType next() throws IOException;

	/**
	 * 
	 * @return true if there is a next property or value, false otherwise.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public boolean hasNext() throws IOException;

	/**
	 * If the value is of complex type it will skip its content.
	 * 
	 * @return a reference to the reader.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public ObjectReader skipValue() throws IOException;

	/**
	 * @return The type of current value.
	 * @see ValueType
	 */
	public ValueType getValueType();

	/**
	 * 
	 * @param name the name of the metadata to retrieve.
	 * @return value of metadata with name as key or null if there is no such metadata.
	 * @throws IOException
	 * @throws JsonStreamException
	 */
	public String metadata(String name) throws IOException;

	/**
	 * @return the name of current property, valid only if we are in a object and you called
	 *         {@link #next()} before.
	 * @throws IOException
	 */
	public String name() throws IOException;

	/**
	 * 
	 * @return the current value as a String. It will try to convert the actual value to String if
	 *         its not of that type.
	 * @throws IOException
	 */
	public String valueAsString() throws IOException;

	/**
	 * @see #valueAsString()
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public int valueAsInt() throws IOException;

	/**
	 * @see #valueAsString()
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public long valueAsLong() throws IOException;

	/**
	 * @see #valueAsString()
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public double valueAsDouble() throws IOException;

	/**
	 * @see #valueAsString()
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public short valueAsShort() throws IOException;

	/**
	 * @see #valueAsString()
	 * @return
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public float valueAsFloat() throws IOException;

	/**
	 * @see #valueAsString()
	 * @return
	 * @throws IOException
	 */
	public boolean valueAsBoolean() throws IOException;
}
