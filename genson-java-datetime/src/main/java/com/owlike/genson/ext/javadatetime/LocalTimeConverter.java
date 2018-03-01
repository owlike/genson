package com.owlike.genson.ext.javadatetime;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.LinkedHashMap;

/**
 * Converter for values of type {@link LocalTime}
 */
public class LocalTimeConverter extends BaseTemporalAccessorConverter<LocalTime> {
	LocalTimeConverter(DateTimeConverterOptions options) {
		super(options, new LocalTimeTimestampHandler(options), LocalTime::from);
	}

	private static class LocalTimeTimestampHandler extends TimestampHandler<LocalTime> {
		private static final LinkedHashMap<String, TemporalField> LOCAL_TIME_TEMPORAL_FIELDS = new LinkedHashMap<>();
		static{
			LOCAL_TIME_TEMPORAL_FIELDS.put("hour", ChronoField.HOUR_OF_DAY);
			LOCAL_TIME_TEMPORAL_FIELDS.put("minute", ChronoField.MINUTE_OF_HOUR);
			LOCAL_TIME_TEMPORAL_FIELDS.put("second", ChronoField.SECOND_OF_MINUTE);
			LOCAL_TIME_TEMPORAL_FIELDS.put("nano", ChronoField.NANO_OF_SECOND);
		}

		private LocalTimeTimestampHandler(DateTimeConverterOptions options) {
			super(lt -> DateTimeUtil.getMillis(lt.toSecondOfDay(), lt.getNano()),
					LocalTimeConverter::localTimeFromMillisOfDay,
					LocalTime::toNanoOfDay,
					LocalTime::ofNanoOfDay,
					LOCAL_TIME_TEMPORAL_FIELDS, LocalTime::now);
		}

	}

	private static LocalTime localTimeFromMillisOfDay(long millis){
		long seconds = DateTimeUtil.getSecondsFromMillis(millis);
		long nanos = DateTimeUtil.getNanosFromMillis(millis);
		return LocalTime.ofSecondOfDay(seconds).withNano((int) nanos);
	}
}
