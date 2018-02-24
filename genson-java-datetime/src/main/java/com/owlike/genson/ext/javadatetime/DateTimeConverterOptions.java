package com.owlike.genson.ext.javadatetime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeConverterOptions {
	private final DateTimeFormatter dateTimeFormatter;
	private final boolean asTimestamp;
	private final TimestampFormat timestampFormat;
	private final ZoneId zoneId;

	DateTimeConverterOptions(Class<?> clazz, DateTimeFormatter dateTimeFormatter, boolean asTimestamp, TimestampFormat timestampFormat, ZoneId zoneId) {
		this.dateTimeFormatter = dateTimeFormatter;
		this.asTimestamp = asTimestamp;
		this.timestampFormat = timestampFormat;
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
