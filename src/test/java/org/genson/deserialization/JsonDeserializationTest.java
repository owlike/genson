package org.genson.deserialization;

import static org.junit.Assert.*;

import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.JsonProperty;
import org.genson.bean.ComplexObject;
import org.genson.bean.Primitives;
import org.genson.reflect.BeanDescriptor;
import org.genson.stream.JsonReader;
import org.junit.Before;
import org.junit.Test;

public class JsonDeserializationTest {
	Genson genson;

	@Before
	public void init() throws TransformationException, IOException {
		genson = new Genson();
	}

	@Test
	public void testJsonEmptyObject() throws TransformationException, IOException {
		String src = "{}";
		Primitives p = genson.deserialize(src, Primitives.class);
		assertNull(p.getText());
	}

	@Test
	public void testJsonEmptyArray() throws TransformationException, IOException {
		String src = "[]";
		Integer[] p = genson.deserialize(src, Integer[].class);
		assertTrue(p.length == 0);
	}

	@Test
	public void testJsonComplexObjectEmpty() throws TransformationException, IOException {
		String src = "{\"primitives\":null,\"listOfPrimitives\":[], \"arrayOfPrimitives\": null}";
		ComplexObject co = genson.deserialize(src, ComplexObject.class);

		assertNull(co.getPrimitives());
		assertTrue(co.getListOfPrimitives().size() == 0);
		assertNull(co.getArrayOfPrimitives());
	}
	
	@Test public void testDeserializeEmptyJson() throws TransformationException, IOException {
		Integer i = genson.deserialize("\"\"", Integer.class);
		assertNull(i);
		i = genson.deserialize("", Integer.class);
		assertNull(i);
		i = genson.deserialize("null", Integer.class);
		assertNull(i);
		
		int[] arr = genson.deserialize("", int[].class);
		assertNull(arr);
		arr = genson.deserialize("null", int[].class);
		assertNull(arr);
		arr = genson.deserialize("[]", int[].class);
		assertNotNull(arr);
		
		Primitives p = genson.deserialize("", Primitives.class);
		assertNull(p);
		p = genson.deserialize("null", Primitives.class);
		assertNull(p);
		p = genson.deserialize("{}", Primitives.class);
		assertNotNull(p);
	}

	@Test
	public void testJsonNumbersLimit() throws TransformationException, IOException {
		String src = "[" + String.valueOf(Long.MAX_VALUE) + "," + String.valueOf(Long.MIN_VALUE) + "]";
		long[] arr = genson.deserialize(src, long[].class);
		assertTrue(Long.MAX_VALUE == arr[0]);
		assertTrue(Long.MIN_VALUE == arr[1]);

		src = "[" + String.valueOf(Double.MAX_VALUE) + "," + String.valueOf(Double.MIN_VALUE) + "]";
		double[] arrD = genson.deserialize(src, double[].class);
		assertTrue(Double.MAX_VALUE == arrD[0]);
		assertTrue(Double.MIN_VALUE == arrD[1]);
	}

	@Test
	public void testJsonPrimitivesObject() throws TransformationException, IOException {
		String src = "{\"intPrimitive\":1, \"integerObject\":7, \"doublePrimitive\":1.01,"
				+ "\"doubleObject\":2.003,\"text\": \"HEY...YA!\", "
				+ "\"booleanPrimitive\":true,\"booleanObject\":false}";
		Primitives p = genson.deserialize(src, Primitives.class);
		assertEquals(p.getIntPrimitive(), 1);
		assertEquals(p.getIntegerObject(), new Integer(7));
		assertEquals(p.getDoublePrimitive(), 1.01, 0);
		assertEquals(p.getDoubleObject(), new Double(2.003));
		assertEquals(p.getText(), "HEY...YA!");
		assertEquals(p.isBooleanPrimitive(), true);
		assertEquals(p.isBooleanObject(), Boolean.FALSE);
	}

	@Test
	public void testJsonDoubleArray() throws TransformationException, IOException {
		String src = "[5,      0.006, 9.0E-11 ]";
		double[] array = genson.deserialize(src, double[].class);
		assertEquals(array[0], 5, 0);
		assertEquals(array[1], 0.006, 0);
		assertEquals(array[2], 0.00000000009, 0);
	}

	@Test
	public void testJsonComplexObject() throws TransformationException, IOException {
		ComplexObject coo = new ComplexObject(createPrimitives(), Arrays.asList(createPrimitives(),
				createPrimitives()), new Primitives[] { createPrimitives(), createPrimitives() });
		ComplexObject co = genson.deserialize(coo.jsonString(), ComplexObject.class);
		ComplexObject.assertCompareComplexObjects(co, coo);
	}

	@Test
	public void testJsonComplexObjectSkipValue() throws TransformationException, IOException {
		ComplexObject coo = new ComplexObject(createPrimitives(), Arrays.asList(createPrimitives(),
				createPrimitives(), createPrimitives(), createPrimitives(), createPrimitives(),
				createPrimitives()), new Primitives[] { createPrimitives(), createPrimitives() });

		DummyWithFieldToSkip dummy = new DummyWithFieldToSkip(coo, coo, createPrimitives(),
				Arrays.asList(coo));
		DummyWithFieldToSkip dummy2 = genson.deserialize(dummy.jsonString(),
				DummyWithFieldToSkip.class);

		ComplexObject.assertCompareComplexObjects(dummy.getO1(), dummy2.getO1());
		ComplexObject.assertCompareComplexObjects(dummy.getO2(), dummy2.getO2());
		Primitives.assertComparePrimitives(dummy.getP(), dummy2.getP());
	}

	@Test
	public void testJsonToBeanWithConstructor() throws TransformationException, IOException {
		String json = "{\"other\":{\"name\":\"TITI\", \"age\": 13}, \"name\":\"TOTO\", \"age\":26}";
		BeanWithConstructor bean = genson.deserialize(json, BeanWithConstructor.class);
		assertEquals(bean.age, 26);
		assertEquals(bean.name, "TOTO");
		assertEquals(bean.other.age, 13);
		assertEquals(bean.other.name, "TITI");
	}

	public static class BeanWithConstructor {
		final String name;
		final int age;
		final BeanWithConstructor other;

		public BeanWithConstructor(@JsonProperty(name = "name") String name, @JsonProperty(name = "age") int age,
				@JsonProperty(name = "other") BeanWithConstructor other) {
			this.name = name;
			this.age = age;
			this.other = other;
		}

		public void setName(String name) {
			fail();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testJsonToUntypedList() throws TransformationException, IOException {
		String src = "[1, 1.1, \"aa\", true, false]";
		List<Object> l = genson.deserialize(src, List.class);
		assertArrayEquals(new Object[] { 1, 1.1, "aa", true, false },
				l.toArray(new Object[l.size()]));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testContentDrivenDeserialization() throws TransformationException, IOException {
		String src = "{\"list\":[1, 2.3, 5, null]}";
		TypeVariableList<Number> tvl = genson.deserialize(src, TypeVariableList.class);
		assertArrayEquals(tvl.list.toArray(new Number[tvl.list.size()]), new Number[] { 1, 2.3, 5,
				null });

		// doit echouer du a la chaine et que list est de type <E extends Number>
		src = "{\"list\":[1, 2.3, 5, \"a\"]}";
		try {
			tvl = genson.deserialize(src, TypeVariableList.class);
			fail();
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUntypedDeserializationToMap() throws TransformationException, IOException {
		String src = "{\"key1\": 1, \"key2\": 1.2, \"key3\": true, \"key4\": \"string\", \"key5\": null, \"list\":[1,2.0005,3]}";
		Map<String, Object> map = genson.deserialize(src, Map.class);
		assertEquals(map.get("key1"), 1);
		assertEquals(map.get("key2"), 1.2);
		assertEquals(map.get("key3"), true);
		assertEquals(map.get("key4"), "string");
		assertNull(map.get("key5"));
		List<Number> list = (List<Number>) map.get("list");
		assertArrayEquals(list.toArray(new Number[list.size()]), new Number[] { 1, 2.0005, 3 });
	}

	@Test
	public void testDeserializeWithConstructorAndFieldsAndSetters() throws TransformationException, IOException {
		String json = "{\"p0\":0,\"p1\":1,\"p2\":2,\"shouldSkipIt\":55}";
		ClassWithConstructorFieldsAndGetters c = genson.deserialize(json,
				ClassWithConstructorFieldsAndGetters.class);
		assertEquals(c.constructorCalled, 2);
		assertEquals(c.p0, new Integer(0));
		assertEquals(c.p1, new Integer(1));
		assertEquals(c.p2, new Integer(2));
	}

	@Test
	public void testDeserializeWithConstructorAndMissingFields() throws TransformationException, IOException {
		String json = "{\"p0\":0,\"p1\":1}";
		ClassWithConstructorFieldsAndGetters c = genson.deserialize(json,
				ClassWithConstructorFieldsAndGetters.class);
		assertEquals(c.p0, new Integer(0));
		assertEquals(c.p1, new Integer(1));
		assertEquals(c.p2, null);
		assertEquals(c.constructorCalled, 1);
	}

	@Test
	public void testDeserializeWithConstructorMixedAnnotation() throws TransformationException, IOException {
		String json = "{\"p0\":0,\"p1\":1,\"p2\":2,\"shouldSkipIt\":55,   \"nameInJson\":\"125\"}";
		ClassWithConstructorFieldsAndGetters c = genson.deserialize(json,
				ClassWithConstructorFieldsAndGetters.class);
		assertEquals(3, c.constructorCalled);
		assertEquals(c.p0, new Integer(0));
		assertEquals(c.p1, new Integer(1));
		assertEquals(c.p2, new Integer(2));
		assertEquals(c.hidden, new Integer(125));
	}

	@Test
	public void testDeserializeJsonWithClassAlias() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().addAlias("rect", Rectangle.class).create();
		Shape p = genson.deserialize("{\"@class\":\"rect\"}", Shape.class);
		assertTrue(p instanceof Rectangle);
		p = genson.deserialize("{\"@class\":\"java.awt.Rectangle\"}", Shape.class);
		assertTrue(p instanceof Rectangle);
	}

	public void testDeserealizeIntoExistingBean() throws IOException, TransformationException {
		BeanDescriptor desc = genson.getBeanDescriptorProvider().provideBeanDescriptor(ClassWithConstructorFieldsAndGetters.class);
		ClassWithConstructorFieldsAndGetters c = new ClassWithConstructorFieldsAndGetters(1) {
			@Override
			public void setP2(Integer p2) {
				this.p2 = p2;
			}
		};
		String json = "{\"p0\":0,\"p1\":1,\"p2\":2,\"shouldSkipIt\":55,   \"nameInJson\":\"125\"}";
		desc.deserialize(c, new JsonReader(json), new Context(genson));
		
		assertEquals(c.p0, new Integer(0));
		assertEquals(c.p1, new Integer(1));
		assertEquals(c.p2, new Integer(2));
	}
	
	
	@SuppressWarnings("unused")
	private static class ClassWithConstructorFieldsAndGetters {
		final Integer p0;
		private Integer p1;
		protected Integer p2;
		transient final Integer hidden;
		// should not be serialized
		public final transient int constructorCalled;

		public ClassWithConstructorFieldsAndGetters(Integer p0, Integer p2) {
			constructorCalled = 2;
			this.p0 = p0;
			this.p2 = p2;
			this.hidden = null;
		}

		public ClassWithConstructorFieldsAndGetters(Integer p0) {
			constructorCalled = 1;
			this.p0 = p0;
			this.hidden = null;
		}

		public ClassWithConstructorFieldsAndGetters(Integer p0, Integer p2,
				@JsonProperty(name = "nameInJson") String dontCareOfTheName) {
			constructorCalled = 3;
			this.p0 = p0;
			this.p2 = p2;
			this.hidden = Integer.parseInt(dontCareOfTheName);
		}

		public void setP1(Integer p1) {
			this.p1 = p1;
		}

		// use the constructor
		public void setP2(Integer p2) {
			fail();
			this.p2 = p2;
		}
	}

	@SuppressWarnings("unused")
	private static class TypeVariableList<E extends Number> {
		List<E> list;

		public TypeVariableList() {
		}

		public List<E> getList() {
			return list;
		}

		public void setList(List<E> list) {
			this.list = list;
		}

	}

	@SuppressWarnings("unused")
	private static class DummyWithFieldToSkip {
		ComplexObject o1;
		ComplexObject o2;
		Primitives p;
		List<ComplexObject> list;

		public DummyWithFieldToSkip() {
		}

		public DummyWithFieldToSkip(ComplexObject o1, ComplexObject o2, Primitives p,
				List<ComplexObject> list) {
			super();
			this.o1 = o1;
			this.o2 = o2;
			this.p = p;
			this.list = list;
		}

		public String jsonString() {
			StringBuilder sb = new StringBuilder();

			sb.append("{\"list\":[");
			if (list != null) {
				for (int i = 0; i < list.size(); i++)
					sb.append(list.get(i).jsonString()).append(',');
				sb.append(list.get(list.size() - 1).jsonString());
			}
			sb.append("],\"o1\":").append(o1.jsonString()).append(",\"o2\":")
					.append(o2.jsonString()).append(",\"ooooooSkipMe\":").append(o2.jsonString())
					.append(",\"p\":").append(p.jsonString()).append('}');

			return sb.toString();
		}

		public ComplexObject getO1() {
			return o1;
		}

		public void setO1(ComplexObject o1) {
			this.o1 = o1;
		}

		public ComplexObject getO2() {
			return o2;
		}

		public void setO2(ComplexObject o2) {
			this.o2 = o2;
		}

		public Primitives getP() {
			return p;
		}

		public void setP(Primitives p) {
			this.p = p;
		}
	}

	private Primitives createPrimitives() {
		return new Primitives(1, new Integer(10), 1.00001, new Double(0.00001), "TEXT ...  HEY!",
				true, new Boolean(false));
	}
}
