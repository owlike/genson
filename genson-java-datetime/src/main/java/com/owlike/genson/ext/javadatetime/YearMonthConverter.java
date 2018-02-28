package com.owlike.genson.ext.javadatetime;

import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

class YearMonthConverter extends BaseTemporalAccessorConverter<YearMonth> {
	private static final YearMonth EPOCH_YEAR_MONTH = YearMonth.of(1970, 1);

	YearMonthConverter(DateTimeConverterOptions options) {
		super(options, new YearMonthTimestampHandler(options), YearMonth::from);
	}

	private static class YearMonthTimestampHandler extends TimestampHandler<YearMonth> {
		private static final LinkedHashMap<String, TemporalField> YEAR_MONTH_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			YEAR_MONTH_TEMPORAL_FIELDS.put("year", ChronoField.YEAR);
			YEAR_MONTH_TEMPORAL_FIELDS.put("month", ChronoField.MONTH_OF_YEAR);
		}

		YearMonthTimestampHandler(DateTimeConverterOptions options) {
			super(YearMonthConverter::getEpochMonth, YearMonthConverter::fromEpochMonth,
					YearMonthConverter::getEpochMonth, YearMonthConverter::fromEpochMonth,
					YEAR_MONTH_TEMPORAL_FIELDS, YearMonth::now);
		}
	}

	private static long getEpochMonth(YearMonth yearMonth){
		return EPOCH_YEAR_MONTH.until(yearMonth, ChronoUnit.MONTHS);
	}

	private static YearMonth fromEpochMonth(long months){
		return EPOCH_YEAR_MONTH.plus(months, ChronoUnit.MONTHS);
	}
}
