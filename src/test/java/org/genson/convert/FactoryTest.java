package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.genson.ChainedFactory;
import org.genson.Context;
import org.genson.Factory;
import org.genson.GenericType;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.HandleClassMetadata;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FactoryTest {
	private BasicConvertersFactory factory;
	private Genson genson;

	@Before
	public void setUp() {
		final List<? super Converter<?>> converters = new ArrayList<Converter<?>>();
		converters.add(DefaultConverters.StringConverter.instance);
		converters.add(DefaultConverters.BooleanConverter.instance);
		converters.add(DefaultConverters.IntegerConverter.instance);
		converters.add(DefaultConverters.DoubleConverter.instance);
		converters.add(DefaultConverters.LongConverter.instance);
		converters.add(new DefaultConverters.DateConverter());

		final List<Factory<?>> factories = new ArrayList<Factory<?>>();
		factories.add(DefaultConverters.ArrayConverterFactory.instance);
		factories.add(DefaultConverters.CollectionConverterFactory.instance);
		factories.add(DefaultConverters.MapConverterFactory.instance);
		factories.add(DefaultConverters.PrimitiveConverterFactory.instance);
		factories.add(DefaultConverters.UntypedConverterFactory.instance);

		genson = new Genson.Builder() {
			@Override
			protected Factory<Converter<?>> createConverterFactory() {
				factory = new BasicConvertersFactory(converters, factories,
						getBeanDescriptorProvider());

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
		assertEquals(DefaultConverters.MapConverter.class, cm.getClass());
	}

	@Test
	public void testCircularReferencingClasses() {
		Genson genson = new Genson();
		Converter<A> converter = genson.provideConverter(A.class);
		assertNotNull(converter);
	}

	@Test
	public void testUnwrapAnnotations() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().with(new ClassMetadataConverter()).create();
		@SuppressWarnings("unchecked")
		Wrapper<Converter<A>> wrapper = (Wrapper<Converter<A>>) genson.provideConverter(A.class);
		Converter<A> converter = wrapper.unwrap();
		assertTrue(converter instanceof ClassMetadataConverter);
		assertFalse(ClassMetadataConverter.used);
		converter.serialize(null, A.class, null, null);
		assertTrue(ClassMetadataConverter.used);
	}

	@HandleClassMetadata
	static class ClassMetadataConverter implements Converter<A> {
		static boolean used = false;
		
		@Override
		public void serialize(A obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			used = true;
		}

		@Override
		public A deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class A {
		A a;
		B b;
		C c;
	}

	@SuppressWarnings("unused")
	private static class B {
		B b;
		A a;
	}

	@SuppressWarnings("unused")
	private static class C {
		B b;
	}
}
