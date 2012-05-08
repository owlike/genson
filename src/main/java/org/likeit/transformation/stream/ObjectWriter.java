package org.likeit.transformation.stream;

import java.io.Flushable;
import java.io.IOException;

public interface ObjectWriter extends Flushable {
	
	public ObjectWriter beginArray() throws IOException;
	
	public ObjectWriter endArray() throws IOException;
	
	public ObjectWriter beginObject() throws IOException;
	
	public ObjectWriter endObject() throws IOException;
	
	public ObjectWriter name(String name) throws IOException;

	public ObjectWriter value(int value) throws IOException;
	
	public ObjectWriter value(double value) throws IOException;
	
	public ObjectWriter value(long value) throws IOException;
	
	public ObjectWriter value(boolean value) throws IOException;
	
	public ObjectWriter value(Number value) throws IOException;
	
	public ObjectWriter value(String value) throws IOException;
	
	public ObjectWriter valueNull() throws IOException;
}
