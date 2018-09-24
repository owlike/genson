package com.owlike.genson.ext.javadatetime;

import java.time.OffsetTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

/**
 * Converter for values of type {@link OffsetTime}
 */
public class OffsetTimeConverter extends BaseTemporalAccessorConverter<OffsetTime> {
	OffsetTimeConverter(DateTimeConverterOptions options) {
		super(options, new OffsetTimeTimestampHandler(options), OffsetTime::from);
	}

	private static class OffsetTimeTimestampHandler extends TimestampHandler<OffsetTime> {
		private static final LinkedHashMap<String, TemporalField> OFFSET_TIME_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			OFFSET_TIME_TEMPORAL_FIELDS .put("hour", ChronoField.HOUR_OF_DAY);
			OFFSET_TIME_TEMPORAL_FIELDS .put("minute", ChronoField.MINUTE_OF_HOUR);
			OFFSET_TIME_TEMPORAL_FIELDS .put("second", ChronoField.SECOND_OF_MINUTE);
			OFFSET_TIME_TEMPORAL_FIELDS .put("nano", ChronoField.NANO_OF_SECOND);
			OFFSET_TIME_TEMPORAL_FIELDS .put("offsetSeconds", ChronoField.OFFSET_SECONDS);
		}

		private OffsetTimeTimestampHandler(DateTimeConverterOptions options) {
			super(null, null, null, null,
					OFFSET_TIME_TEMPORAL_FIELDS , OffsetTime::now);
		}
	}
}
