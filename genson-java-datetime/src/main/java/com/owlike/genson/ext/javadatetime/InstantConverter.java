package com.owlike.genson.ext.javadatetime;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

/**
 * Converter for values of type {@link Instant}
 */
class InstantConverter extends BaseTemporalAccessorConverter<Instant> {
	InstantConverter(DateTimeConverterOptions options) {
		super(options, new InstantTimestampHandler(options), Instant::from);
	}

	private static class InstantTimestampHandler extends TimestampHandler<Instant> {
		private static final LinkedHashMap<String, TemporalField> INSTANT_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			INSTANT_TEMPORAL_FIELDS.put("second", ChronoField.INSTANT_SECONDS);
			INSTANT_TEMPORAL_FIELDS.put("nano", ChronoField.NANO_OF_SECOND);
		}

		private InstantTimestampHandler(DateTimeConverterOptions options) {
			super(DateTimeUtil::instantToMillis,
					DateTimeUtil::instantFromMillis,
					DateTimeUtil::instantToNanos,
					DateTimeUtil::instantFromNanos,
					INSTANT_TEMPORAL_FIELDS, Instant::now);
		}
	}
}
