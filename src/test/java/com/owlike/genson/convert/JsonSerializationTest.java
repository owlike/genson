package com.owlike.genson.convert;

import java.util.Arrays;
import java.util.List;

import com.owlike.genson.GensonBuilder;
import org.junit.Test;

import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.bean.ComplexObject;
import com.owlike.genson.bean.Primitives;
import com.owlike.genson.bean.Media.Player;

import static org.junit.Assert.*;

public class JsonSerializationTest {
	Genson genson = new Genson();
	
	@Test
	public void testJsonPrimitiveObject() {
		Primitives p = createPrimitives();
		String json = genson.serialize(p);
		assertEquals(p.jsonString(), json);
	}

	@Test
	public void testJsonArrayOfPrimitives() {
		String expected = "[\"a\",1,3.2,null,true]";
		Object[] array = new Object[] { "a", 1, 3.2, null, true };
		String json = genson.serialize(array);
		assertEquals(expected, json);
	}

	@Test
	public void testJsonArrayOfMixedContent() {
		Primitives p = createPrimitives();
		p.setIntPrimitive(-88);
		p.setDoubleObject(null);
		String expected = "[\"a\"," + p.jsonString() + ",1,3.2,null,false," + p.jsonString() + "]";
		Object[] array = new Object[] { "a", p, 1, 3.2, null, false, p };
		String json = genson.serialize(array);
		assertEquals(expected, json);
	}

	@Test
	public void testJsonComplexObject() {
		Primitives p = createPrimitives();
		List<Primitives> list = Arrays.asList(p, p, p, p, p);
		ComplexObject co = new ComplexObject(p, list, list.toArray(new Primitives[list.size()]));
		String json = genson.serialize(co);
		assertEquals(co.jsonString(), json);
	}

	/*
	 * Serialize all public getXX present and all the public/package fields that don't match an used
	 * XX getter.
	 */
	@Test
	public void testSerializationMixedFieldsAndGetters() {
		String json = "{\"age\":15,\"name\":\"TOTO\",\"noField\":\"TOTO15\"}";
		ClassWithFieldsAndGetter object = new ClassWithFieldsAndGetter("TOTO", 15);
		String out = genson.serialize(object);
		assertEquals(json, out);
	}

	@Test
	public void testSerializeWithAlias() {
		Genson genson = new GensonBuilder().addAlias("ClassWithFieldsAndGetter",
				ClassWithFieldsAndGetter.class).create();
		String json = genson.serialize(new ClassWithFieldsAndGetter("a", 0));
		assertTrue(json.startsWith("{\"@class\":\"ClassWithFieldsAndGetter\""));
		genson = new GensonBuilder().useClassMetadata(true).create();
		json = genson.serialize(new ClassWithFieldsAndGetter("a", 0));
		assertTrue(json
				.startsWith("{\"@class\":\"com.owlike.genson.convert.JsonSerializationTest$ClassWithFieldsAndGetter\""));
	}
	
	@Test public void testSerializeEnum() {
		assertEquals("\"JAVA\"", genson.serialize(Player.JAVA));
	}

    @Test public void testSerializeBoxedFloat() {
        assertEquals("2.0", genson.serialize(new Float(2)));
    }

	private Primitives createPrimitives() {
		return new Primitives(1, new Integer(10), 1.00001, new Double(0.00001), "TEXT ...  HEY!",
				true, new Boolean(false));
	}

	@SuppressWarnings("unused")
	private static class ClassWithFieldsAndGetter {
		private final String name;
		@JsonIgnore private String lastName;
		final int age;
		public transient int skipThisField;

		public ClassWithFieldsAndGetter(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public String getNoField() {
			return name + age;
		}
	}
}
