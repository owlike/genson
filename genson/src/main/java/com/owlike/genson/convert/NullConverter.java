package com.owlike.genson.convert;

import java.lang.reflect.Type;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.Wrapper;
import com.owlike.genson.annotation.HandleNull;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

/**
 * The default implementation handles null values by returning null during deserialization and
 * calling writer.writeNull() during serialization.
 * <p/>
 * You can also change the way null values are handled by registering your own Null Converter
 * {@link com.owlike.genson.Genson.Builder#setNullConverter(com.owlike.genson.Converter)
 * Genson.Builder.setNullConverter(org.genson.convert.Converter)}.
 *
 * @author eugen
 */
public class NullConverter implements Converter<Object> {
  public static class NullConverterFactory extends ChainedFactory {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
      return Wrapper.toAnnotatedElement(nextConverter).isAnnotationPresent(HandleNull.class) ? nextConverter
        : new NullConverterWrapper(genson.getNullConverter(), nextConverter);
    }
  }

  public static class NullConverterWrapper<T> extends Wrapper<Converter<T>> implements
    Converter<T> {
    private final Converter<Object> nullConverter;

    public NullConverterWrapper(Converter<Object> nullConverter, Converter<T> converter) {
      super(converter);
      this.nullConverter = nullConverter;
    }

    public void serialize(T obj, ObjectWriter writer, Context ctx) throws Exception {
      if (obj == null) {
        nullConverter.serialize(obj, writer, ctx);
      } else {
        wrapped.serialize(obj, writer, ctx);
      }
    }

    @SuppressWarnings("unchecked")
    public T deserialize(ObjectReader reader, Context ctx) throws Exception {
      if (ValueType.NULL == reader.getValueType())
        return (T) nullConverter.deserialize(reader, ctx);

      return wrapped.deserialize(reader, ctx);
    }
  }

  public NullConverter() {
  }

  public void serialize(Object obj, ObjectWriter writer, Context ctx) {
    writer.writeNull();
  }

  public Object deserialize(ObjectReader reader, Context ctx) {
    return null;
  }
}
