package com.owlike.genson;

import org.junit.Test;

import com.owlike.genson.convert.BasicConvertersFactory;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import static org.junit.Assert.*;

public class GensonBuilderTest {
  @Test
  public void testCustomConverterRegistration() {
    final Converter<Number> dummyConverter = new Converter<Number>() {
      public void serialize(Number object, ObjectWriter writer, Context ctx) {
      }

      public Number deserialize(ObjectReader reader, Context ctx) {
        return null;
      }
    };
    Genson genson = new GensonBuilder() {
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
