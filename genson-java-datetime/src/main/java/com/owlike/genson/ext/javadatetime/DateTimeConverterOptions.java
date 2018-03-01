package com.owlike.genson.ext.javadatetime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

class DateTimeConverterOptions {
	private final DateTimeFormatter dateTimeFormatter;
	private final boolean asTimestamp;
	private final TimestampFormat timestampFormat;
	private final ZoneId zoneId;

	/**
	 * Options to use when creating a {@link com.owlike.genson.Converter} for a {@link java.time.temporal.TemporalAccessor} type
	 * @param clazz The class to which the converter applies
	 * @param dateTimeFormatter The {@link DateTimeFormatter} to use for the {@link java.time.temporal.TemporalAccessor} type
	 * @param asTimestamp Whether values of the specified type should be serialized/deserialized as timestamps
	 * @param timestampFormat The {@link TimestampFormat} to use if asTimestamp is true
	 * @param zoneId The default {@link ZoneId} to use when parsing
	 */
	DateTimeConverterOptions(Class<?> clazz, DateTimeFormatter dateTimeFormatter, boolean asTimestamp, TimestampFormat timestampFormat, ZoneId zoneId) {
		this.dateTimeFormatter = dateTimeFormatter == null ? null : DateTimeUtil.createFormatterWithDefaults(dateTimeFormatter, zoneId);
		this.asTimestamp = asTimestamp;
		this.timestampFormat = timestampFormat;
		// Instant should always be in UTC timezone
		this.zoneId = clazz == Instant.class ? ZoneId.of("UTC") : zoneId;
	}

	public DateTimeFormatter getDateTimeFormatter() {
		return dateTimeFormatter;
	}

	public boolean isAsTimestamp() {
		return asTimestamp;
	}

	public TimestampFormat getTimestampFormat() {
		return timestampFormat;
	}

	public ZoneId getZoneId() {
		return zoneId;
	}
}
