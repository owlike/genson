package org.genson.reflect;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;

import org.genson.TransformationException;
import org.junit.Test;


import static org.junit.Assert.*;

public class BeanDescriptorTest {

	@Test
	public void genericTypeTest() throws TransformationException, IOException {
		BaseBeanDescriptorProvider provider = new BaseBeanDescriptorProvider(
				new BeanMutatorAccessorResolver.ConventionalBeanResolver(),
				new PropertyNameResolver.ConventionalBeanPropertyNameResolver(), true, true);

		BeanDescriptor bd = provider.provideBeanDescriptor(SpecilizedClass.class);
		assertEquals(B.class, getAccessor("t", bd).type);
		assertEquals(B.class,
				((GenericArrayType) getAccessor("tArray", bd).type).getGenericComponentType());
		assertEquals(Double.class, getAccessor("value", bd).type);
	}

	PropertyAccessor getAccessor(String name, BeanDescriptor bd) {
		for (PropertyAccessor a : bd.accessibleProperties)
			if (name.equals(a.name))
				return a;
		return null;
	}

	public static class B {
		public String v;
	}

	public static class ClassWithGenerics<T, E extends Number> {
		public T t;
		public T[] tArray;
		public E value;

		public void setT(T t) {
			this.t = t;
		}
	}

	public static class SpecilizedClass extends ClassWithGenerics<B, Double> {

	}
}
