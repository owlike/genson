package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.convert.ContextualFactory;
import com.owlike.genson.ext.GensonBundle;
import com.owlike.genson.ext.javadatetime.annotation.JsonTimestampFormat;
import com.owlike.genson.ext.javadatetime.annotation.JsonZoneId;
import com.owlike.genson.reflect.BeanProperty;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class JavaDateTimeBundle extends GensonBundle {
	private ZoneId zoneId = ZoneId.systemDefault();
	private TimestampFormat timestampFormat = TimestampFormat.MILLIS;

	private Map<Class<?>, ConverterGenerator> converterGenerators = new HashMap<>();
	{
		registerConverterGenerator(Instant.class, InstantTimestampableConverter::instant );
		registerConverterGenerator(ZonedDateTime.class, InstantTimestampableConverter::zonedDateTime );
		registerConverterGenerator(OffsetDateTime.class, InstantTimestampableConverter::offsetDateTime );
		registerConverterGenerator(LocalDateTime.class, InstantTimestampableConverter::localDateTime );
		registerConverterGenerator(LocalDate.class, LocalTimestampableConverter::localDate);
		registerConverterGenerator(LocalTime.class, LocalTimestampableConverter::localTime);
		registerConverterGenerator(Year.class, ArrayTimestampConverters::year);
		registerConverterGenerator(YearMonth.class, ArrayTimestampConverters::yearMonth);
		registerConverterGenerator(MonthDay.class, ArrayTimestampConverters::monthDay);
		registerConverterGenerator(OffsetTime.class, ArrayTimestampConverters::offsetTime);
		registerConverterGenerator(Period.class, TemporalAmountConverter::period);
		registerConverterGenerator(Duration.class, TemporalAmountConverter::duration);
	}

	private Map<Class<?>, DateTimeFormatter> formatters = new HashMap<>();
	{
		formatters.put(Instant.class, DateTimeFormatter.ISO_INSTANT);
		formatters.put(ZonedDateTime.class, DateTimeFormatter.ISO_ZONED_DATE_TIME);
		formatters.put(OffsetDateTime.class, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		formatters.put(LocalDateTime.class, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		formatters.put(LocalDate.class, DateTimeFormatter.ISO_LOCAL_DATE);
		formatters.put(LocalTime.class, DateTimeFormatter.ISO_LOCAL_TIME);
		formatters.put(OffsetTime.class, DateTimeFormatter.ISO_OFFSET_TIME);
		formatters.put(Year.class, DateTimeFormatter.ofPattern("uuuu"));
		formatters.put(YearMonth.class, DateTimeFormatter.ofPattern("uuuu-MM"));
		formatters.put(MonthDay.class, DateTimeFormatter.ofPattern("MM-dd"));
	}

	@Override
	public void configure(GensonBuilder builder) {
		boolean asTimestamp = builder.isDateAsTimestamp();
		builder.withContextualFactory(new JavaDateTimeContextualFactory(asTimestamp, timestampFormat));
		for(Map.Entry<Class<?>, ConverterGenerator> converterGeneratorEntry: converterGenerators.entrySet()){
			Class<?> clazz = converterGeneratorEntry.getKey();
			ConverterGenerator<?> converterGenerator = converterGeneratorEntry.getValue();
			DateTimeFormatter formatter = formatters.get(clazz);
			DateTimeConverterOptions options = new DateTimeConverterOptions(clazz, formatter, asTimestamp, timestampFormat, zoneId);
			Converter converter = converterGenerator.createConverter(options);
			builder.withConverters(converter);
		}
	}

	private <T> void registerConverterGenerator(Class<T> clazz, Function<DateTimeConverterOptions, Converter<T>> converterGeneratorFunction){
		ConverterGenerator converterGenerator = new ConverterGenerator<>(converterGeneratorFunction);
		converterGenerators.put(clazz, converterGenerator);
	}

	public JavaDateTimeBundle setFormatter(Class<? extends TemporalAccessor> clazz, DateTimeFormatter formatter){
		formatters.put(clazz, formatter);
		return this;
	}

	public JavaDateTimeBundle setZoneId(ZoneId zoneId) {
		this.zoneId = zoneId;
		return this;
	}

	public JavaDateTimeBundle setTimestampFormat(TimestampFormat timestampFormat) {
		this.timestampFormat = timestampFormat;
		return this;
	}

	private class JavaDateTimeContextualFactory implements ContextualFactory {
		private boolean timestampByDefault;
		private TimestampFormat defaultTimestampFormat;

		private JavaDateTimeContextualFactory(boolean timestampByDefault, TimestampFormat defaultTimestampFormat) {
			this.timestampByDefault = timestampByDefault;
			this.defaultTimestampFormat = defaultTimestampFormat;
		}

		@Override
		public Converter create(BeanProperty property, Genson genson) {
			if(hasRelevantAnnotation(property)) {
				Class<?> rawClass = property.getRawClass();
				for (Map.Entry<Class<?>, ConverterGenerator> supportedType : converterGenerators.entrySet()) {
					Class<?> clazz = supportedType.getKey();
					ConverterGenerator generator = supportedType.getValue();
					if (clazz.isAssignableFrom(rawClass)) {
						DateTimeConverterOptions options = createOptions(property, clazz);
						return generator.createConverter(options);
					}
				}
			}

			return null;
		}

		private boolean hasRelevantAnnotation(BeanProperty property){
			JsonDateFormat formatAnn = property.getAnnotation(JsonDateFormat.class);
			JsonZoneId zoneIdAnn = property.getAnnotation(JsonZoneId.class);
			JsonTimestampFormat timestampFormatAnn = property.getAnnotation(JsonTimestampFormat.class);
			return formatAnn != null || zoneIdAnn != null || timestampFormatAnn != null;
		}

		private DateTimeConverterOptions createOptions(BeanProperty property, Class<?> clazz) {
			JsonDateFormat jsonDateFormat = property.getAnnotation(JsonDateFormat.class);
			DateTimeFormatter formatter = getFormatter(jsonDateFormat, formatters.get(clazz));
			boolean asTimestamp = jsonDateFormat == null ? timestampByDefault : jsonDateFormat.asTimeInMillis();
			ZoneId zoneId = getZoneId(property);
			TimestampFormat timestampFormat = getTimestampFormat(property, defaultTimestampFormat);
			return new DateTimeConverterOptions(clazz, formatter, asTimestamp, timestampFormat, zoneId);
		}

		private DateTimeFormatter getFormatter(JsonDateFormat ann, DateTimeFormatter defaultFormatter) {
			if(ann == null || ann.value().isEmpty()){
				return defaultFormatter;
			}
			else {
				Locale locale = ann.lang().isEmpty() ? Locale.getDefault() : new Locale(ann.lang());
				return DateTimeFormatter.ofPattern(ann.value()).withLocale(locale);
			}
		}

		private ZoneId getZoneId(BeanProperty property){
			JsonZoneId ann = property.getAnnotation(JsonZoneId.class);
			return ann == null || ann.value().isEmpty() ? zoneId : ZoneId.of(ann.value());
		}

		private TimestampFormat getTimestampFormat(BeanProperty property, TimestampFormat defaultTimestampFormat) {
			JsonTimestampFormat ann = property.getAnnotation(JsonTimestampFormat.class);
			return ann == null ? defaultTimestampFormat : ann.value();
		}

	}

	private class ConverterGenerator<T>{
		private Function<DateTimeConverterOptions, Converter<T>> converterGeneratorFunction;

		ConverterGenerator(Function<DateTimeConverterOptions, Converter<T>> converterGeneratorFunction) {
			this.converterGeneratorFunction = converterGeneratorFunction;
		}

		Converter<T> createConverter(DateTimeConverterOptions options){
			return converterGeneratorFunction.apply(options);
		}
	}
}
