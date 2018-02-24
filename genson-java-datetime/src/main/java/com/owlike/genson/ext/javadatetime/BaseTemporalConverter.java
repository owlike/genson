package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Converter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

abstract class BaseTemporalConverter<T extends TemporalAccessor> implements Converter<T> {
	private DateTimeFormatter formatter;
	private TemporalQuery<T> query;
	private boolean asTimestamp;
	private TimestampFormat timestampFormat;
	private ZoneId zoneId;

	BaseTemporalConverter(DateTimeConverterOptions options, TemporalQuery<T> query) {
		this.formatter = DateTimeUtil.createFormatterWithDefaults(options.getDateTimeFormatter(), options.getZoneId());
		this.query = query;
		this.asTimestamp = options.isAsTimestamp();
		this.timestampFormat = options.getTimestampFormat();
		this.zoneId = options.getZoneId();
	}

	T parseValue(CharSequence seq){
		T value = formatter.parse(seq, query);
		if(value instanceof OffsetDateTime){
			value = (T) DateTimeUtil.correctOffset((OffsetDateTime) value, zoneId);
		}
		return value;
	}

	String formatValue(T val){
		return formatter.format(val);
	}

	boolean isAsTimestamp() {
		return asTimestamp;
	}

	TimestampFormat getTimestampFormat() {
		return timestampFormat;
	}
}
