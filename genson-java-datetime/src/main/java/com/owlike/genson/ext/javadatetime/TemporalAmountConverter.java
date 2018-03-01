package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class TemporalAmountConverter<T extends TemporalAmount> implements Converter<T> {
	private static final LinkedHashMap<String, Function<Period, Number>> PERIOD_FIELD_EXTRACTORS = new LinkedHashMap<>();
	static{
		PERIOD_FIELD_EXTRACTORS.put("years", Period::getYears);
		PERIOD_FIELD_EXTRACTORS.put("months", Period::getMonths);
		PERIOD_FIELD_EXTRACTORS.put("days", Period::getDays);
	}

	private static final LinkedHashMap<String, BiFunction<Period, Number, Period>> PERIOD_FIELD_APPLICATORS = new LinkedHashMap<>();
	static{
		PERIOD_FIELD_APPLICATORS.put("years", (p, n) -> p.withYears(n.intValue()));
		PERIOD_FIELD_APPLICATORS.put("months", (p, n) -> p.withMonths(n.intValue()));
		PERIOD_FIELD_APPLICATORS.put("days", (p, n) -> p.withDays(n.intValue()));
	}

	private static final LinkedHashMap<String, Function<Duration, Number>> DURATION_FIELD_EXTRACTORS = new LinkedHashMap<>();
	static{
		DURATION_FIELD_EXTRACTORS.put("seconds", Duration::getSeconds);
		DURATION_FIELD_EXTRACTORS.put("nanos", Duration::getNano);
	}

	private static final LinkedHashMap<String, BiFunction<Duration, Number, Duration>> DURATION_FIELD_APPLICATORS = new LinkedHashMap<>();
	static{
		DURATION_FIELD_APPLICATORS.put("seconds", (d, n) -> d.withSeconds(n.longValue()));
		DURATION_FIELD_APPLICATORS.put("nanos", (d, n) -> d.withNanos(n.intValue()));
	}


	private DateTimeConverterOptions options;
	private Function<String, T> parseFunction;
	private Supplier<T> instanceProvider;
	private LinkedHashMap<String, Function<T, Number>> fieldExtractors;
	private LinkedHashMap<String, BiFunction<T, Number,T >> fieldApplicatiors;

	private TemporalAmountConverter(DateTimeConverterOptions options, Function<String, T> parseFunction, Supplier<T> instanceProvider, LinkedHashMap<String, Function<T, Number>> fieldExtractors, LinkedHashMap<String, BiFunction<T, Number, T>> fieldApplicatiors) {
		this.options = options;
		this.parseFunction = parseFunction;
		this.instanceProvider = instanceProvider;
		this.fieldExtractors = fieldExtractors;
		this.fieldApplicatiors = fieldApplicatiors;
	}

	static TemporalAmountConverter<Duration> duration(DateTimeConverterOptions options){
		return new TemporalAmountConverter<Duration>(options, Duration::parse, () -> Duration.ZERO, DURATION_FIELD_EXTRACTORS, DURATION_FIELD_APPLICATORS) {};
	}

	static TemporalAmountConverter<Period> period(DateTimeConverterOptions options){
		return new TemporalAmountConverter<Period>(options, Period::parse, () -> Period.ZERO, PERIOD_FIELD_EXTRACTORS, PERIOD_FIELD_APPLICATORS) {};
	}

	@Override
	public void serialize(T object, ObjectWriter writer, Context ctx) {
		if (options.isAsTimestamp()) {
			switch(options.getTimestampFormat()){
				case ARRAY:
					writeArray(object, writer);
					break;
				case OBJECT:
					writeObject(object, writer);
					break;
				default:
					throw new IllegalArgumentException("Unsupported timestamp format");
			}
		}
		else {
			writer.writeValue(object.toString());
		}
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx) {
		T obj;
		if (options.isAsTimestamp()) {
			switch(options.getTimestampFormat()){
				case ARRAY:
					obj = readArray(reader);
					break;
				case OBJECT:
					obj = readObject(reader);
					break;
				default:
					throw new IllegalArgumentException("Unsupported timestamp format");
			}
		}
		else {
			obj = parseFunction.apply(reader.valueAsString());
		}

		return obj;
	}

	private void writeArray(T object, ObjectWriter writer) {
		writer.beginArray();
		for(Function<T, Number> fieldExtractor: fieldExtractors.values()){
			writer.writeValue(fieldExtractor.apply(object));
		}
		writer.endArray();
	}

	private T readArray(ObjectReader reader) {
		T obj = instanceProvider.get();
		reader.beginArray();
		for(BiFunction<T, Number, T> fieldApplicator: fieldApplicatiors.values()){
			reader.next();
			obj = fieldApplicator.apply(obj, reader.valueAsLong());
		}
		reader.endArray();
		return obj;
	}

	private void writeObject(T object, ObjectWriter writer) {
		writer.beginObject();
		for(Map.Entry<String, Function<T, Number>> fieldExtractor: fieldExtractors.entrySet()){
			writer.writeName(fieldExtractor.getKey());
			writer.writeValue(fieldExtractor.getValue().apply(object));
		}
		writer.endObject();
	}

	private T readObject(ObjectReader reader) {
		T obj = instanceProvider.get();
		reader.beginObject();
		while(reader.hasNext()){
			reader.next();
			String applicatorName = reader.name();
			BiFunction<T, Number, T> applicator = fieldApplicatiors.get(applicatorName);
			obj = applicator.apply(obj, reader.valueAsLong());
		}
		reader.endObject();
		return obj;
	}
}
