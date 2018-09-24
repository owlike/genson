package com.owlike.genson.ext.javadatetime;

import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

/**
 * Converter for values of type {@link Year}
 */
public class YearConverter extends BaseTemporalAccessorConverter<Year> {
	YearConverter(DateTimeConverterOptions options) {
		super(options, new YearTimestampHandler(options), Year::from);
	}

	private static class YearTimestampHandler extends TimestampHandler<Year> {
		private static final LinkedHashMap<String, TemporalField> YEAR_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			YEAR_TEMPORAL_FIELDS.put("year", ChronoField.YEAR);
		}

		private YearTimestampHandler(DateTimeConverterOptions options) {
			super(y -> y.getLong(ChronoField.YEAR), l -> Year.of(l.intValue()), y -> y.getLong(ChronoField.YEAR), l -> Year.of(l.intValue()),
					YEAR_TEMPORAL_FIELDS, Year::now);
		}
	}
}
