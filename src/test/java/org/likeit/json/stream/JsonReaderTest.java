package org.likeit.json.stream;

import java.io.IOException;
import java.io.StringReader;

import org.codehaus.jackson.map.MappingIterator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;
import org.likeit.transformation.stream.JsonReader;

public class JsonReaderTest {
	@Test
	public void testReader() throws IOException {
		String src = "{\"nom\" : \"toto titi, tu\", \"pre \\\"n'om\" : \"albert  \", \"entier\" : 1322.6}";
		JsonReader reader = new JsonReader(new StringReader(src));
		reader.beginObject();

		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.name(), "nom");
		assertEquals(reader.value(), "toto titi, tu");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.name(), "pre \"n'om");
		assertEquals(reader.value(), "albert  ");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.name(), "entier");
		assertEquals(reader.value(), "1322.6");
		assertFalse(reader.hasNext());

		reader.endObject();
	}

	@Test
	public void testPrimitivesArray() throws IOException {
		String src = "[1.0,\"abcde ..u\",null,12222.0101,true,false,-0.9]";
		JsonReader reader = new JsonReader(new StringReader(src));

		reader.beginArray();

		assertEquals(reader.next().value(), "1.0");
		assertEquals(reader.next().value(), "abcde ..u");
		assertEquals(reader.next().value(), "null");
		assertEquals(reader.next().value(), "12222.0101");
		assertEquals(reader.next().value(), "true");
		assertEquals(reader.next().value(), "false");
		assertEquals(reader.next().value(), "-0.9");

		reader.endArray();
	}

	@Test
	public void testPrimitivesObject() throws IOException {
		String src = "{\"a\":1.0,\"b\":\"abcde ..u\",\"c\":null,\"d\":12222.0101,\"e\":true,\"f\":false,\"h\":-0.9}";
		JsonReader reader = new JsonReader(new StringReader(src));

		reader.beginObject();

		assertEquals(reader.next().name(), "a");
		assertEquals(reader.value(), "1.0");
		assertEquals(reader.next().name(), "b");
		assertEquals(reader.value(), "abcde ..u");
		assertEquals(reader.next().name(), "c");
		assertEquals(reader.value(), "null");
		assertEquals(reader.next().name(), "d");
		assertEquals(reader.value(), "12222.0101");
		assertEquals(reader.next().name(), "e");
		assertEquals(reader.value(), "true");
		assertEquals(reader.next().name(), "f");
		assertEquals(reader.value(), "false");
		assertEquals(reader.next().name(), "h");
		assertEquals(reader.value(), "-0.9");

		reader.endObject();
	}

	@Test
	public void testRootArrayAndNestedObjects() throws IOException {
		String src = "[{},      " +
						"	[]," +
						"	[\"a a\", -9.9909], " +
						"false, " +
						"{" +
							"	\"nom\": \"toto\", " +
							"\"tab\":[5,6,7], " +
							"\"nestedObj\":	   	 {\"prenom\":\"titi\"}" +
						"}" +
					  "]";
		JsonReader reader = new JsonReader(new StringReader(src));

		assertTrue(reader.beginArray().hasNext());
		
		assertTrue(!reader.beginObject().hasNext());
		reader.endObject();
		
		assertTrue(!reader.beginArray().hasNext());
		reader.endArray();
		
		assertTrue(reader.beginArray().hasNext());
		assertEquals(reader.next().name(), null);
		assertEquals(reader.next().value(), "\"a a\"");
		assertTrue(reader.hasNext());
		assertEquals(reader.next().value(), "-9.9909");
		assertTrue(!reader.hasNext());
		assertEquals(reader.next().name(), "nom");
		assertTrue(reader.beginArray().hasNext());
		assertEquals(reader.next().value(), "5");
		assertTrue(reader.hasNext());
		assertEquals(reader.next().value(), "6");
		assertTrue(reader.hasNext());
		assertEquals(reader.next().value(), "7");
		assertTrue(reader.hasNext());
		assertEquals(reader.next().value(), "false");
		
		assertTrue(reader.hasNext());
		assertTrue(reader.beginObject().hasNext());
		
		reader.endObject();
		
		reader.endArray();
	}
}
