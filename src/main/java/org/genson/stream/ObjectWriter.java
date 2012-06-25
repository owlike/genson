package org.genson.stream;

import java.io.Flushable;
import java.io.IOException;

public interface ObjectWriter extends Flushable {
	
	public ObjectWriter beginArray() throws IOException;
	
	public ObjectWriter endArray() throws IOException;
	
	public ObjectWriter beginObject() throws IOException;
	
	public ObjectWriter endObject() throws IOException;
	
	public ObjectWriter writeName(String name) throws IOException;

	public ObjectWriter writeValue(int value) throws IOException;
	
	public ObjectWriter writeValue(double value) throws IOException;
	
	public ObjectWriter writeValue(long value) throws IOException;
	
	public ObjectWriter writeValue(boolean value) throws IOException;
	
	public ObjectWriter writeValue(Number value) throws IOException;
	
	public ObjectWriter writeValue(String value) throws IOException;
	
	public ObjectWriter writeUnsafeValue(String value) throws IOException;
	
	public ObjectWriter writeNull() throws IOException;
	
	public ObjectWriter metadata(String name, String value) throws IOException;
}
