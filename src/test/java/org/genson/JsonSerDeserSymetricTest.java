package org.genson;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.junit.Assert.*;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.Creator;
import org.genson.annotation.JsonProperty;
import org.genson.bean.ComplexObject;
import org.genson.bean.Feed;
import org.genson.bean.MediaContent;
import org.genson.bean.Primitives;
import org.genson.bean.Tweet;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.junit.Test;

public class JsonSerDeserSymetricTest {
	Genson genson = new Genson.Builder().setWithDebugInfoPropertyNameResolver(true).create();

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
		B b2 = (B) genson.deserialize(json, Object.class);
		assertEquals(b.a, b2.a);
		assertEquals(b.b, b2.b);
	}

	@Test
	public void testSerializerAndDeserializeWithView() throws TransformationException, IOException {
		Person p = new Person("Mr");
		p.birthYear = 1986;
		p.name = "eugen";

		Genson genson = new Genson.Builder().setWithBeanViewConverter(true)
				.setWithDebugInfoPropertyNameResolver(true).create();

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
		Genson genson = new Genson.Builder().withConverters(new Converter<URL>() {

			public void serialize(URL obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeValue(obj.toExternalForm());
			}

			public URL deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return new URL(reader.valueAsString());
			}

		}).create();

		String serializedUrl = genson.serialize(new URL("http://www.google.com"));
		assertEquals("\"http://www.google.com\"", serializedUrl);
		assertEquals("http://www.google.com", genson.deserialize(serializedUrl, URL.class)
				.toExternalForm());
	}

	public ObjectMapper getMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
		mapper.setDateFormat(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US));
		return mapper;
	}

	public Genson getGenson() {
		return new Genson.Builder().setDateFormat(
				new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US)).create();
	}

	@Test
	public void testDeserializeSerializeTweets() throws JsonParseException, JsonMappingException,
			IOException, TransformationException {
		ObjectMapper mapper = getMapper();
		Genson genson = getGenson();

		// we first deserialize the original data and ensure that genson deserialized it exactly as
		// jackson
		Tweet[] jacksonTweets = mapper.readValue(ClassLoader.class
				.getResourceAsStream("/TWEETS.json"), Tweet[].class);
		Tweet[] gensonTweets = genson.deserialize(new InputStreamReader(ClassLoader.class
				.getResourceAsStream("/TWEETS.json")), Tweet[].class);
		assertArrayEquals(jacksonTweets, gensonTweets);

		// and then we serialize it and try to deserialize again and match again what was
		// deserialized by jackson
		// this proves that genson serializes and deserializes correctly (except if there is a same
		// error in genson and jackson lol!)
		String tweetsString = genson.serialize(gensonTweets);
		gensonTweets = genson.deserialize(tweetsString, Tweet[].class);
		assertArrayEquals(jacksonTweets, gensonTweets);
	}

	@Test
	public void testDeserializeSerializeReaderShort() throws IOException, TransformationException {
		ObjectMapper mapper = getMapper();
		Genson genson = getGenson();

		// same test as before...
		Feed jacksonShortFeed = mapper.readValue(ClassLoader.class
				.getResourceAsStream("/READER_SHORT.json"), Feed.class);
		Feed gensonShortFeed = genson.deserialize(new InputStreamReader(ClassLoader.class
				.getResourceAsStream("/READER_SHORT.json")), Feed.class);
		assertEquals(jacksonShortFeed, gensonShortFeed);
		String shortFeedString = genson.serialize(gensonShortFeed);
		gensonShortFeed = genson.deserialize(shortFeedString, Feed.class);
		assertEquals(jacksonShortFeed, gensonShortFeed);

	}

	@Test
	public void testDeserializeSerializeReaderLong() throws IOException, TransformationException {
		ObjectMapper mapper = getMapper();
		Genson genson = getGenson();

		// and again for the long reader data...
		Feed jacksonLongFeed = mapper.readValue(ClassLoader.class
				.getResourceAsStream("/READER_LONG.json"), Feed.class);
		Feed gensonLongFeed = genson.deserialize(new InputStreamReader(ClassLoader.class
				.getResourceAsStream("/READER_LONG.json")), Feed.class);
		assertEquals(jacksonLongFeed, gensonLongFeed);
		String longFeedString = genson.serialize(gensonLongFeed);
		gensonLongFeed = genson.deserialize(longFeedString, Feed.class);
		assertEquals(jacksonLongFeed, gensonLongFeed);
	}

	@Test
	public void testWithCustomObjectConverter() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().withConverters(new Converter<Point>() {

			public void serialize(Point obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.beginObject().writeName("x").writeValue(obj.x).writeName("y").writeValue(
						obj.y).endObject();
			}

			public Point deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				Point p = new Point();
				reader.beginObject();
				for (; reader.hasNext();) {
					reader.next();
					String name = reader.name();
					if ("x".equals(name))
						p.x = reader.valueAsInt();
					else if ("y".equals(name))
						p.y = reader.valueAsInt();
					else {
						// lets skip it
						reader.skipValue();
					}
				}
				reader.endObject();
				return p;
			}

		}).create();

		Point p = new Point(1, 2);
		String serializedPoint = genson.serialize(p);
		Point q = genson.deserialize(serializedPoint, Point.class);
		assertEquals(p.x, q.x);
		assertEquals(p.y, q.y);
	}

	@Test
	public void testSerializeDeserializeMediaContent() throws JsonParseException,
			JsonMappingException, IOException, TransformationException {
		ObjectMapper mapper = new ObjectMapper();
		Genson genson = new Genson();
		MediaContent jacksonContent = mapper.readValue(ClassLoader.class
				.getResourceAsStream("/MEDIA_CONTENT.json"), MediaContent.class);
		MediaContent gensonContent = genson.deserialize(new InputStreamReader(ClassLoader.class
				.getResourceAsStream("/MEDIA_CONTENT.json")), MediaContent.class);
		assertEquals(jacksonContent, gensonContent);
		String json = genson.serialize(gensonContent);
		gensonContent = genson.deserialize(json, MediaContent.class);
		assertEquals(jacksonContent, gensonContent);
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

		public @JsonProperty(value = "name")
		String getNameOf(Person p) {
			return p.name;
		}

		public int getAge(Person p) {
			return GregorianCalendar.getInstance().get(Calendar.YEAR) - p.birthYear;
		}

		public void setName(String name, Person p) {
			p.name = name;
		}

		@JsonProperty(value = "age")
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
