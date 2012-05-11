package org.likeit.json.deserialization;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.likeit.transformation.stream.JsonReader;

public class JsonReaderTest {
	@Test public void testReader() throws IOException {
		String src = "{\"nom\" : \"toto titi, tu\"}";
		JsonReader reader = new JsonReader(new StringReader(src));
		reader.beginObject();
		String name = reader.name();
		System.out.println(name);
	}
}
