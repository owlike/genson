package com.owlike.genson;

import java.io.IOException;

import org.junit.Test;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.convert.BasicConvertersFactory;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import static org.junit.Assert.*;

public class GensonBuilderTest {
	@Test
	public void testCustomConverterRegistration() {
		final Converter<Number> dummyConverter = new Converter<Number>() {
			public void serialize(Number object, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
			}

			public Number deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return null;
			}
		};
		Genson genson = new Genson.Builder() {
			@Override
			protected Factory<Converter<?>> createConverterFactory() {
				assertEquals(dummyConverter, getSerializersMap().get(Number.class));
				assertEquals(dummyConverter, getSerializersMap().get(Long.class));
				assertEquals(dummyConverter, getSerializersMap().get(Double.class));
				assertEquals(dummyConverter, getDeserializersMap().get(Number.class));
				assertEquals(dummyConverter, getDeserializersMap().get(Long.class));
				assertEquals(dummyConverter, getDeserializersMap().get(Double.class));
				return new BasicConvertersFactory(getSerializersMap(), getDeserializersMap(), getFactories(), getBeanDescriptorProvider());
			}
		}.withConverters(dummyConverter).withConverter(dummyConverter, Long.class).withConverter(dummyConverter, new GenericType<Double>() {
		}).create();
		
		assertEquals(dummyConverter, genson.provideConverter(Number.class));
		assertEquals(dummyConverter, genson.provideConverter(Long.class));
		assertEquals(dummyConverter, genson.provideConverter(Double.class));
	}
}
