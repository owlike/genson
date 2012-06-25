package org.genson;

import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.Creator;
import org.genson.annotation.JsonProperty;
import org.genson.bean.ComplexObject;
import org.genson.bean.Primitives;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.junit.Test;

public class JsonSerDeserSymetricTest {
	Genson genson = new Genson();

	@Test
	public void testJsonComplexObjectSerDeser() throws TransformationException, IOException {
		ComplexObject coo = new ComplexObject(createPrimitives(), Arrays.asList(createPrimitives(),
				createPrimitives()), new Primitives[] { createPrimitives(), createPrimitives() });

		String json = genson.serialize(coo);
		ComplexObject co = genson.deserialize(json, ComplexObject.class);
		ComplexObject.assertCompareComplexObjects(co, coo);
	}

	@Test
	public void testPrimitiveSerDeser() throws TransformationException, IOException {
		int a = 54000048;
		int b = genson.deserialize(genson.serialize(a), int.class);
		assertEquals(a, b);

		double c = 64800486.000649463;
		double d = genson.deserialize(genson.serialize(c), double.class);
		assertTrue(c == d);

		String s = "hey you!";
		String s2 = genson.deserialize(genson.serialize(s), String.class);
		assertEquals(s, s2);
	}

	@Test
	public void testAbstractType() throws TransformationException, IOException {
		B b = new B();
		b.a = "aa";
		b.b = "bb";
		Genson genson = new Genson.Builder().setWithClassMetadata(true).create();
		String json = genson.serialize(b);
		B b2 = (B) genson.deserialize(json, A.class);
		assertEquals(b.a, b2.a);
		assertEquals(b.b, b2.b);
	}

	@Test
	public void testSerializerAndDeserializeWithView() throws TransformationException, IOException {
		Person p = new Person("Mr");
		p.birthYear = 1986;
		p.name = "eugen";

		String json = genson.serialize(p, ViewOfPerson.class);
		assertEquals("{\"age\":" + new ViewOfPerson().getAge(p) + ",\"gender\":\"M\",\"name\":\""
				+ p.name + "\"}", json);
		Person me = genson.deserialize(json, Person.class, ViewOfPerson.class);
		assertEquals(p.civility, me.civility);
		assertEquals(p.name, me.name);
		assertEquals(p.birthYear, me.birthYear);
	}

	@Test
	public void testWithUrlConverter() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().with(new Converter<URL>() {

			@Override
			public void serialize(URL obj, Type type, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeValue(obj.toExternalForm());
			}

			@Override
			public URL deserialize(Type type, ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return new URL(reader.valueAsString());
			}

		}).create();

		String serializedUrl = genson.serialize(new URL("http://www.google.com"));
		assertEquals("\"http://www.google.com\"", serializedUrl);
		assertEquals("http://www.google.com", genson.deserialize(serializedUrl, URL.class)
				.toExternalForm());
	}

	@Test
	public void testWithCustomObjectConverter() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().with(new Converter<Point>() {

			@Override
			public void serialize(Point obj, Type type, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.beginObject().writeName("x").writeValue(obj.x).writeName("y")
						.writeValue(obj.y).endObject();
			}

			@Override
			public Point deserialize(Type type, ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				Point p = new Point();
				reader.beginObject();
				for(; reader.hasNext();) {
					reader.next();
					String name = reader.name();
    				if ("x".equals(name)) p.x = reader.valueAsInt();
    				else if ("y".equals(name)) p.y = reader.valueAsInt();
    				else {
    					// lets skip it
    					reader.skipValue();
    				}
				}
				reader.endObject();
				return p;
			}

		}).create();
		
		Point p = new Point(1,2);
		String serializedPoint = genson.serialize(p);
		Point q = genson.deserialize(serializedPoint, Point.class);
		assertEquals(p.x, q.x);
		assertEquals(p.y, q.y);
	}

	public static class Person {
		String civility;
		String name;
		int birthYear;

		Person(String civility) {
			this.civility = civility;
		}
	}

	public static class ViewOfPerson implements BeanView<Person> {
		public ViewOfPerson() {
		}

		static boolean usedCtr = false;

		@Creator
		public static Person createNewPerson(String gender) {
			usedCtr = true;
			String civility = "M".equalsIgnoreCase(gender) ? "Mr"
					: "F".equalsIgnoreCase(gender) ? "UNKNOWN" : "";
			return new Person(civility);
		}

		public String getGender(Person p) {
			return "Mr".equalsIgnoreCase(p.civility) ? "M"
					: "Mrs".equalsIgnoreCase(p.civility) ? "F" : "UNKNOWN";
		}

		public @JsonProperty(name = "name")
		String getNameOf(Person p) {
			return p.name;
		}

		public int getAge(Person p) {
			return GregorianCalendar.getInstance().get(Calendar.YEAR) - p.birthYear;
		}

		public void setName(String name, Person p) {
			p.name = name;
		}

		@JsonProperty(name = "age")
		public void setBirthYear(int personBirthYear, Person p) {
			p.birthYear = GregorianCalendar.getInstance().get(Calendar.YEAR) - personBirthYear;
		}
	}

	public static class A {
		String a;
	}

	public static class B extends A {
		String b;
	}

	private Primitives createPrimitives() {
		return new Primitives(1, new Integer(10), 1.00001, new Double(0.00001), "TEXT ...  HEY!",
				true, new Boolean(false));
	}
}
