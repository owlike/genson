package com.owlike.genson.ext.jodatime;

import com.owlike.genson.Context;
import com.owlike.genson.Deserializer;
import com.owlike.genson.Serializer;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.List;

public final class ReadableInstantSerDe {

  public static List<ReadableInstantDeser<? extends ReadableInstant>> readableInstantDesers() {

    return Arrays.<ReadableInstantDeser<? extends ReadableInstant>>asList(
      new ReadableInstantDeser<DateTime>() {
        protected DateTime fromLong(long value) {
          return new DateTime(value);
        }

        protected DateTime fromString(String value) {
          return formatter.parseDateTime(value);
        }
      }, new ReadableInstantDeser<MutableDateTime>() {
        protected MutableDateTime fromLong(long value) {
          return new MutableDateTime(value);
        }

        protected MutableDateTime fromString(String value) {
          return formatter.parseMutableDateTime(value);
        }
      }, new ReadableInstantDeser<Instant>() {
        protected Instant fromLong(long value) {
          return new Instant(value);
        }

        protected Instant fromString(String value) {
          return new Instant(value);
        }
      }
    );
  }

  public static class ReadableInstantSer implements Serializer<ReadableInstant> {
    private final boolean dateAsMillis;
    private final DateTimeFormatter formatter;

    public ReadableInstantSer(boolean dateAsMillis, DateTimeFormatter formatter) {
      this.dateAsMillis = dateAsMillis;
      this.formatter = formatter;
    }

    @Override
    public void serialize(ReadableInstant object, ObjectWriter writer, Context ctx) throws Exception {
      if (dateAsMillis) writer.writeValue(object.getMillis());
      else writer.writeString(formatter.print(object));
    }
  }

  public static abstract class ReadableInstantDeser<T extends ReadableInstant> implements Deserializer<T> {
    protected boolean dateAsMillis;
    protected DateTimeFormatter formatter;

    protected abstract T fromLong(long value);
    protected abstract T fromString(String value);

    @Override
    public T deserialize(ObjectReader reader, Context ctx) throws Exception {
      if (ValueType.INTEGER == reader.getValueType()) return fromLong(reader.valueAsLong());
      else return fromString(reader.valueAsString());
    }

    // Ugly but this will reduce the size of the code so we don't have to repeat the ctr in subclasses
    public void setFormatter(DateTimeFormatter formatter) {
      this.formatter = formatter;
    }

    public void setDateAsMillis(boolean dateAsMillis) {
      this.dateAsMillis = dateAsMillis;
    }
  }
}
