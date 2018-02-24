package com.owlike.genson.ext.javadatetime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.function.Function;

abstract class LocalTimestampableConverter<T extends TemporalAccessor> extends BaseNumericTimestampConverter<T> {
	private LocalTimestampableConverter(DateTimeConverterOptions options, TemporalQuery<T> query, Function<T, Long> toMillis, Function<Long, T> fromMills,  Function<T, Long> toNanos, Function<Long, T> fromNanos) {
		super(options, query, toMillis, fromMills, toNanos, fromNanos);
	}

	static LocalTimestampableConverter<LocalDate> localDate(DateTimeConverterOptions options){
		return new LocalTimestampableConverter<LocalDate>(options, LocalDate::from, LocalDate::toEpochDay, LocalDate::ofEpochDay, LocalDate::toEpochDay, LocalDate::ofEpochDay){};
	}

	static LocalTimestampableConverter<LocalTime> localTime(DateTimeConverterOptions options){
		return new LocalTimestampableConverter<LocalTime>(options, LocalTime::from,
				lt -> DateTimeUtil.getMillis(lt.toSecondOfDay(), lt.getNano()), LocalTimestampableConverter::localTimeFromMillisOfDay,
				LocalTime::toNanoOfDay, LocalTime::ofNanoOfDay){};
	}

	private static LocalTime localTimeFromMillisOfDay(long millis){
		long seconds = DateTimeUtil.getSecondsFromMillis(millis);
		long nanos = DateTimeUtil.getNanosFromMillis(millis);
		return LocalTime.ofSecondOfDay(seconds).withNano((int) nanos);
	}
}
