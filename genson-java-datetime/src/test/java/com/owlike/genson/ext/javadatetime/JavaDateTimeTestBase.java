package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;

@SuppressWarnings("unchecked")
class JavaDateTimeTestBase {
	ZoneId torontoZoneId = ZoneId.of("America/Toronto");
	ZoneId londonZoneId = ZoneId.of("Europe/London");
	ZoneId defaultZoneId = ZoneId.systemDefault();

	Genson createFormatterGenson(DateTimeFormatter dateTimeFormatter, Class<? extends TemporalAccessor> formatterType) {
		return createFormatterGenson(dateTimeFormatter, formatterType, null);
	}


	Genson createFormatterGenson(DateTimeFormatter dateTimeFormatter, Class<? extends TemporalAccessor> formatterType, ZoneId zoneId) {
		JavaDateTimeBundle bundle = new JavaDateTimeBundle().setFormatter(formatterType, dateTimeFormatter);
		if(zoneId != null){
			bundle.setZoneId(zoneId);
		}
		return new GensonBuilder().useDateAsTimestamp(false).withBundle(bundle).create();
	}

	Genson createFormatterGenson(){
		return createFormatterGenson(null);
	}

	Genson createFormatterGenson(ZoneId zoneId) {
		JavaDateTimeBundle bundle = new JavaDateTimeBundle();
		if(zoneId != null){
			bundle.setZoneId(zoneId);
		}
		return new GensonBuilder().useDateAsTimestamp(false).withBundle(bundle).create();
	}

	Genson createTimestampGenson(){
		return new GensonBuilder().useDateAsTimestamp(true).withBundle(new JavaDateTimeBundle()).create();
	}

	Genson createTimestampGenson(Class<? extends TemporalAccessor> clazz, TimestampFormat format){
		return createTimestampGenson(clazz, format, null);
	}

	Genson createTimestampGenson(Class<? extends  TemporalAccessor> clazz,  TimestampFormat format, ZoneId zoneId) {
		JavaDateTimeBundle bundle = new JavaDateTimeBundle().setTemporalAccessorTimestampFormat(clazz, format);
		if(zoneId != null){
			bundle.setZoneId(zoneId);
		}
		return new GensonBuilder().useDateAsTimestamp(true).withBundle(bundle).create();
	}

	Genson createTemporalAmountTimestampGenson(Class<? extends TemporalAmount> clazz, TimestampFormat format){
		return createTemporalAmountTimestampGenson(clazz, format, null);
	}

	Genson createTemporalAmountTimestampGenson(Class<? extends TemporalAmount> clazz, TimestampFormat format, ZoneId zoneId) {
		JavaDateTimeBundle bundle = new JavaDateTimeBundle().setTemporalAmountTimestampFormat(clazz, format);
		if(zoneId != null){
			bundle.setZoneId(zoneId);
		}
		return new GensonBuilder().useDateAsTimestamp(true).withBundle(bundle).create();
	}

	String toJsonQuotedString(String string){
		return "\"" + string + "\"";
	}

	String toJsonArray(Object... objects){
		StringBuilder jsonArray = new StringBuilder("[");
		for(int i = 0; i < objects.length; i++){
			jsonArray.append(objects[i]);
			if(i != (objects.length -1)){
				jsonArray.append(',');
			}
		}
		jsonArray.append(']');
		return jsonArray.toString();
	}
}
