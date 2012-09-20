package com.owlike.genson.functional;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;

import org.junit.Test;

import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;

public class GenericTypesTest {
	private Genson genson = new Genson();

	@Test
	public void testSerializeWithGenericType() throws TransformationException, IOException {
		ContainerClass cc = new ContainerClass();
		cc.urlContainer = new MyGenericClass<URL>();
		cc.urlContainer.tField = new URL("http://www.google.com");

		assertEquals("{\"urlContainer\":{\"tField\":\"http://www.google.com\"}}",
				genson.serialize(cc));
	}

	@Test
	public void testDeserializeWithGenericType() throws TransformationException, IOException {
		ContainerClass cc = genson.deserialize(
				"{\"urlContainer\":{\"tField\":\"http://www.google.com\"}}", ContainerClass.class);
		assertEquals(URL.class, cc.urlContainer.tField.getClass());
		assertEquals(new URL("http://www.google.com"), cc.urlContainer.tField);
	}

	@Test
	public void testDeserializeDeepGenericsUsingGenericType() throws TransformationException, IOException {
		Type type = new GenericType<MyGenericClass<ContainerClass>>() {
		}.getType();
		MyGenericClass<ContainerClass> mgc = genson.deserialize(
				"{\"tField\":{\"urlContainer\":{\"tField\":\"http://www.google.com\"}}}", type);
		assertEquals(ContainerClass.class, mgc.tField.getClass());
		assertEquals(URL.class, mgc.tField.urlContainer.tField.getClass());
		assertEquals(new URL("http://www.google.com"), mgc.tField.urlContainer.tField);
	}

	static class ContainerClass {
		MyGenericClass<URL> urlContainer;
	}

	static class MyGenericClass<T> {
		T tField;
	}
}
