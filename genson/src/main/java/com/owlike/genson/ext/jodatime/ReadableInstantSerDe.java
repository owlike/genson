package com.owlike.genson.ext.jodatime;

import com.owlike.genson.*;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.convert.ContextualFactory;
import com.owlike.genson.reflect.BeanProperty;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class ReadableInstantSerDe {


  final static class ReadableDateTimeConverterContextualFactory implements ContextualFactory {
    private final DateTimeFormatter defaultFormatter;

    ReadableDateTimeConverterContextualFactory(DateTimeFormatter defaultFormatter) {
      this.defaultFormatter = defaultFormatter;
    }

    @Override
    public Converter create(BeanProperty property, Genson genson) {
      ReadableInstantConverter<? extends ReadableDateTime> converter = null;
      JsonDateFormat ann = property.getAnnotation(JsonDateFormat.class);
      if (ann != null) {
        if (MutableDateTime.class.isAssignableFrom(property.getRawClass())) {
          converter = makeMutableDateTimeDeser();
        } else if (DateTime.class.isAssignableFrom(property.getRawClass())) {
          converter = makeDateTimeDeser();
        }

        if (converter != null) {
          converter.setDateAsMillis(ann.asTimeInMillis());
          converter.setFormatter(formatter(ann));
        }
      }
      return converter;
    }

    private DateTimeFormatter formatter(JsonDateFormat ann) {
      Locale locale = ann.lang().isEmpty() ? Locale.getDefault() : new Locale(ann.lang());
      if (ann.value() == null || ann.value().isEmpty()) return defaultFormatter;
      else return DateTimeFormat.forPattern(ann.value()).withLocale(locale);
    }
  }

  public static abstract class ReadableInstantConverter<T extends ReadableInstant> implements Converter<T> {
    protected boolean dateAsMillis;
    protected DateTimeFormatter formatter;

    protected abstract T fromLong(long value);
    protected abstract T fromString(String value);

    @Override
    public void serialize(ReadableInstant object, ObjectWriter writer, Context ctx) throws Exception {
      if (dateAsMillis) writer.writeValue(object.getMillis());
      else writer.writeString(formatter.print(object));
    }

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

  static ReadableInstantConverter<DateTime> makeDateTimeDeser() {
    return new ReadableInstantConverter<DateTime>() {
      protected DateTime fromLong(long value) {
        return new DateTime(value);
      }
      protected DateTime fromString(String value) {
        return formatter.parseDateTime(value);
      }
    };
  }

  static ReadableInstantConverter<MutableDateTime> makeMutableDateTimeDeser() {
    return  new ReadableInstantConverter<MutableDateTime>() {
      protected MutableDateTime fromLong(long value) {
        return new MutableDateTime(value);
      }
      protected MutableDateTime fromString(String value) {
        return formatter.parseMutableDateTime(value);
      }
    };
  }

  public static List<ReadableInstantConverter<? extends ReadableInstant>> readableInstantConverters() {

    return Arrays.<ReadableInstantConverter<? extends ReadableInstant>>asList(
      makeDateTimeDeser(), makeMutableDateTimeDeser(), new ReadableInstantConverter<Instant>() {
        protected Instant fromLong(long value) {
          return new Instant(value);
        }

        protected Instant fromString(String value) {
          return new Instant(value);
        }
      }
    );
  }
}
