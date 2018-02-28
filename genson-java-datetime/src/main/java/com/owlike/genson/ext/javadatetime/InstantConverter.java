package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.time.ZoneId;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

public class InstantConverter extends BaseTemporalAccessorConverter<Instant> {
	InstantConverter(DateTimeConverterOptions options) {
		super(options, new InstantTimestampHandler(options), Instant::from);
	}

	private static class InstantTimestampHandler extends TimestampHandler<Instant> {
		private static final LinkedHashMap<String, TemporalField> INSTANT_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			INSTANT_TEMPORAL_FIELDS.put("second", ChronoField.INSTANT_SECONDS);
			INSTANT_TEMPORAL_FIELDS.put("nano", ChronoField.NANO_OF_SECOND);
		}

		InstantTimestampHandler(DateTimeConverterOptions options) {
			super(DateTimeUtil::instantToMillis,
					DateTimeUtil::instantFromMillis,
					DateTimeUtil::instantToNanos,
					DateTimeUtil::instantFromNanos,
					INSTANT_TEMPORAL_FIELDS, Instant::now);
		}
	}
}
