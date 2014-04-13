package com.owlike.genson.functional;

import java.awt.*;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.owlike.genson.*;
import com.owlike.genson.stream.JsonWriter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultConvertersTest {
	private Genson genson = new Genson();

    @Test public void testReadWriteByteAsInt() {
        Genson genson = new Genson.Builder().setWithDebugInfoPropertyNameResolver(true).useByteAsInt(true).create();
        PojoWithByteArray expected = new PojoWithByteArray(5, 777.777, "ABCD".getBytes());
        String json = genson.serialize(expected);
        PojoWithByteArray actual = genson.deserialize(json, PojoWithByteArray.class);

        assertEquals("{\"b\":[65,66,67,68],\"f\":777.777,\"i\":5}", json);
        assertArrayEquals(expected.b, actual.b);
        assertEquals(expected.f, actual.f, 1e-21);
        assertEquals(expected.i, actual.i);
    }

    @Test public void testPojoWithBytes() {
        Genson genson = new Genson.Builder().setWithDebugInfoPropertyNameResolver(true).create();
        PojoWithByteArray expected = new PojoWithByteArray(5, 777.777, "ABCD".getBytes());
        String json = genson.serialize(expected);
        PojoWithByteArray actual = genson.deserialize(json, PojoWithByteArray.class);

        assertEquals("{\"b\":\"QUJDRA==\",\"f\":777.777,\"i\":5}", json);
        assertArrayEquals(expected.b, actual.b);
        assertEquals(expected.f, actual.f, 1e-21);
        assertEquals(expected.i, actual.i);
    }
	
	@Test public void testByteArray() throws UnsupportedEncodingException {
		byte[] byteArray = "hey convert me to bytes".getBytes("UTF-8");
		String json = genson.serialize(byteArray);
		assertArrayEquals(byteArray, genson.deserialize(json, byte[].class));
	}
	
	@Test public void testEnumSet() {
		EnumSet<Color> foo = EnumSet.of(Color.blue, Color.red);
        String json = genson.serialize(foo);
        EnumSet<Color> bar = genson.deserialize(json, new GenericType<EnumSet<Color>>() {
        });
        assertTrue(bar.contains(Color.blue));
        assertTrue(bar.contains(Color.red));
	}
	
	@Test
	public void testClassMetadataOnceWhenUsedWithRuntimeType() {
		Genson genson = new Genson.Builder().setUseRuntimeTypeForSerialization(true)
				.addAlias("subBean", SubBean.class).setWithClassMetadata(true).create();
		RootBean rootBean = new SubBean();
		rootBean.bean = new SubBean();
		assertEquals("{\"@class\":\"subBean\",\"bean\":{\"@class\":\"subBean\",\"bean\":null}}", genson.serialize(rootBean));
	}

	@Test
	public void testMapWithPrimitiveKeys() {
		Map<Long, String> expected = new HashMap<Long, String>();
		expected.put(5L, "hey");
		String json = genson.serialize(expected);
		// due to type erasure we consider keys as strings
		@SuppressWarnings("rawtypes")
		Map map = genson.deserialize(json, Map.class);
		assertNull(map.get(5L));
		assertNotNull(map.get("5"));

		// when map type is defined we deserialize to expected primitive types
		map = genson.deserialize(json, new GenericType<Map<Long, String>>() {
		});
		assertEquals(expected.get(5L), map.get(5L));
	}

	@Test
	public void testPropertiesConverter() {
		Properties props = new Properties();
		props.put("key", "value");
		String json = genson.serialize(props);
		assertEquals("value", genson.deserialize(json, Properties.class).get("key"));
	}

	@Test
	public void testComplexMapConverter() {
		Map<UUID, List<UUID>> expected = new HashMap<UUID, List<UUID>>();
		expected.put(UUID.randomUUID(), Arrays.asList(UUID.randomUUID(), UUID.randomUUID()));
		expected.put(UUID.randomUUID(), Arrays.asList(UUID.randomUUID()));
		expected.put(null, null);
		String json = genson.serialize(expected, new GenericType<Map<UUID, List<UUID>>>() {
		});
		assertEquals(expected, genson.deserialize(json, new GenericType<Map<UUID, List<UUID>>>() {
        }));
	}

	@Test
	public void testUUIDConverter() {
		UUID uuid = UUID.randomUUID();
		String json = genson.serialize(uuid);
		assertEquals(uuid, genson.deserialize(json, UUID.class));
	}

	@Test
	public void testDateConverter() {
		Genson genson = new Genson.Builder().setDateFormat(
				new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.FRENCH)).create();
		Date date = new Date();
		String json = genson.serialize(date);
		Date dateDeserialized = genson.deserialize(json, Date.class);
		assertEquals(date.toString(), dateDeserialized.toString());

	}

	@Test
	public void testCalendarConverter() {
		Genson genson = new Genson.Builder().useTimeInMillis(true).create();
		Calendar cal = Calendar.getInstance();
		String json = genson.serialize(cal);
		Calendar cal2 = genson.deserialize(json, Calendar.class);
		assertEquals(cal.getTime(), cal2.getTime());
	}

	public static enum Color {
		blue, red;
	}
	
	public static class RootBean {
		public RootBean bean;
	}

	public static class SubBean extends RootBean {
	}

    public static class PojoWithByteArray {
        int i;
        double f;
        byte[] b;

        PojoWithByteArray(int i, double f, byte[] b) {
            this.i = i;
            this.f = f;
            this.b = b;
        }
    }
}
