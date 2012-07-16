package org.genson.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * ObjectWriter defines the api allowing to write data to different formats and
 * the contract for classes that implement ObjectWriter to provide different formats support.
 * 
 * If you want to write the array new int[1, 2, 3] to the ObjectWriter:
 * <pre>
 * writer.beginArray().writeValue(1).writeValue(2).writeValue(3).endArray();
 * </pre>
 * 
 * And to write Person (we simplify, in practice you must handle null values):
 * <pre>
 * 	class Person {
 * 		String name;
 * 		int age;
 * 		int[] childrenYearOfBirth;
 * }
 * writer.beginObject().writeName("name").writeValue(name).writeName("age").writeAge(age).beginArray();
 * for (int year : childrenYearOfBirth) writer.writeValue(year);
 * writer.endArray().endObject();
 * </pre>
 * 
 * ObjectWriter implementations will handle the representation of the data. Actually it will only
 * write it to the json format however it may be possible to have other formats, for example xml.
 * 
 * @author eugen
 * 
 */
public interface ObjectWriter extends Flushable, Closeable {

	public ObjectWriter beginArray() throws IOException;

	public ObjectWriter endArray() throws IOException;

	public ObjectWriter beginObject() throws IOException;

	public ObjectWriter endObject() throws IOException;

	/**
	 * Writes the name of a property. Names can be written only in objects and must be called before
	 * writing the properties value.
	 * 
	 * @param name
	 * @return a reference to this ObjectWriter, allowing method chaining.
	 * @throws IOException
	 */
	public ObjectWriter writeName(String name) throws IOException;

	public ObjectWriter writeValue(int value) throws IOException;

	public ObjectWriter writeValue(double value) throws IOException;

	public ObjectWriter writeValue(long value) throws IOException;

	public ObjectWriter writeValue(boolean value) throws IOException;

	public ObjectWriter writeValue(Number value) throws IOException;

	/**
	 * Writes value and applies some pre-processing and checks. Use this method if you need to write
	 * some text.
	 * 
	 * @param value
	 * @return a reference to this ObjectWriter, allowing method chaining.
	 * @throws IOException
	 */
	public ObjectWriter writeValue(String value) throws IOException;

	/**
	 * Writes value as is without any pre-processing, it's faster than {@link #writeValue(String)}
	 * but should be used only if you know that it is safe.
	 * 
	 * @param value
	 * @return a reference to this ObjectWriter, allowing method chaining.
	 * @throws IOException
	 */
	public ObjectWriter writeUnsafeValue(String value) throws IOException;

	/**
	 * Must be called when a null value is encountered. Implementations will deal with the null
	 * representation (just skip it or write null, etc).
	 * 
	 * @return a reference to this ObjectWriter, allowing method chaining.
	 * @throws IOException
	 */
	public ObjectWriter writeNull() throws IOException;


//	public ObjectWriter write(String name, boolean value) throws IOException;
//	public ObjectWriter write(String name, double value) throws IOException;
//	public ObjectWriter write(String name, long value) throws IOException;
//	public ObjectWriter write(String name, int value) throws IOException;
//	public ObjectWriter write(String name, Number value) throws IOException;
//	public ObjectWriter write(String name, String value) throws IOException;
//	public ObjectWriter writeUnsafe(String name, String value) throws IOException;
//	public ObjectWriter writeNull(String name) throws IOException;
	public ObjectWriter metadata(String name, String value) throws IOException;
}
