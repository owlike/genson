package org.genson.stream;

import java.io.IOException;
import java.io.StringWriter;

import org.genson.stream.JsonWriter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class JsonWriterTest {
	private StringWriter sw;
	private JsonWriter w;
	
	@Before public void init() {
		sw = new StringWriter();
		w = new JsonWriter(sw);
	}
	
	@Test public void testRootEmptyArray() throws IOException {
		w.beginArray().endArray().flush();
		assertEquals(sw.toString(), "[]");
	}
	
	@Test public void testRootArrayNumbers() throws IOException {
		w.beginArray().writeValue(11).writeValue(0.09).writeValue(0.0009).writeValue(-51.07).endArray().flush();
		assertEquals(sw.toString(), "[11,0.09,9.0E-4,-51.07]");
	}
	
	@Test public void testRootArrayStrings() throws IOException {
		w.beginArray().writeValue("a").writeValue("b . d").writeValue("\"\\ u").endArray().flush();
		String s = "[\"a\",\"b . d\",\"\\\"\\\\ u\"]";
		assertEquals(sw.toString(), s);
	}
	
	@Test public void testRootArrayBooleans() throws IOException {
		w.beginArray().writeValue(false).writeValue(true).writeValue(false).endArray().flush();
		assertEquals(sw.toString(), "[false,true,false]");
	}

	@Test public void testRootObject() throws IOException {
		w.beginObject().writeName("nom").writeValue("toto")
					.writeName("null").writeNull()
					.writeName("doub").writeValue(10.012)
					.writeName("int").writeValue(7)
					.writeName("bool").writeValue(false)
					.writeName("emptyObj").beginObject().endObject()
					.writeName("emptyTab").beginArray().endArray()
		.endObject().flush();
		
		String value = "{\"nom\":\"toto\",\"null\":null,\"doub\":10.012,\"int\":7,\"bool\":false,\"emptyObj\":{},\"emptyTab\":[]}";
		assertEquals(sw.toString(), value);
	}
	
	@Test public void testRootObjectWithNested() throws IOException {
		w.beginObject()
			.writeName("nom").writeValue("toto")
			.writeName("null").writeNull()
			.writeName("doub").writeValue(10.012)
			.writeName("int").writeValue(7)
			.writeName("bool").writeValue(false)
			.writeName("nestedObj").beginObject()
				.writeName("h1").writeValue("fd")
				.writeName("h2").writeValue(true)
				.writeName("htab").beginArray()
					.writeValue(false).writeValue(4).writeValue("s t")
				.endArray()
			.endObject()
			.writeName("nestedTab").beginArray()
    			.writeValue(8)
    			.beginArray().writeValue("hey").writeValue(2.29).writeValue("bye").endArray()
    			.beginObject().writeName("t1").writeValue(true).writeName("t2").writeValue("kk").writeName("t3").writeNull().endObject()
			.endArray()
		.endObject().flush();
		
		String value = "{\"nom\":\"toto\",\"null\":null,\"doub\":10.012,\"int\":7,\"bool\":false," +
				"\"nestedObj\":{\"h1\":\"fd\",\"h2\":true,\"htab\":[false,4,\"s t\"]}," +
				"\"nestedTab\":[8,[\"hey\",2.29,\"bye\"],{\"t1\":true,\"t2\":\"kk\",\"t3\":null}]}";
		assertEquals(sw.toString(), value);
	}
	
}
