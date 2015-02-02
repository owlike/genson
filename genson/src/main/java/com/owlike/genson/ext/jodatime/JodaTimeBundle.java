package com.owlike.genson.ext.jodatime;

import com.owlike.genson.*;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.convert.ContextualFactory;
import com.owlike.genson.ext.GensonBundle;
import static com.owlike.genson.ext.jodatime.BaseReadableInstantConverter.*;
import static com.owlike.genson.ext.jodatime.BaseLocalConverter.*;
import com.owlike.genson.reflect.BeanProperty;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Locale;

public class JodaTimeBundle extends GensonBundle {
  private final class JodaTimeConverterContextualFactory implements ContextualFactory {

    @Override
    public Converter create(BeanProperty property, Genson genson) {
      JsonDateFormat ann = property.getAnnotation(JsonDateFormat.class);
      if (ann != null) {
        if (MutableDateTime.class.isAssignableFrom(property.getRawClass())) {
          return makeMutableDateTimeConverter(ann.asTimeInMillis(), formatter(ann, dateTimeFormatter));
        } else if (DateTime.class.isAssignableFrom(property.getRawClass())) {
          return makeDateTimeConverter(ann.asTimeInMillis(), formatter(ann, dateTimeFormatter));
        }
        // local formats
        else if (LocalDate.class.isAssignableFrom(property.getRawClass())) {
          return makeLocalDateConverter(formatter(ann, localDateFormatter));
        } else if (LocalDateTime.class.isAssignableFrom(property.getRawClass())) {
          return makeLocalDateTimeConverter(formatter(ann, localDateTimeFormatter));
        } else if (LocalTime.class.isAssignableFrom(property.getRawClass())) {
          return makeLocalTimeConverter(formatter(ann, localTimeFormatter));
        }
      }
      return null;
    }

    private DateTimeFormatter formatter(JsonDateFormat ann, DateTimeFormatter defaultFormatter) {
      Locale locale = ann.lang().isEmpty() ? Locale.getDefault() : new Locale(ann.lang());
      if (ann.value() == null || ann.value().isEmpty()) return defaultFormatter;
      else return DateTimeFormat.forPattern(ann.value()).withLocale(locale);
    }
  }

  private DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
  private DateTimeFormatter localDateFormatter = ISODateTimeFormat.date().withZone(DateTimeZone.getDefault());
  private DateTimeFormatter localDateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.getDefault());
  private DateTimeFormatter localTimeFormatter = ISODateTimeFormat.time().withZone(DateTimeZone.getDefault());

  @Override
  public void configure(GensonBuilder builder) {
    builder.withContextualFactory(new JodaTimeConverterContextualFactory())
      .withConverters(
          new DurationConverter(),
          new PeriodConverter(),
          makeDateTimeConverter(builder.isDateAsTimestamp(), dateTimeFormatter),
          makeMutableDateTimeConverter(builder.isDateAsTimestamp(), dateTimeFormatter),
          makeInstantConverter(builder.isDateAsTimestamp(), dateTimeFormatter),
          makeLocalDateConverter(localDateFormatter),
          makeLocalDateTimeConverter(localDateTimeFormatter),
          makeLocalTimeConverter(localTimeFormatter)
        )
      .withConverterFactory(new IntervalConverter.ConverterFactory());
  }

  public JodaTimeBundle useDateTimeFormatter(DateTimeFormatter formatter) {
    this.dateTimeFormatter = formatter;
    return this;
  }

  public JodaTimeBundle useLocalDateFormatter(DateTimeFormatter formatter) {
    this.localDateFormatter = formatter;
    return this;
  }

  public JodaTimeBundle useLocalDateTimeFormatter(DateTimeFormatter formatter) {
    this.localDateTimeFormatter = formatter;
    return this;
  }

  public JodaTimeBundle useLocalTimeFormatter(DateTimeFormatter formatter) {
    this.localTimeFormatter = formatter;
    return this;
  }
}
