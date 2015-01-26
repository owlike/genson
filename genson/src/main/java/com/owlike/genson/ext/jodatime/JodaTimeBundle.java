package com.owlike.genson.ext.jodatime;

import com.owlike.genson.*;
import com.owlike.genson.ext.GensonBundle;
import com.owlike.genson.ext.jodatime.ReadableInstantSerDe.*;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;

public class JodaTimeBundle extends GensonBundle {
  private DateTimeFormatter formatter = ISODateTimeFormat.dateTime();

  @Override
  public void configure(GensonBuilder builder) {
    builder.withContextualFactory(new ReadableDateTimeConverterContextualFactory(formatter))
      .withConverters(new DurationConverter(), new PeriodConverter())
      .withConverterFactory(new IntervalConverter.ConverterFactory());

    setupAndRegister(builder, ReadableInstantSerDe.readableInstantConverters());
  }

  protected void setupAndRegister(GensonBuilder builder, List<ReadableInstantConverter<? extends ReadableInstant>> converters) {
    for (ReadableInstantConverter<? extends ReadableInstant> converter : converters) {
      converter.setDateAsMillis(builder.isDateAsTimestamp());
      converter.setFormatter(formatter);

      builder.withConverters(converter);
    }
  }

  public JodaTimeBundle useDateFormatter(DateTimeFormatter formatter) {
    this.formatter = formatter;
    return this;
  }
}
