package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

/**
 * Base converter for serializing/deserializing a {@link TemporalAccessor} type
 */
abstract class BaseTemporalAccessorConverter<T extends TemporalAccessor> implements Converter<T> {
	private DateTimeConverterOptions options;
	private TimestampHandler<T> timestampHandler;
	private TemporalQuery<T> query;

	BaseTemporalAccessorConverter(DateTimeConverterOptions options, TimestampHandler<T> timestampHandler, TemporalQuery<T> query) {
		this.options = options;
		this.timestampHandler = timestampHandler;
		this.query = query;
	}

	@Override
	public void serialize(T object, ObjectWriter writer, Context ctx) {
		if(options.isAsTimestamp()) {
			TimestampFormat timestampFormat = options.getTimestampFormat();
			switch (timestampFormat) {
				case MILLIS:
				case NANOS:
					timestampHandler.writeNumericTimestamp(object, writer, timestampFormat);
					break;
				case OBJECT:
					timestampHandler.writeObjectTimestamp(object, writer);
					break;
				case ARRAY:
					timestampHandler.writeArrayTimestamp(object, writer);
					break;
			}
		}
		else{
			writer.writeValue(options.getDateTimeFormatter().format(object));
		}
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx) {
		T obj = null;
		if(options.isAsTimestamp()) {
			TimestampFormat timestampFormat = options.getTimestampFormat();
			switch (timestampFormat) {
				case MILLIS:
				case NANOS:
					obj = timestampHandler.readNumericTimestamp(reader, options.getTimestampFormat());
					break;
				case OBJECT:
					obj = timestampHandler.readObjectTimestamp(reader);
					break;
				case ARRAY:
					obj = timestampHandler.readArrayTimestamp(reader);
					break;
			}
		}
		else{
			obj = options.getDateTimeFormatter().parse(reader.valueAsString(), query);
			if(obj instanceof OffsetDateTime){
				obj = (T) DateTimeUtil.correctOffset((OffsetDateTime) obj, options.getZoneId());
			}
		}

		return obj;
	}
}
