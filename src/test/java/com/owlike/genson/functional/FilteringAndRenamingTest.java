package com.owlike.genson.functional;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonIgnore;

public class FilteringAndRenamingTest {

	@Test
	public void testSetterWithoutArgs() {
		// bug https://groups.google.com/forum/?fromgroups=#!topic/genson/9rE026i7Vhg
		assertNotNull(new Genson.Builder().exclude("any").create()
				.provideConverter(BeanWithVoidSetter.class));
	}

	static class BeanWithVoidSetter {
		public void setXX() {
		}
		
		public void getXX() {
			
		}
	}

	@Test
	public void testRenameProperty() {
		MyAClass mac = new MyAClass();
		mac.myname = "toto";
		String expectedSuccess = "{\"name\":\"toto\"}";
		String expectedFailure = "{\"myname\":\"toto\"}";

		String json = new Genson.Builder().rename("myname", "name").create().serialize(mac);
		assertEquals(expectedSuccess, json);

		json = new Genson.Builder().rename(String.class, "name").create().serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().rename(int.class, "name").create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().rename("myname", MyAClass.class, "name").create()
				.serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().rename("myname", List.class, "name").create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().rename("myname", MyAClass.class, "name", String.class).create()
				.serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().rename("myname", MyAClass.class, "name", Integer.class)
				.create().serialize(mac);
		assertEquals(expectedFailure, json);
	}

	@Test
	public void testExcludeProperty() {
		MyAClass mac = new MyAClass();
		mac.myname = "toto";
		String expectedSuccess = "{}";
		String expectedFailure = "{\"myname\":\"toto\"}";

		String json = new Genson.Builder().exclude("myname").create().serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().exclude("xxx").create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().exclude(String.class).create().serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().exclude(Integer.class).create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().exclude("myname", MyAClass.class).create().serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().exclude("myname", List.class).create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().exclude("myname", MyAClass.class, String.class).create()
				.serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().exclude("myname", MyAClass.class, Integer.class).create()
				.serialize(mac);
		assertEquals(expectedFailure, json);
	}

	@Test
	public void testIncludeProperty() {
		ClassWithIncludedProperty mac = new ClassWithIncludedProperty();
		mac.prop = "toto";
		String expectedSuccess = "{\"prop\":\"toto\"}";
		String expectedFailure = "{}";

		String json = new Genson.Builder().include("prop").create().serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().include("xxx").create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().include(String.class).create().serialize(mac);
		assertEquals(expectedSuccess, json);

		json = new Genson.Builder().include("prop", ClassWithIncludedProperty.class).create()
				.serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().include("prop", MyAClass.class).create().serialize(mac);
		assertEquals(expectedFailure, json);

		json = new Genson.Builder().include("prop", ClassWithIncludedProperty.class, String.class)
				.create().serialize(mac);
		assertEquals(expectedSuccess, json);
		json = new Genson.Builder().include("prop", ClassWithIncludedProperty.class, Integer.class)
				.create().serialize(mac);
		assertEquals(expectedFailure, json);
	}

	@Test
	public void testExcludePropertyFromSuperClass() {
		AnotherClass mac = new AnotherClass();
		mac.transientLong = 11;
		mac.prop2 = "hi";
		String expectedSuccess = "{\"prop2\":\"hi\"}";

		String json = new Genson.Builder().exclude("transientLong", ClassWithTransient.class)
				.create().serialize(mac);
		assertEquals(expectedSuccess, json);
	}

	static class AnotherClass extends ClassWithTransient {
		public String prop2;
	}

	static class ClassWithIncludedProperty {
		@JsonIgnore
		private String prop;
	}

	static class ClassWithTransient {
		public transient long transientLong;

		public long getTransientLong() {
			return transientLong;
		}
	}

	static class MyAClass {
		private String myname;

		public String getMyname() {
			return myname;
		}

		public void setMyname(String myname) {
			this.myname = myname;
		}
	}
}
