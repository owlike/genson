package com.owlike.genson.reflect;

import com.owlike.genson.GensonBuilder;
import com.owlike.genson.annotation.JsonCreator;
import org.junit.Test;

import com.owlike.genson.BeanView;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonProperty;

import static org.junit.Assert.*;

public class BeanViewTest {
	Genson genson = new GensonBuilder()
			.useBeanViews(true)
			.useConstructorWithArguments(true)
			.set(new BeanMutatorAccessorResolver.StandardMutaAccessorResolver(VisibilityFilter.ALL,
					VisibilityFilter.PACKAGE_PUBLIC, VisibilityFilter.PACKAGE_PUBLIC)).create();
	
	@Test
	public void testSerializeWithInheritedView() {
		MyClass c = new MyClass();
		c.name = "toto";

		String json = genson.serialize(c, ExtendedView.class);
		assertEquals("{\"forName\":\"his name is : " + c.name + "\",\"value\":1}", json);

		json = genson.serialize(c, ExtendedBeanView2Class.class);
		assertEquals(json, "{\"value\":2}");

		json = genson.serialize(c, ConcreteView.class);
		assertEquals(json, "{\"value\":3}");
	}

	@Test
	public void testDeserializeWithInheritedView() {
		String json = "{\"forName\": \"titi\", \"value\": 123}";
		MyClass mc = genson.deserialize(json, MyClass.class, ExtendedView.class);
		assertTrue(ExtendedView.usedCtr);
		assertFalse(ExtendedView.usedForNameMethod);
		assertEquals(ExtendedView.val, 123);
		assertEquals("titi", mc.name);
	}

	public static class MyClass {
		public String name;
	}

	public static class MyClassView implements BeanView<MyClass> {
		static boolean usedCtr = false;
		static boolean usedForNameMethod = false;
		static int val;

		@JsonCreator
		public static MyClass create(String forName, @JsonProperty(value = "value") Integer theValue) {
			usedCtr = true;
			MyClass mc = new MyClass();
			mc.name = forName;
			val = theValue;
			return mc;
		}

		public void setForName(String name, MyClass target) {
			target.name = name;
			usedForNameMethod = true;
		}

		@JsonProperty(value = "forName")
		public String getHisName(MyClass b) {
			return "his name is : " + b.name;
		}
	}

	public static class ExtendedView extends MyClassView {
		public int getValue(MyClass b) {
			return 1;
		}
	}

	public static interface ExtendedBeanView2<T extends MyClass> extends BeanView<T> {
	}

	public static class ExtendedBeanView2Class implements ExtendedBeanView2<MyClass> {
		public int getValue(MyClass t) {
			return 2;
		}
	}

	public static class AbstractView<T> implements BeanView<T> {
		public int getValue(T t) {
			return 3;
		}
	}

	public static class ConcreteView extends AbstractView<MyClass> {

	}
}
