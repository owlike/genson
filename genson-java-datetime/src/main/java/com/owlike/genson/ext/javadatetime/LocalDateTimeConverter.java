package com.owlike.genson.ext.javadatetime;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

public class LocalDateTimeConverter extends BaseTemporalAccessorConverter<LocalDateTime> {
	LocalDateTimeConverter(DateTimeConverterOptions options) {
		super(options, new LocalDateTimeTimestampHandler(options), LocalDateTime::from);
	}

	private static class LocalDateTimeTimestampHandler extends TimestampHandler<LocalDateTime> {
		private static final LinkedHashMap<String, TemporalField> LOCAL_DATE_TIME_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("year", ChronoField.YEAR);
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("month", ChronoField.MONTH_OF_YEAR);
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("day", ChronoField.DAY_OF_MONTH);
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("hour", ChronoField.HOUR_OF_DAY);
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("minute", ChronoField.MINUTE_OF_HOUR);
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("second", ChronoField.SECOND_OF_MINUTE);
			LOCAL_DATE_TIME_TEMPORAL_FIELDS.put("nano", ChronoField.NANO_OF_SECOND);
		}

		LocalDateTimeTimestampHandler(DateTimeConverterOptions options) {
			super(lt -> DateTimeUtil.instantToMillis(lt.atZone(options.getZoneId()).toInstant()),
					millis -> LocalDateTime.ofInstant(DateTimeUtil.instantFromMillis(millis), options.getZoneId()),
					lt -> DateTimeUtil.instantToNanos(lt.atZone(options.getZoneId()).toInstant()),
					nanos -> LocalDateTime.ofInstant(DateTimeUtil.instantFromNanos(nanos), options.getZoneId()),
					LOCAL_DATE_TIME_TEMPORAL_FIELDS, LocalDateTime::now);
		}
	}
}
