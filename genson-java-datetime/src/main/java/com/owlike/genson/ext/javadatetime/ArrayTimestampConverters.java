package com.owlike.genson.ext.javadatetime;

import java.time.MonthDay;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

abstract class ArrayTimestampConverters<T extends TemporalAccessor> extends BaseArrayTimestampConverter<T> {
	private static final List<TemporalField> YEAR_FIELDS = Collections.singletonList(ChronoField.YEAR);
	private static final List<TemporalField> YEAR_MONTH_FIELDS = Arrays.asList(ChronoField.YEAR, ChronoField.MONTH_OF_YEAR);
	private static final List<TemporalField> MONTH_DAY_FIELDS = Arrays.asList(ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH);
	private static final List<TemporalField> OFFSET_TIME_FIELDS = Arrays.asList(
			ChronoField.HOUR_OF_DAY, ChronoField.MINUTE_OF_HOUR, ChronoField.SECOND_OF_MINUTE,
			ChronoField.NANO_OF_SECOND, ChronoField.OFFSET_SECONDS);

	private List<TemporalField> fields;
	private Supplier<T> instanceProvider;

	private ArrayTimestampConverters(DateTimeConverterOptions options, TemporalQuery<T> query, List<TemporalField> fields, Supplier<T> instanceProvider) {
		super(options, query);
		this.fields = fields;
		this.instanceProvider = instanceProvider;
	}

	static ArrayTimestampConverters<Year> year(DateTimeConverterOptions options){
		return new ArrayTimestampConverters<Year>(options, Year::from, YEAR_FIELDS, Year::now) {};
	}

	static ArrayTimestampConverters<YearMonth> yearMonth(DateTimeConverterOptions options){
		return new ArrayTimestampConverters<YearMonth>(options, YearMonth::from, YEAR_MONTH_FIELDS, YearMonth::now) {};
	}

	static ArrayTimestampConverters<OffsetTime> offsetTime(DateTimeConverterOptions options){
		return new ArrayTimestampConverters<OffsetTime>(options, OffsetTime::from, OFFSET_TIME_FIELDS, OffsetTime::now) {};
	}

	static ArrayTimestampConverters<MonthDay> monthDay(DateTimeConverterOptions options){
		return new ArrayTimestampConverters<MonthDay>(options, MonthDay::from, MONTH_DAY_FIELDS, MonthDay::now) {
			@Override
			MonthDay fromNumbers(List<Number> numbers) {
				return MonthDay.of(numbers.get(0).intValue(), numbers.get(1).intValue());
			}
		};
	}


	@Override
	List<Number> toNumbers(T object) {
		List<Number> nums = new ArrayList<>();
		for(TemporalField field: fields){
			nums.add(object.getLong(field));
		}
		return nums;
	}

	@Override
	T fromNumbers(List<Number> numbers) {
		Temporal obj = (Temporal) instanceProvider.get();
		for(int i = 0; i < fields.size(); i++){
			TemporalField field = fields.get(i);
			Number number = numbers.get(i);
			obj = obj.with(field, number.longValue());
		}
		return (T) obj;
	}
}
