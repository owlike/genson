package com.owlike.genson.ext.javadatetime;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

/**
 * Converter for values of type {@link OffsetDateTime}
 */
public class OffsetDateTimeConverter extends BaseTemporalAccessorConverter<OffsetDateTime> {
	OffsetDateTimeConverter(DateTimeConverterOptions options) {
		super(options, new OffsetDateTimeTimestampHandler(options), OffsetDateTime::from);
	}

	private static class OffsetDateTimeTimestampHandler extends TimestampHandler<OffsetDateTime> {
		private static final LinkedHashMap<String, TemporalField> OFFSET_DATE_TIME_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("year", ChronoField.YEAR);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("month", ChronoField.MONTH_OF_YEAR);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("day", ChronoField.DAY_OF_MONTH);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("hour", ChronoField.HOUR_OF_DAY);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("minute", ChronoField.MINUTE_OF_HOUR);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("second", ChronoField.SECOND_OF_MINUTE);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("nano", ChronoField.NANO_OF_SECOND);
			OFFSET_DATE_TIME_TEMPORAL_FIELDS.put("offsetSeconds", ChronoField.OFFSET_SECONDS);
		}

		private OffsetDateTimeTimestampHandler(DateTimeConverterOptions options) {
			super(ot -> DateTimeUtil.instantToMillis(ot.toInstant()),
					millis -> OffsetDateTime.ofInstant(DateTimeUtil.instantFromMillis(millis), options.getZoneId()),
					ot -> DateTimeUtil.instantToNanos(ot.toInstant()),
					nanos -> OffsetDateTime.ofInstant(DateTimeUtil.instantFromNanos(nanos), options.getZoneId()),
					OFFSET_DATE_TIME_TEMPORAL_FIELDS, OffsetDateTime::now);
		}
	}
}
