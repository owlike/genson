package org.likeit.json.stream;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.likeit.transformation.stream.JsonWriter;

public class JsonWriterTest {
	private StringWriter sw;
	private JsonWriter w;
	
	@Before public void init() {
		sw = new StringWriter();
		w = new JsonWriter(sw);
	}
	
	@Test public void testRootEmptyArray() throws IOException {
		w.beginArray().endArray();
		assertEquals(sw.toString(), "[]");
	}
	
	@Test public void testRootArrayNumbers() throws IOException {
		w.beginArray().value(11).value(0.09).value(0.0009).value(-51.07).endArray();
		assertEquals(sw.toString(), "[11,0.09,9.0E-4,-51.07]");
	}
	
	@Test public void testRootArrayStrings() throws IOException {
		w.beginArray().value("a").value("b . d").value("\"\\ u").endArray();
		String s = "[\"a\",\"b . d\",\"\\\"\\\\ u\"]";
		assertEquals(sw.toString(), s);
	}
	
	@Test public void testRootArrayBooleans() throws IOException {
		w.beginArray().value(false).value(true).value(false).endArray();
		assertEquals(sw.toString(), "[false,true,false]");
	}

	@Test public void testRootObject() throws IOException {
		w.beginObject().name("nom").value("toto")
					.name("null").valueNull()
					.name("doub").value(10.012)
					.name("int").value(7)
					.name("bool").value(false)
					.name("emptyObj").beginObject().endObject()
					.name("emptyTab").beginArray().endArray()
		.endObject();
		
		String value = "{\"nom\":\"toto\",\"null\":null,\"doub\":10.012,\"int\":7,\"bool\":false,\"emptyObj\":{},\"emptyTab\":[]}";
		assertEquals(sw.toString(), value);
	}
	
	@Test public void testRootObjectWithNested() throws IOException {
		w.beginObject()
			.name("nom").value("toto")
			.name("null").valueNull()
			.name("doub").value(10.012)
			.name("int").value(7)
			.name("bool").value(false)
			.name("nestedObj").beginObject()
				.name("h1").value("fd")
				.name("h2").value(true)
				.name("htab").beginArray()
					.value(false).value(4).value("s t")
				.endArray()
			.endObject()
			.name("nestedTab").beginArray()
    			.value(8)
    			.beginArray().value("hey").value(2.29).value("bye").endArray()
    			.beginObject().name("t1").value(true).name("t2").value("kk").name("t3").valueNull().endObject()
			.endArray()
		.endObject();
		
		String value = "{\"nom\":\"toto\",\"null\":null,\"doub\":10.012,\"int\":7,\"bool\":false," +
				"\"nestedObj\":{\"h1\":\"fd\",\"h2\":true,\"htab\":[false,4,\"s t\"]}," +
				"\"nestedTab\":[8,[\"hey\",2.29,\"bye\"],{\"t1\":true,\"t2\":\"kk\",\"t3\":null}]}";
		assertEquals(sw.toString(), value);
	}
	
}
