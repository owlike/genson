package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class TemporalAmountConverter<T extends TemporalAmount> implements Converter<T> {
	private static final List<Function<Period, Number>> PERIOD_FIELD_EXTRACTORS = Arrays.asList(
			Period::getYears,
			Period::getMonths,
			Period::getDays);

	private static final List<BiFunction<Period, Number, Period>> PERIOD_FIELD_APPLICATORS = Arrays.asList(
			(p, n) -> p.withYears(n.intValue()),
			(p, n) -> p.withMonths(n.intValue()),
			(p, n) -> p.withDays(n.intValue()) );

	private static final List<Function<Duration, Number>> DURATION_FIELD_EXTRACTORS = Arrays.asList(
			Duration::getSeconds,
			Duration::getNano);

	private static final List<BiFunction<Duration, Number, Duration>> DURATION_FIELD_APPLICATORS = Arrays.asList(
			(d, n) -> d.withSeconds(n.longValue()),
			(d, n) -> d.withNanos(n.intValue()));

	private boolean isAsTimestamp;
	private Function<String, T> parseFunction;
	private Supplier<T> instanceProvider;
	private List<Function<T, Number>> fieldExtractors;
	private List<BiFunction<T, Number,T >> fieldApplicatiors;

	private TemporalAmountConverter(boolean isAsTimestamp, Function<String, T> parseFunction, Supplier<T> instanceProvider, List<Function<T, Number>> fieldExtractors, List<BiFunction<T, Number, T>> fieldApplicatiors) {
		this.isAsTimestamp = isAsTimestamp;
		this.parseFunction = parseFunction;
		this.instanceProvider = instanceProvider;
		this.fieldExtractors = fieldExtractors;
		this.fieldApplicatiors = fieldApplicatiors;
	}

	static TemporalAmountConverter<Duration> duration(DateTimeConverterOptions options){
		return new TemporalAmountConverter<Duration>(options.isAsTimestamp(), Duration::parse, () -> Duration.ZERO, DURATION_FIELD_EXTRACTORS, DURATION_FIELD_APPLICATORS) {};
	}

	static TemporalAmountConverter<Period> period(DateTimeConverterOptions options){
		return new TemporalAmountConverter<Period>(options.isAsTimestamp(), Period::parse, () -> Period.ZERO, PERIOD_FIELD_EXTRACTORS, PERIOD_FIELD_APPLICATORS) {};
	}

	private List<Number> toNumbers(T object) {
		List<Number> nums = new ArrayList<>();
		for (Function<T, Number> fieldExtractor : fieldExtractors) {
			nums.add(fieldExtractor.apply(object));
		}
		return nums;
	}

	private T fromNumbers(List<Number> numbers) {
		T obj = instanceProvider.get();
		for (int i = 0; i < fieldApplicatiors.size(); i++) {
			BiFunction<T, Number, T> fieldApplicator = fieldApplicatiors.get(i);
			Number number = numbers.get(i);
			obj = fieldApplicator.apply(obj, number);
		}
		return obj;
	}

	@Override
	public void serialize(T object, ObjectWriter writer, Context ctx) {
		if (isAsTimestamp) {
			List<Number> numbers = toNumbers(object);
			writer.beginArray();
			numbers.forEach(writer::writeValue);
			writer.endArray();
		} else {
			writer.writeValue(object.toString());
		}
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx) {
		if (ValueType.ARRAY == reader.getValueType()) {
			List<Number> numbers = new ArrayList<>();
			reader.beginArray();
			while (reader.hasNext()) {
				reader.next();
				numbers.add(reader.valueAsLong());
			}
			reader.endArray();
			return fromNumbers(numbers);
		} else {
			return parseFunction.apply(reader.valueAsString());
		}
	}
}
