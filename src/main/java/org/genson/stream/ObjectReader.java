package org.genson.stream;

import java.io.Closeable;
import java.io.IOException;

public interface ObjectReader extends Closeable {
	public ObjectReader beginObject() throws IOException;
	public ObjectReader endObject() throws IOException;
	public ObjectReader beginArray() throws IOException;
	public ObjectReader endArray() throws IOException;
	
	public ObjectReader nextObjectMetadata() throws IOException;
	public ValueType next() throws IOException;
	public boolean hasNext() throws IOException;
	public ObjectReader skipValue() throws IOException;
	
	public ValueType getValueType();
	public String metadata(String name) throws IOException;
	public String name() throws IOException;
	public String valueAsString() throws IOException;
	public int valueAsInt() throws IOException;
	public long valueAsLong() throws IOException;
	public double valueAsDouble() throws IOException;
	public boolean valueAsBoolean() throws IOException;
}
