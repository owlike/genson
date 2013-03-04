package com.owlike.genson.convert;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.Wrapper;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.convert.BasicConvertersFactory;
import com.owlike.genson.convert.BeanViewConverter;
import com.owlike.genson.convert.ChainedFactory;
import com.owlike.genson.convert.DefaultConverters;
import com.owlike.genson.convert.NullConverter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import static org.junit.Assert.*;

public class FactoryTest {
	private BasicConvertersFactory factory;
	private Genson genson;

	@SuppressWarnings("serial")
	public static class ParameterizedSuperType extends HashMap<Object, String> {
	}
	
	@Test public void testConstructionForTypeWithParameters() {
		assertNotNull(new Genson().provideConverter(ParameterizedSuperType.class));
	}
	
	@Before
	public void setUp() {

		genson = new Genson.Builder() {
			@Override
			protected Factory<Converter<?>> createConverterFactory() {
				factory = new BasicConvertersFactory(getSerializersMap(), getDeserializersMap(),
						getFactories(), getBeanDescriptorProvider());

				ChainedFactory chain = new NullConverter.NullConverterFactory();
				chain.withNext(
						new BeanViewConverter.BeanViewConverterFactory(
								getBeanViewDescriptorProvider())).withNext(factory);

				return chain;
			}
		}.create();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBasicConvertersFactory() {
		Converter<Integer> ci = (Converter<Integer>) factory.create(Integer.class, genson);
		assertEquals(DefaultConverters.IntegerConverter.class, ci.getClass());

		Converter<Boolean> cb = (Converter<Boolean>) factory.create(Boolean.class, genson);
		assertEquals(DefaultConverters.BooleanConverter.class, cb.getClass());

		assertNotNull(factory.create(int.class, genson));

		Converter<Long[]> cal = (Converter<Long[]>) factory.create(Long[].class, genson);
		assertEquals(DefaultConverters.ArrayConverter.class, cal.getClass());

		Converter<Object[]> cao = (Converter<Object[]>) factory.create(Object[].class, genson);
		assertEquals(DefaultConverters.ArrayConverter.class, cao.getClass());

		Converter<List<?>> converter = (Converter<List<?>>) factory.create(List.class, genson);
		assertEquals(DefaultConverters.CollectionConverter.class, converter.getClass());

		Converter<Map<String, Object>> cm = (Converter<Map<String, Object>>) factory.create(
				new GenericType<Map<String, Object>>() {
				}.getType(), genson);
		assertEquals(DefaultConverters.HashMapConverter.class, cm.getClass());
	}

	@Test
	public void testCircularReferencingClasses() {
		Genson genson = new Genson();
		Converter<A> converter = genson.provideConverter(A.class);
		assertNotNull(converter);
	}

	@Test
	public void testUnwrapAnnotations() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().withConverters(new ClassMetadataConverter()).create();
		@SuppressWarnings({ "unchecked", "rawtypes" }) // argh its ugly with those warnings...
		Wrapper<Converter<A>> wrapper = (Wrapper) genson.provideConverter(A.class);
		Converter<A> converter = wrapper.unwrap();
		assertTrue(converter instanceof ClassMetadataConverter);
		assertFalse(ClassMetadataConverter.used);
		converter.serialize(new A(), null, null);
		assertTrue(ClassMetadataConverter.used);
	}

	@HandleClassMetadata
	static class ClassMetadataConverter implements Converter<A> {
		static boolean used = false;

		public void serialize(A obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			used = true;
		}

		public A deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return null;
		}
	}

	static class A {
		A a;
		B b;
		C c;
	}

	static class B {
		B b;
		A a;
	}

	static class C {
		B b;
	}
}
