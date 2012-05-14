package org.likeit.json.stream;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import static org.junit.Assert.*;
import org.likeit.transformation.stream.JsonReader;
import org.likeit.transformation.stream.TokenType;

public class JsonReaderTest {

	@Test
	public void testReader() throws IOException {
		String src = "{\"nom\" : \"toto titi, tu\", \"pre \\\"n'om\" : \"albert  \", \"entier\" : 1322.6}";
		JsonReader reader = new JsonReader(new StringReader(src));
		reader.beginObject();

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.STRING);
		assertEquals(reader.name(), "nom");
		assertEquals(reader.value(), "toto titi, tu");
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.STRING);
		assertEquals(reader.name(), "pre \"n'om");
		assertEquals(reader.value(), "albert  ");
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.DOUBLE);
		assertEquals(reader.name(), "entier");
		assertEquals(reader.value(), "1322.6");
		assertFalse(reader.hasNext());

		reader.endObject();
	}

	@Test
	public void testTokenTypesAndHasNext() throws IOException {
		String src = "[{\"a\": 1, \"b\": \"a\", \"c\":1.1,\"d\":null,\"e\":false, \"f\":[]},[1, 1.1], null, false, true, \"tt\"]";
		JsonReader reader = new JsonReader(new StringReader(src));

		reader.beginArray();

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.OBJECT);
		assertEquals(reader.beginObject().next(), TokenType.INTEGER);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.STRING);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.DOUBLE);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.NULL);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.BOOLEAN);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.ARRAY);
		assertFalse(reader.beginArray().hasNext());
		assertTrue(reader.endArray().endObject().hasNext());

		assertEquals(reader.next(), TokenType.ARRAY);
		assertTrue(reader.beginArray().hasNext());
		assertEquals(reader.next(), TokenType.INTEGER);
		assertEquals(reader.next(), TokenType.DOUBLE);
		assertFalse(reader.hasNext());

		assertTrue(reader.endArray().hasNext());
		assertEquals(reader.next(), TokenType.NULL);
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.BOOLEAN);
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.BOOLEAN);
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), TokenType.STRING);
		assertFalse(reader.hasNext());

		reader.endArray();
	}

	@Test
	public void testPrimitivesArray() throws IOException {
		String src = "[\"\\u0019\",\"abcde ..u\",null,12222.0101,true,false,9.0E-7]";
		JsonReader reader = new JsonReader(new StringReader(src));
		
		reader.beginArray();

		reader.next();
		assertEquals(reader.value(), "\u0019");
		reader.next();
		assertEquals(reader.value(), "abcde ..u");
		reader.next();
		assertEquals(reader.value(), "null");
		reader.next();
		assertEquals(reader.value(), "12222.0101");
		reader.next();
		assertEquals(reader.value(), "true");
		reader.next();
		assertEquals(reader.value(), "false");
		reader.next();
		assertEquals(reader.value(), "9.0E-7");

		assertFalse(reader.hasNext());

		reader.endArray();
	}

	@Test
	public void testPrimitivesObject() throws IOException {
		String src = "{\"a\":1.0,\"b\":\"abcde ..u\",\"c\":null,\"d\":12222.0101,\"e\":true,\"f\":false,\"h\":-0.9}";
		JsonReader reader = new JsonReader(new StringReader(src));

		reader.beginObject();

		reader.next();
		assertEquals(reader.name(), "a");
		assertEquals(reader.value(), "1.0");
		reader.next();
		assertEquals(reader.name(), "b");
		assertEquals(reader.value(), "abcde ..u");
		reader.next();
		assertEquals(reader.name(), "c");
		assertEquals(reader.value(), "null");
		reader.next();
		assertEquals(reader.name(), "d");
		assertEquals(reader.value(), "12222.0101");
		reader.next();
		assertEquals(reader.name(), "e");
		assertEquals(reader.value(), "true");
		reader.next();
		assertEquals(reader.name(), "f");
		assertEquals(reader.value(), "false");
		reader.next();
		assertEquals(reader.name(), "h");
		assertEquals(reader.value(), "-0.9");

		reader.endObject();
	}

	@Test
	public void testEmptyArrayAndObjects() throws IOException {
		String src = "[{},[]]";
		JsonReader reader = new JsonReader(new StringReader(src));

		assertTrue(reader.beginArray().hasNext());
		assertEquals(reader.next(), TokenType.OBJECT);
		assertFalse(reader.beginObject().hasNext());
		assertTrue(reader.endObject().hasNext());
		assertEquals(reader.next(), TokenType.ARRAY);
		assertFalse(reader.beginArray().hasNext());
		assertFalse(reader.endArray().hasNext());
		reader.endArray();
	}

	@Test
	public void testRootArrayAndNestedObjects() throws IOException {
		String src = "[{},      " + "	[]," + "	[\"a a\", -9.9909], "
				+ "false, " + "{" + "	\"nom\": \"toto\", "
				+ "\"tab\":[5,6,7], "
				+ "\"nestedObj\":	   	 {\"prenom\":\"titi\"}" + "}" + "]";
		JsonReader reader = new JsonReader(new StringReader(src));

		assertTrue(reader.beginArray().hasNext());

		reader.next();
		assertFalse(reader.beginObject().hasNext());
		assertTrue(reader.endObject().hasNext());

		reader.next();
		assertFalse(reader.beginArray().hasNext());
		assertTrue(reader.endArray().hasNext());

		reader.next();
		assertTrue(reader.beginArray().hasNext());
		reader.next();
		assertEquals(reader.value(), "a a");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.value(), "-9.9909");
		assertFalse(reader.hasNext());
		reader.endArray();

		reader.next();
		assertEquals(reader.value(), "false");
		assertTrue(reader.hasNext());

		reader.next();
		assertTrue(reader.beginObject().hasNext());
		reader.next();
		assertEquals(reader.name(), "nom");
		assertEquals(reader.value(), "toto");

		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.name(), "tab");
		assertTrue(reader.beginArray().hasNext());
		reader.next();
		assertEquals(reader.value(), "5");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.value(), "6");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.value(), "7");
		assertFalse(reader.hasNext());
		reader.endArray();
		
		reader.next();
		assertEquals(reader.name(), "nestedObj");
		assertTrue(reader.beginObject().hasNext());
		reader.next();
		assertEquals(reader.name(), "prenom");
		assertEquals(reader.value(), "titi");
		reader.endObject();

		reader.endObject();

		reader.endArray();
	}
}
