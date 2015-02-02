package com.owlike.genson.ext.jodatime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.HandleBeanView;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.lang.reflect.Type;

@HandleBeanView
public class IntervalConverter implements Converter<Interval> {

  public static class ConverterFactory implements Factory<Converter<Interval>> {

    @Override
    public Converter<Interval> create(Type type, Genson genson) {
      return new IntervalConverter(genson.<DateTime>provideConverter(DateTime.class));
    }
  }

  private final Converter<DateTime> dateTimeConverter;

  public IntervalConverter(Converter<DateTime> dateTimeConverter) {
    this.dateTimeConverter = dateTimeConverter;
  }

  @Override
  public void serialize(Interval interval, ObjectWriter writer, Context ctx) throws Exception {
    writer.beginObject();
    writer.writeName("start");
    dateTimeConverter.serialize(interval.getStart(), writer, ctx);
    writer.writeName("end");
    dateTimeConverter.serialize(interval.getEnd(), writer, ctx);
    writer.endObject();
  }

  @Override
  public Interval deserialize(ObjectReader reader, Context ctx) throws Exception {
    DateTime start = null, end = null;

    reader.beginObject();
    while(reader.hasNext()) {
      reader.next();
      if ("start".equals(reader.name())) start = dateTimeConverter.deserialize(reader, ctx);
      else if ("end".equals(reader.name())) end = dateTimeConverter.deserialize(reader, ctx);
      else {
        throw new IllegalStateException("Encountered unexpected property " + reader.name() + " and value " + reader.valueAsString());
      }
    }
    reader.endObject();

    return new Interval(start, end);
  }
}
