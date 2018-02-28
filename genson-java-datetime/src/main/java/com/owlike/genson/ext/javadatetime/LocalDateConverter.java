package com.owlike.genson.ext.javadatetime;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

public class LocalDateConverter extends BaseTemporalAccessorConverter<LocalDate> {
	LocalDateConverter(DateTimeConverterOptions options) {
		super(options, new LocalDateTimestampHandler(options), LocalDate::from);
	}

	private static class LocalDateTimestampHandler extends TimestampHandler<LocalDate> {
		private static final LinkedHashMap<String, TemporalField> LOCAL_DATE_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			LOCAL_DATE_TEMPORAL_FIELDS.put("year", ChronoField.YEAR);
			LOCAL_DATE_TEMPORAL_FIELDS.put("month", ChronoField.MONTH_OF_YEAR);
			LOCAL_DATE_TEMPORAL_FIELDS.put("day", ChronoField.DAY_OF_MONTH);
		}

		LocalDateTimestampHandler(DateTimeConverterOptions options) {
			super(LocalDate::toEpochDay, LocalDate::ofEpochDay, LocalDate::toEpochDay, LocalDate::ofEpochDay,
					LOCAL_DATE_TEMPORAL_FIELDS, LocalDate::now);
		}
	}
}
