package com.owlike.genson.stream;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.owlike.genson.stream.JsonReader;
import com.owlike.genson.stream.ValueType;

import static org.junit.Assert.*;


public class JsonReaderTest {

	@Test
	public void testReader() throws IOException {
		String src = "{\"nom\" : \"toto titi, tu\", \"prenom\" : \"albert  \", \"entier\" : 1322.6}";
		JsonReader reader = new JsonReader(new StringReader(src));
		reader.beginObject();

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.STRING);
		assertEquals(reader.name(), "nom");
		assertEquals(reader.valueAsString(), "toto titi, tu");
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.STRING);
		assertEquals(reader.name(), "prenom");
		assertEquals(reader.valueAsString(), "albert  ");
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.DOUBLE);
		assertEquals(reader.name(), "entier");
		assertEquals(reader.valueAsString(), "1322.6");
		assertFalse(reader.hasNext());

		reader.endObject();
	}

	@Test
	public void testTokenTypesAndHasNext() throws IOException {
		String src = "[{\"a\": 1, \"b\": \"a\", \"c\":1.1,\"d\":null,\"e\":false, \"f\":[]},[1, 1.1], null, false, true, \"tt\"]";
		JsonReader reader = new JsonReader(new StringReader(src));

		reader.beginArray();

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.OBJECT);
		assertEquals(reader.beginObject().next(), ValueType.INTEGER);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.STRING);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.DOUBLE);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.NULL);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.BOOLEAN);

		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.ARRAY);
		assertFalse(reader.beginArray().hasNext());
		assertTrue(reader.endArray().endObject().hasNext());

		assertEquals(reader.next(), ValueType.ARRAY);
		assertTrue(reader.beginArray().hasNext());
		assertEquals(reader.next(), ValueType.INTEGER);
		assertEquals(reader.next(), ValueType.DOUBLE);
		assertFalse(reader.hasNext());

		assertTrue(reader.endArray().hasNext());
		assertEquals(reader.next(), ValueType.NULL);
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.BOOLEAN);
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.BOOLEAN);
		assertTrue(reader.hasNext());
		assertEquals(reader.next(), ValueType.STRING);
		assertFalse(reader.hasNext());

		reader.endArray();
	}
	
	@Test public void testReadDoubles() throws IOException {
		String src = "[" + String.valueOf(Double.MAX_VALUE) + "," + String.valueOf(Double.MIN_VALUE) + 
				","+ Double.MAX_VALUE + "" + 1 + "," + -Double.MAX_VALUE + "" + 1 + "]";
		JsonReader reader = new JsonReader(src);
		reader.beginArray();
		reader.next();
		assertTrue(Double.MAX_VALUE == reader.valueAsDouble());
		reader.next();
		assertTrue(Double.MIN_VALUE == reader.valueAsDouble());
		reader.next();
		assertTrue(Double.isInfinite(reader.valueAsDouble()));
		assertTrue(Double.MAX_VALUE < reader.valueAsDouble());
		reader.next();
		assertTrue(Double.isInfinite(reader.valueAsDouble()));
		assertTrue(0 > reader.valueAsDouble());
		reader.endArray();
	}
	
	@Test public void testReadLong() throws IOException {
		String src = "[" + String.valueOf(Long.MAX_VALUE) + "," + String.valueOf(Long.MIN_VALUE) + 
				","+ Long.MAX_VALUE + "" + 1 + "," + -Long.MAX_VALUE + "" + 1 + "," + 999999999999999990l + "]";
		JsonReader reader = new JsonReader(src);
		reader.beginArray();
		reader.next();
		assertTrue(Long.MAX_VALUE == reader.valueAsLong());
		reader.next();
		assertTrue(Long.MIN_VALUE == reader.valueAsLong());
		reader.next();
		// for the moment we won't do anything special for cases where numbers overflow
		// later we can add an option to throw exceptions if numbers overflow
		long l = Long.MAX_VALUE * 10 + 1;
		assertTrue(l == reader.valueAsLong());
		reader.next();
		assertTrue(-l == reader.valueAsLong());
		reader.next();
		assertTrue(999999999999999990l == reader.valueAsLong());
		reader.endArray();
		
	}

	@Test
	public void testPrimitivesArray() throws IOException {
		String src = "[\"\\u0019\",\"abcde ..u\",null,12222.0101,true,false,9.0E-7]";
		JsonReader reader = new JsonReader(new StringReader(src));
		
		reader.beginArray();

		reader.next();
		assertEquals(reader.valueAsString(), "\u0019");
		reader.next();
		assertEquals(reader.valueAsString(), "abcde ..u");
		reader.next();
		assertEquals(reader.valueAsString(), "null");
		reader.next();
		assertEquals(reader.valueAsString(), "12222.0101");
		reader.next();
		assertEquals(reader.valueAsString(), "true");
		reader.next();
		assertEquals(reader.valueAsString(), "false");
		reader.next();
		assertEquals(reader.valueAsString(), "9.0E-7");
		assertTrue(reader.valueAsDouble()==0.0000009);

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
		assertEquals(reader.valueAsString(), "1.0");
		reader.next();
		assertEquals(reader.name(), "b");
		assertEquals(reader.valueAsString(), "abcde ..u");
		reader.next();
		assertEquals(reader.name(), "c");
		assertEquals(reader.valueAsString(), "null");
		reader.next();
		assertEquals(reader.name(), "d");
		assertEquals(reader.valueAsString(), "12222.0101");
		reader.next();
		assertEquals(reader.name(), "e");
		assertEquals(reader.valueAsString(), "true");
		reader.next();
		assertEquals(reader.name(), "f");
		assertEquals(reader.valueAsString(), "false");
		reader.next();
		assertEquals(reader.name(), "h");
		assertEquals(reader.valueAsString(), "-0.9");

		reader.endObject();
	}

	@Test
	public void testEmptyArrayAndObjects() throws IOException {
		String src = "[{},[]]";
		JsonReader reader = new JsonReader(new StringReader(src));

		assertTrue(reader.beginArray().hasNext());
		assertEquals(reader.next(), ValueType.OBJECT);
		assertFalse(reader.beginObject().hasNext());
		assertTrue(reader.endObject().hasNext());
		assertEquals(reader.next(), ValueType.ARRAY);
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
		assertEquals(reader.valueAsString(), "a a");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.valueAsString(), "-9.9909");
		assertFalse(reader.hasNext());
		reader.endArray();

		reader.next();
		assertEquals(reader.valueAsString(), "false");
		assertTrue(reader.hasNext());

		reader.next();
		assertTrue(reader.beginObject().hasNext());
		reader.next();
		assertEquals(reader.name(), "nom");
		assertEquals(reader.valueAsString(), "toto");

		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.name(), "tab");
		assertTrue(reader.beginArray().hasNext());
		reader.next();
		assertEquals(reader.valueAsString(), "5");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.valueAsString(), "6");
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.valueAsString(), "7");
		assertFalse(reader.hasNext());
		reader.endArray();
		
		reader.next();
		assertEquals(reader.name(), "nestedObj");
		assertTrue(reader.beginObject().hasNext());
		reader.next();
		assertEquals(reader.name(), "prenom");
		assertEquals(reader.valueAsString(), "titi");
		reader.endObject();

		reader.endObject();

		reader.endArray();
	}
	
	@Test public void testSkipValue() throws IOException {
		String src = "{\"a\":[], \"b\":{}, \"c\": [{\"c\":null, \"d\":121212.02}, 4, null], \"e\":1234, \"end\":\"the end\"}";
		JsonReader reader = new JsonReader(new StringReader(src));
		
		reader.beginObject();
		
		reader.next();
		reader.skipValue();
		
		assertTrue(reader.hasNext());
		reader.next();
		assertEquals(reader.name(), "b");
		reader.skipValue();
		
		assertTrue(reader.hasNext());
		reader.next(); 
		assertEquals(reader.name(), "c");
		reader.skipValue();
		
		assertTrue(reader.hasNext());
		reader.next(); 
		assertEquals(reader.name(), "e");
		reader.skipValue();
		
		assertTrue(reader.hasNext());
		reader.next(); 
		assertEquals(reader.name(), "end");
		
		reader.endObject();
	}
	
	@Test public void testIllegalReadObjectInstedOfArray() throws IOException {
		String src = "[1,2]";
		JsonReader reader = new JsonReader(new StringReader(src));
		try {
			reader.beginObject();
			fail();
		} catch (IllegalStateException ise) {}
	}
	
	@Test public void testIllegalOperationCallNext() throws IOException {
		String src = "[1,2]";
		JsonReader reader = new JsonReader(new StringReader(src));
		try {
			reader.beginArray();
			reader.next();
			reader.next();
			reader.next();
			fail();
		} catch (IllegalStateException ise) {}
	}
	
	@Test public void testIncompleteSource() throws IOException {
		String src = "[1,";
		JsonReader reader = new JsonReader(new StringReader(src));
		try {
			reader.beginArray();
			reader.next();
			reader.next();
			fail();
		} catch (IOException ioe) {}
	}
	
	@Test
	public void testMetadata() throws IOException {
		String src = "{\"@class\"	: \"theclass\"" +
				",     \"@author\":\"me\"" +
				", \"@comment\":\"no comment\"" +
				", \"obj\" :      	" +
				"			{\"@class\":\"anotherclass\"}}";
		JsonReader reader = new JsonReader(new StringReader(src));
		assertTrue(reader.beginObject().hasNext());
		assertEquals("theclass", reader.metadata("class"));
		assertEquals("me", reader.metadata("author"));
		assertEquals("no comment", reader.metadata("comment"));
		reader.next();
		reader.beginObject();
		assertNull(reader.metadata("author"));
		assertEquals("anotherclass", reader.metadata("class"));
		assertFalse(reader.hasNext());
		reader.endObject().endObject();
	}
	
	@Test public void testMultipleCallsTonextObjectMetadata() throws IOException {
		String src = "{\"@class\"	: \"theclass\"" +
				",     \"@author\":\"me\"" +
				", \"@comment\":\"no comment\"}";
		JsonReader reader = new JsonReader(new StringReader(src));
		assertEquals("theclass", reader.nextObjectMetadata().nextObjectMetadata().metadata("class"));
		assertEquals("theclass", reader.nextObjectMetadata().metadata("class"));
		assertEquals("no comment", reader.metadata("comment"));
		assertEquals("no comment", reader.nextObjectMetadata().metadata("comment"));
		assertEquals("me", reader.beginObject().metadata("author"));
		reader.endObject();
	}
	
	@Test public void testReadMalformedJson() throws IOException {
		String src = "";
		JsonReader reader = new JsonReader(src);
		try {
			reader.beginObject();
			fail();
		} catch (IllegalStateException ise) {}
	}
}
