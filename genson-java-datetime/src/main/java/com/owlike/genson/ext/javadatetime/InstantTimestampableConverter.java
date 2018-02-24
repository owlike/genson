package com.owlike.genson.ext.javadatetime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.function.Function;

abstract class InstantTimestampableConverter<T extends TemporalAccessor> extends BaseNumericTimestampConverter<T> {
	private InstantTimestampableConverter(DateTimeConverterOptions converterOptions, TemporalQuery<T> query, Function<Instant, T> fromInstant, Function<T, Instant> toInstant) {
		super(converterOptions, query,
				obj -> instantToMillis(toInstant.apply(obj)),
				millis -> fromInstant.apply(DateTimeUtil.instantFromMillis(millis)),
				obj -> instantToNanos(toInstant.apply(obj)),
				nanos -> fromInstant.apply(DateTimeUtil.instantFromNanos(nanos))
		);
	}

	static InstantTimestampableConverter<Instant> instant(DateTimeConverterOptions converterOptions) {
		return new InstantTimestampableConverter<Instant>(converterOptions, Instant::from, instant -> instant, instant -> instant){};
	}

	static InstantTimestampableConverter<ZonedDateTime> zonedDateTime(DateTimeConverterOptions converterOptions) {
		return new InstantTimestampableConverter<ZonedDateTime>(converterOptions, ZonedDateTime::from,
				instant -> ZonedDateTime.ofInstant(instant, converterOptions.getZoneId()), ZonedDateTime::toInstant){};
	}

	static InstantTimestampableConverter<OffsetDateTime> offsetDateTime(DateTimeConverterOptions converterOptions) {
		return new InstantTimestampableConverter<OffsetDateTime>(converterOptions, OffsetDateTime::from,
				instant -> OffsetDateTime.ofInstant(instant, converterOptions.getZoneId()), OffsetDateTime::toInstant){};
	}

	static InstantTimestampableConverter<LocalDateTime> localDateTime(DateTimeConverterOptions converterOptions) {
		return new InstantTimestampableConverter<LocalDateTime>(converterOptions, LocalDateTime::from,
				instant -> LocalDateTime.ofInstant(instant, converterOptions.getZoneId()), lt -> lt.atZone(converterOptions.getZoneId()).toInstant()){};
	}

	private static Long instantToMillis(Instant instant){
		return instant.toEpochMilli();
	}

	private static Long instantToNanos(Instant instant){
		return DateTimeUtil.getNanos(instant.getEpochSecond(), instant.getNano());
	}
}
