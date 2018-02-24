package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Context;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.List;

abstract class BaseArrayTimestampConverter<T extends TemporalAccessor> extends BaseTemporalConverter<T> {
	BaseArrayTimestampConverter(DateTimeConverterOptions options, TemporalQuery<T> query) {
		super(options, query);
	}

	abstract List<Number> toNumbers(T object);
	abstract T fromNumbers(List<Number> numbers);


	@Override
	public void serialize(T object, ObjectWriter writer, Context ctx) {
		if(isAsTimestamp()){
			List<Number> numbers = toNumbers(object);
			writer.beginArray();
			numbers.forEach(writer::writeValue);
			writer.endArray();
		}
		else{
			writer.writeValue(formatValue(object));
		}
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx) {
		if(ValueType.ARRAY == reader.getValueType()){
			List<Number> numbers = new ArrayList<>();
			reader.beginArray();
			while(reader.hasNext()){
				reader.next();
				numbers.add(reader.valueAsLong());
			}
			reader.endArray();
			return fromNumbers(numbers);
		}
		else{
			return parseValue(reader.valueAsString());
		}
	}

}
