package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.stream.ObjectReader;

import java.time.MonthDay;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MonthDayConverter extends BaseTemporalAccessorConverter<MonthDay> {
	MonthDayConverter(DateTimeConverterOptions options) {
		super(options, new MonthDayTimestampHandler(options), MonthDay::from);
	}

	private static class MonthDayTimestampHandler extends TimestampHandler<MonthDay> {
		private static final LinkedHashMap<String, TemporalField> MONTH_DAY_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			MONTH_DAY_TEMPORAL_FIELDS.put("month", ChronoField.MONTH_OF_YEAR);
			MONTH_DAY_TEMPORAL_FIELDS.put("day", ChronoField.DAY_OF_MONTH);
		}

		MonthDayTimestampHandler(DateTimeConverterOptions options) {
			super(null, null, null, null,
					MONTH_DAY_TEMPORAL_FIELDS, MonthDay::now);
		}

		@Override
		protected MonthDay readFieldsFromObject(Supplier<MonthDay> instanceProvider, ObjectReader reader) {
			Map<String, Integer> values = new HashMap<>();
			reader.next();
			values.put(reader.name(), reader.valueAsInt());
			reader.next();
			values.put(reader.name(), reader.valueAsInt());
			return MonthDay.of(values.get("month"), values.get("day"));
		}

		@Override
		protected MonthDay readFieldsFromArray(Supplier<MonthDay> instanceProvider, ObjectReader reader) {
			reader.next();
			int month = reader.valueAsInt();
			reader.next();
			int day = reader.valueAsInt();
			return MonthDay.of(month, day);
		}
	}
}
