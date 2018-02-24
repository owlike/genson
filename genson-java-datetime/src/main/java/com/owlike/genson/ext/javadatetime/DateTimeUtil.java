package com.owlike.genson.ext.javadatetime;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

class DateTimeUtil {
	private static final long THOUSAND = 1_000L;
	private static final long MILLION = 1_000_000L;
	private static final long BILLION = 1_000_000_000L;

	private static final DateTimeFormatter DEFAULTS =
			new DateTimeFormatterBuilder()
					.parseDefaulting(ChronoField.YEAR, 2000)
					.parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
					.parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
					.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
					.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
					.parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
					.toFormatter();

	static DateTimeFormatter createFormatterWithDefaults(DateTimeFormatter formatter, ZoneId zoneId) {
		//Trying to apply zoneId and zoneOffset defaults to ISO_INSTANT causes parsing to throw exceptions
		//Since instants are in UTC zone and always have all required fields, we can return it as is
		if(formatter == DateTimeFormatter.ISO_INSTANT){
			return formatter;
		}
		else {
			DateTimeFormatterBuilder formatterWithDefaultsBuilder = new DateTimeFormatterBuilder().append(formatter).append(DEFAULTS);
			formatterWithDefaultsBuilder.parseDefaulting(ChronoField.OFFSET_SECONDS, (long) OffsetDateTime.now(zoneId).getOffset().getTotalSeconds());
			return formatterWithDefaultsBuilder.toFormatter().withZone(zoneId);
		}
	}

	static long getNanos(long seconds, long nanoAdjustment){
		return seconds * BILLION + nanoAdjustment;
	}

	static long getMillis(long seconds, long nanoAdjustment){
		return (seconds * THOUSAND) + (nanoAdjustment / MILLION);
	}

	static long getSecondsFromMillis(long millis){
		return millis / THOUSAND;
	}

	static long getNanosFromMillis(long millis){
		return (millis % THOUSAND) * MILLION;
	}

	static Instant instantFromMillis(long millis){
		return Instant.ofEpochMilli(millis);
	}

	static Instant instantFromNanos(long nanos){
		long seconds = nanos / BILLION;
		long adjustment = nanos % BILLION;
		return Instant.ofEpochSecond(seconds, adjustment);
	}

	static OffsetDateTime correctOffset(OffsetDateTime value, ZoneId zoneId) {
		Instant instant = value.toLocalDateTime().atZone(zoneId).toInstant();
		return OffsetDateTime.ofInstant(instant, zoneId);
	}
}
