package org.genson;

import java.io.IOException;

import org.genson.convert.BasicConvertersFactory;
import org.genson.convert.Converter;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.junit.Test;
import static org.junit.Assert.*;

public class GensonBuilderTest {
	@Test
	public void testCustomConverterRegistration() {
		final Converter<Number> dummyConverter = new Converter<Number>() {
			@Override
			public void serialize(Number object, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
			}

			@Override
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
