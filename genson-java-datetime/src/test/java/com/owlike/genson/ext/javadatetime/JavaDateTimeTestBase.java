package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@SuppressWarnings("unchecked")
class JavaDateTimeTestBase {
	ZoneId torontoZoneId = ZoneId.of("America/Toronto");
	ZoneId londonZoneId = ZoneId.of("Europe/London");
	ZoneId utcZoneId = ZoneId.of("UTC");
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

	Genson createTimestampGenson(TimestampFormat format){
		return createTimestampGenson(format, null);
	}

	Genson createTimestampGenson(TimestampFormat format, ZoneId zoneId) {
		JavaDateTimeBundle bundle = new JavaDateTimeBundle().setTimestampFormat(format);
		if(zoneId != null){
			bundle.setZoneId(zoneId);
		}
		return new GensonBuilder().useDateAsTimestamp(true).withBundle(bundle).create();
	}

	String toJsonQuotedString(String string){
		return "\"" + string + "\"";
	}

	String toJsonArray(Number... numbers){
		StringBuilder jsonArray = new StringBuilder("[");
		for(int i = 0; i < numbers.length; i++){
			jsonArray.append(numbers[i]);
			if(i != (numbers.length -1)){
				jsonArray.append(',');
			}
		}
		jsonArray.append(']');
		return jsonArray.toString();
	}
}
