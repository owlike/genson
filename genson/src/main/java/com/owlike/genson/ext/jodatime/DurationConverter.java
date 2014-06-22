package com.owlike.genson.ext.jodatime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import org.joda.time.Duration;

public class DurationConverter implements Converter<Duration> {
  @Override
  public void serialize(Duration object, ObjectWriter writer, Context ctx) throws Exception {
    writer.writeValue(object.getMillis());
  }

  @Override
  public Duration deserialize(ObjectReader reader, Context ctx) throws Exception {
    return new Duration(reader.valueAsLong());
  }
}
