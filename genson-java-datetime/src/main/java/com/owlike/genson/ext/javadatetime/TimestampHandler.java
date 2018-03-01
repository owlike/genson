package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class to handle the parsing/serializing of different timestamp formats
 */
abstract class TimestampHandler<T extends TemporalAccessor> {
	private Function<T, Long> toMillis;
	private Function<Long, T> fromMillis;
	private Function<T, Long> toNanos;
	private Function<Long, T> fromNanos;
	private LinkedHashMap<String, TemporalField> temporalFields;
	private Supplier<T> instanceProvider;

	TimestampHandler(Function<T, Long> toMillis, Function<Long, T> fromMillis, Function<T, Long> toNanos, Function<Long, T> fromNanos, LinkedHashMap<String, TemporalField> temporalFields, Supplier<T> instanceProvider) {
		this.toMillis = toMillis;
		this.fromMillis = fromMillis;
		this.toNanos = toNanos;
		this.fromNanos = fromNanos;
		this.temporalFields = temporalFields;
		this.instanceProvider = instanceProvider;
	}

	void writeNumericTimestamp(T object, ObjectWriter writer, TimestampFormat timestampFormat) {
		if(timestampFormat == TimestampFormat.MILLIS){
			writer.writeValue(toMillis.apply(object));
		}
		else{
			writer.writeValue(toNanos.apply(object));
		}
	}

	void writeObjectTimestamp(T object, ObjectWriter writer) {
		writer.beginObject();
		writeFieldsAsObject(object, writer);
		writer.endObject();
	}

	protected void writeFieldsAsObject(T object, ObjectWriter writer) {
		for(Map.Entry<String, TemporalField> temporalFieldEntry: temporalFields.entrySet()){
			String jsonName = temporalFieldEntry.getKey();
			TemporalField field = temporalFieldEntry.getValue();
			long value = object.getLong(field);
			writer.writeName(jsonName);
			writer.writeValue(value);
		}
	}

	void writeArrayTimestamp(T object, ObjectWriter writer) {
		writer.beginArray();
		writeFieldsAsArray(object, writer);
		writer.endArray();
	}

	protected void writeFieldsAsArray(T object, ObjectWriter writer) {
		for(TemporalField field: temporalFields.values()){
			long value = object.getLong(field);
			writer.writeValue(value);
		}
	}

	final T readNumericTimestamp(ObjectReader reader, TimestampFormat timestampFormat) {
		long value = reader.valueAsLong();
		Function<Long, T> numberToInstance = timestampFormat == TimestampFormat.MILLIS ? fromMillis : fromNanos;
		if(numberToInstance == null){
			throw new IllegalArgumentException("Timestamp format not supported");
		}
		return numberToInstance.apply(value);
	}

	final T readArrayTimestamp(ObjectReader reader) {
		reader.beginArray();
		T obj = readFieldsFromArray(instanceProvider, reader);
		reader.endArray();
		return obj;
	}

	protected T readFieldsFromArray(Supplier<T> instanceProvider, ObjectReader reader) {
		Temporal obj = (Temporal) instanceProvider.get();

		for(TemporalField temporalField: temporalFields.values()){
			if(reader.hasNext()){
				reader.next();
				long value = reader.valueAsLong();
				obj = obj.with(temporalField, value);
			}
		}

		return (T) obj;
	}

	final T readObjectTimestamp(ObjectReader reader) {
		reader.beginObject();
		T obj = readFieldsFromObject(instanceProvider, reader);
		reader.endObject();
		return obj;
	}

	protected T readFieldsFromObject(Supplier<T> instanceProvider, ObjectReader reader) {
		T obj = instanceProvider.get();
		while(reader.hasNext()){
			reader.next();
			obj = readFieldFromObject(obj, reader);
		}
		return obj;
	}

	protected T readFieldFromObject(T obj, ObjectReader reader){
		Temporal objTemporal = (Temporal) obj;
		String jsonName = reader.name();
		TemporalField field = temporalFields.get(jsonName);
		long value = reader.valueAsLong();
		return (T) objTemporal.with(field, value);
	}
}
