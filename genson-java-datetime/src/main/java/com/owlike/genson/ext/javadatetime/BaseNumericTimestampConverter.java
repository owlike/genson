package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Context;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.function.Function;

class BaseNumericTimestampConverter<T extends TemporalAccessor> extends BaseTemporalConverter<T>{
	private Function<T, Long> toMillis;
	private Function<Long, T> fromMillis;
	private Function<T, Long> toNanos;
	private Function<Long, T> fromNanos;


	BaseNumericTimestampConverter(DateTimeConverterOptions options, TemporalQuery<T> query, Function<T, Long> toMillis, Function<Long, T> fromMillis, Function<T, Long> toNanos, Function<Long, T> fromNanos) {
		super(options, query);
		this.toMillis = toMillis;
		this.fromMillis = fromMillis;
		this.toNanos = toNanos;
		this.fromNanos = fromNanos;
	}

	@Override
	public void serialize(T object, ObjectWriter writer, Context ctx) {
		if(isAsTimestamp()){
			writer.writeValue(getTimestamp(object));
		}
		else{
			writer.writeValue(formatValue(object));
		}
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx) {
		if(ValueType.INTEGER == reader.getValueType()){
			return parseTimestamp(reader.valueAsLong());
		}
		else{
			return parseValue(reader.valueAsString());
		}
	}

	private Long getTimestamp(T obj){
		switch (getTimestampFormat()){
			case MILLIS:
				return toMillis.apply(obj);
			case NANOS:
				return toNanos.apply(obj);
		}
		throw new IllegalArgumentException("Invalid timestamp format");
	}

	private T parseTimestamp(long timestamp) {
		switch(getTimestampFormat()){
			case MILLIS:
				return fromMillis.apply(timestamp);
			case NANOS:
				return fromNanos.apply(timestamp);
		}
		throw new IllegalArgumentException("Invalid timestamp format");
	}
}
