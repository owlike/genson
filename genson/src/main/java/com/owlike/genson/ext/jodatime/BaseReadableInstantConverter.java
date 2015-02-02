package com.owlike.genson.ext.jodatime;

import com.owlike.genson.*;
import com.owlike.genson.annotation.HandleBeanView;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;

@HandleClassMetadata
@HandleBeanView
public abstract class BaseReadableInstantConverter<T extends ReadableInstant> implements Converter<T> {
  protected final boolean dateAsMillis;
  protected final DateTimeFormatter formatter;

  protected BaseReadableInstantConverter(boolean dateAsMillis, DateTimeFormatter formatter) {
    this.dateAsMillis = dateAsMillis;
    this.formatter = formatter;
  }

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

  static BaseReadableInstantConverter<DateTime> makeDateTimeConverter(boolean dateAsMillis, DateTimeFormatter formatter) {
    return new BaseReadableInstantConverter<DateTime>(dateAsMillis, formatter) {
      protected DateTime fromLong(long value) {
        return new DateTime(value);
      }
      protected DateTime fromString(String value) {
        return formatter.parseDateTime(value);
      }
    };
  }

  static BaseReadableInstantConverter<MutableDateTime> makeMutableDateTimeConverter(boolean dateAsMillis,
                                                                                DateTimeFormatter formatter) {
    return  new BaseReadableInstantConverter<MutableDateTime>(dateAsMillis, formatter) {
      protected MutableDateTime fromLong(long value) {
        return new MutableDateTime(value);
      }
      protected MutableDateTime fromString(String value) {
        return formatter.parseMutableDateTime(value);
      }
    };
  }

  static BaseReadableInstantConverter<Instant> makeInstantConverter(boolean dateAsMillis, DateTimeFormatter formatter) {
    return new BaseReadableInstantConverter<Instant>(dateAsMillis, formatter) {
      protected Instant fromLong(long value) {
        return new Instant(value);
      }

      protected Instant fromString(String value) {
        return new Instant(value);
      }
    };
  }
}
