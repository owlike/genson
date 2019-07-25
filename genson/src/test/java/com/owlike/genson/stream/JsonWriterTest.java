package com.owlike.genson.stream;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonWriterTest {
  private StringWriter sw;
  private JsonWriter w;

  @Before
  public void init() {
    sw = new StringWriter();
    w = new JsonWriter(sw);
  }

  @Test(expected = JsonStreamException.class)
  public void testPreventInvalidJsonOutputInObject() {
    w.beginObject().writeValue("must fail");
  }

  @Test(expected = JsonStreamException.class)
  public void testPreventInvalidJsonOutputInArray() {
    w.beginArray().writeName("key").writeValue("must fail");
  }

  @Test
  public void testRootEmptyArray() {
    w.beginArray().endArray().flush();
    assertEquals(sw.toString(), "[]");
  }

  @Test
  public void testRootArrayNumbers() {
    w.beginArray().writeValue(11).writeValue(0.09).writeValue(0.0009).writeValue(-51.07)
      .endArray().flush();
    assertEquals(sw.toString(), "[11,0.09,9.0E-4,-51.07]");
  }

  @Test
  public void testRootArrayStrings() {
    w.beginArray().writeValue("a").writeValue("b . d").writeValue("\"\\ u").endArray().flush();
    String s = "[\"a\",\"b . d\",\"\\\"\\\\ u\"]";
    assertEquals(sw.toString(), s);
  }

  @Test
  public void testRootArrayBooleans() {
    w.beginArray().writeValue(false).writeValue(true).writeValue(false).endArray().flush();
    assertEquals(sw.toString(), "[false,true,false]");
  }

  @Test
  public void testRootObject() {
    w.beginObject().writeName("nom").writeValue("toto").writeName("null").writeNull()
      .writeName("doub").writeValue(10.012).writeName("int").writeValue(7)
      .writeName("bool").writeValue(false).writeName("emptyObj").beginObject()
      .endObject().writeName("emptyTab").beginArray().endArray().endObject().flush();

    String value =
      "{\"nom\":\"toto\",\"null\":null,\"doub\":10.012,\"int\":7,\"bool\":false,\"emptyObj\":{},\"emptyTab\":[]}";
    assertEquals(sw.toString(), value);
  }

  @Test
  public void testRootObjectWithNested() {
    w.beginObject().writeName("nom").writeValue("toto").writeName("null").writeNull()
      .writeName("doub").writeValue(10.012).writeName("int").writeValue(7)
      .writeName("bool").writeValue(false).writeName("nestedObj").beginObject()
      .writeName("h1").writeValue("fd").writeName("h2").writeValue(true)
      .writeName("htab").beginArray().writeValue(false).writeValue(4).writeValue("s t")
      .endArray().endObject().writeName("nestedTab").beginArray().writeValue(8)
      .beginArray().writeValue("hey").writeValue(2.29).writeValue("bye").endArray()
      .beginObject().writeName("t1").writeValue(true).writeName("t2").writeValue("kk")
      .writeName("t3").writeNull().endObject().endArray().endObject().flush();

    String value =
      "{\"nom\":\"toto\",\"null\":null,\"doub\":10.012,\"int\":7,\"bool\":false,"
        + "\"nestedObj\":{\"h1\":\"fd\",\"h2\":true,\"htab\":[false,4,\"s t\"]},"
        + "\"nestedTab\":[8,[\"hey\",2.29,\"bye\"],{\"t1\":true,\"t2\":\"kk\",\"t3\":null}]}";
    assertEquals(sw.toString(), value);
  }

  @Test(expected = JsonStreamException.class)
  public void testExpectNameInObject() {
    w.beginObject().beginArray();
  }

  @Test
  public void testWriteMetadataWithBeginObject() {
    String expected = "{\"@doc\":\"My doc\",\"name\":null}";
    w.beginObject().writeMetadata("doc", "My doc").writeName("name").writeNull().endObject()
      .flush();
    assertEquals(expected, sw.toString());
  }

  @Test
  public void testArrayMetadataMustSkipSilently() {
    w.beginNextObjectMetadata().writeMetadata("key", "value").beginArray();
    assertTrue(w._metadata.isEmpty());
    assertEquals(JsonType.ARRAY, w._ctx.pop());
    assertEquals(JsonType.EMPTY, w._ctx.pop());
  }

  @Test
  public void testLiteralMetadataMustSkipSilentlyInArray() {
    w.beginArray().beginNextObjectMetadata().writeMetadata("a", "ooooo").writeValue(true)
      .beginObject().endObject().endArray().flush();
    assertEquals("[true,{}]", sw.toString());
  }

  @Test
  public void testWriteMetadataWithBeginNextObjectMetadata() {
    try {
      w.beginNextObjectMetadata().writeMetadata("doc", "My doc").writeName("name")
        .writeNull().endObject().flush();
      fail();
    } catch (RuntimeException ie) {
    }
  }

  @Test
  public void testWriteMetadataWithBeginNextObjectMetadata2() {
    String expected = "{\"@doc\":\"My doc\",\"name\":null}";
    w.beginNextObjectMetadata().writeMetadata("doc", "My doc").beginObject().writeName("name")
      .writeNull().endObject().flush();
    assertEquals(expected, sw.toString());
  }

  @Test
  public void testWriteMetadataWithMultipleBeginNextObjectMetadata() {
    String expected = "{\"@doc\":\"My doc\",\"@a\":\"\",\"name\":null}";
    w.beginNextObjectMetadata().beginNextObjectMetadata().writeMetadata("doc", "My doc")
      .beginNextObjectMetadata().writeMetadata("a", "").beginObject().writeName("name")
      .writeNull().endObject().flush();
    assertEquals(expected, sw.toString());
  }

  @Test
  public void testPrettyPrint() {
    String expected =
      "[\n  2,\n  false,\n  {\n    \"@class\":\"titi\",\n    \"@cc2\":\"iuuiio\",\n    \"name\":\"toto\",\n    \"uu\":null\n  }\n]";
    StringWriter sw = new StringWriter();
    JsonWriter writer = new JsonWriter(sw, false, false, true);
    writer.beginArray().writeValue(2).writeValue(false).beginNextObjectMetadata()
      .writeMetadata("class", "titi").beginObject().writeMetadata("cc2", "iuuiio")
      .writeName("name").writeValue("toto").writeName("uu").writeNull().endObject()
      .endArray().flush();
    writer.flush();
    writer.close();
    assertEquals(expected, sw.toString());
  }

  @Test(expected = NumberFormatException.class)
  public void testDoubleNanThrowsException() {
    w.writeValue(Double.NaN);
  }

  @Test(expected = NumberFormatException.class)
  public void testFloatNaNThrowsException() {
    w.writeValue(Float.NaN);
  }

  @Test(expected = NumberFormatException.class)
  public void testDoubleInfinityhrowsException() {
    w.writeValue(Double.NEGATIVE_INFINITY);
  }

  @Test(expected = NumberFormatException.class)
  public void testFloatInfinityhrowsException() {
    w.writeValue(Float.POSITIVE_INFINITY);
  }

  @Test
  public void writeNullValuesUsingNullSafeMethods_Object() {
    w.beginObject()
      .writeName("a").writeString(null)
      .writeName("b").writeNumber(null)
      .writeName("c").writeNumber(2)
      .endObject()
      .flush();

    assertEquals("{\"a\":null,\"b\":null,\"c\":2}", sw.toString());
  }

  @Test
  public void writeNullValuesUsingNullSafeMethods_Array() {
    w.beginArray()
      .writeString(null)
      .writeNumber(null)
      .writeNumber(2)
      .endArray()
      .flush();

    assertEquals("[null,null,2]", sw.toString());
  }

  @Test
  public void escapeSpecialCharactersAndWriteName() {
    w.beginObject()
      .writeBoolean("foo\"bar", true)
      .writeEscapedName("bar\\\"foo".toCharArray()).writeValue(1)
      .endObject()
      .flush();

    assertEquals("{\"foo\\\"bar\":true,\"bar\\\"foo\":1}", sw.toString());
  }

  @Test public void escapeStringShouldNotSwallowCharacters() {
    assertEquals("a\\nb\\\\nc", new String(JsonWriter.escapeString("a\nb\\nc")));
  }
}
