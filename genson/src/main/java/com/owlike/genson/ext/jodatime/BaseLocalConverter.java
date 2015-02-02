package com.owlike.genson.ext.jodatime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.annotation.HandleBeanView;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.base.BaseLocal;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

@HandleClassMetadata
@HandleBeanView
public abstract class BaseLocalConverter<T extends BaseLocal> implements Converter<T>  {
  protected final DateTimeFormatter formatter;

  protected BaseLocalConverter(DateTimeFormatter formatter) {
    this.formatter = formatter;
  }

  @Override
  public void serialize(T object, ObjectWriter writer, Context ctx) throws Exception {
    writer.writeString(formatter.print(object));
  }

  @Override
  public T deserialize(ObjectReader reader, Context ctx) throws Exception {
    if (ValueType.INTEGER == reader.getValueType()) return fromLong(reader.valueAsLong());
    else return fromString(reader.valueAsString());
  }

  protected abstract T fromLong(long value);
  protected abstract T fromString(String value);

  static BaseLocalConverter<LocalDate> makeLocalDateConverter(DateTimeFormatter formatter) {
    return new BaseLocalConverter<LocalDate>(formatter) {
      protected LocalDate fromString(String value) {
        return formatter.parseLocalDate(value);
      }

      protected LocalDate fromLong(long value) {
        return  new LocalDate(value);
      }
    };
  }

  static BaseLocalConverter<LocalDateTime> makeLocalDateTimeConverter(DateTimeFormatter formatter) {
    return new BaseLocalConverter<LocalDateTime>(formatter) {
      private final DateTimeFormatter localFormatter = ISODateTimeFormat.localDateOptionalTimeParser();
      protected LocalDateTime fromString(String value) {
        return localFormatter.parseLocalDateTime(value);
      }

      protected LocalDateTime fromLong(long value) {
        return  new LocalDateTime(value);
      }
    };
  }

  static BaseLocalConverter<LocalTime> makeLocalTimeConverter(DateTimeFormatter formatter) {
    return new BaseLocalConverter<LocalTime>(formatter) {
      private final DateTimeFormatter localFormatter = ISODateTimeFormat.localTimeParser();
      protected LocalTime fromString(String value) {
        return localFormatter.parseLocalTime(value);
      }

      protected LocalTime fromLong(long value) {
        return  new LocalTime(value);
      }
    };
  }
}
