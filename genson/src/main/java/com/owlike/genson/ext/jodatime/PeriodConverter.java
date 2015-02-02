package com.owlike.genson.ext.jodatime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.annotation.HandleBeanView;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

@HandleClassMetadata
@HandleBeanView
public class PeriodConverter implements Converter<Period> {
  private final PeriodFormatter formatter = ISOPeriodFormat.standard();

  @Override
  public void serialize(Period object, ObjectWriter writer, Context ctx) throws Exception {
    writer.writeString(formatter.print(object));
  }

  @Override
  public Period deserialize(ObjectReader reader, Context ctx) throws Exception {
    return formatter.parsePeriod(reader.valueAsString());
  }
}
