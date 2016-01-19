package com.owlike.genson.convert;

import java.lang.reflect.Type;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.Wrapper;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

public class LiteralAsObjectConverter<T> implements Converter<T> {
  public static class Factory extends ChainedFactory {
    @Override
    protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
      boolean defaultPrimitiveType = false;
      for (ValueType v : ValueType.values()) {
        if (v.toClass() == type) defaultPrimitiveType = true;
      }

      if (!defaultPrimitiveType &&
            Wrapper.toAnnotatedElement(nextConverter).isAnnotationPresent(HandleClassMetadata.class)) {
        return new LiteralAsObjectConverter(nextConverter);
      } else {
        return nextConverter;
      }
    }
  }

  private final Converter<T> concreteConverter;

  public LiteralAsObjectConverter(Converter<T> concreteConverter) {
    this.concreteConverter = concreteConverter;
  }

  @Override
  public void serialize(T object, ObjectWriter writer, Context ctx) throws Exception {
    writer.beginObject().writeName("value");
    concreteConverter.serialize(object, writer, ctx);
    writer.endObject();
  }

  @Override
  public T deserialize(ObjectReader reader, Context ctx) throws Exception {
    reader.beginObject();
    T instance = null;
    while (reader.hasNext()) {
      reader.next();
      if (reader.name().equals("value")) instance = concreteConverter.deserialize(reader, ctx);
      else throw new IllegalStateException(String.format("Encountered unexpected property named '%s'", reader.name()));
    }
    reader.endObject();
    return instance;
  }
}
