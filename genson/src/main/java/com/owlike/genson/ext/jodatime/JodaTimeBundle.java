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
    ReadableInstantSer readableInstantSer = new ReadableInstantSer(builder.isDateAsTimestamp(), formatter);

    builder.withSerializer(readableInstantSer, DateTime.class)
      .withSerializer(readableInstantSer, MutableDateTime.class)
      .withSerializer(readableInstantSer, Instant.class)
      .withConverters(new DurationConverter(), new PeriodConverter())
      .withConverterFactory(new IntervalConverter.ConverterFactory());

    setupAndRegister(builder, ReadableInstantSerDe.readableInstantDesers());
  }

  protected void setupAndRegister(GensonBuilder builder, List<ReadableInstantDeser<? extends ReadableInstant>> desers) {
    for (ReadableInstantDeser<? extends ReadableInstant> deser : desers) {
      deser.setDateAsMillis(builder.isDateAsTimestamp());
      deser.setFormatter(formatter);

      builder.withDeserializers(deser);
    }
  }

  public JodaTimeBundle useDateFormatter(DateTimeFormatter formatter) {
    this.formatter = formatter;
    return this;
  }

}
