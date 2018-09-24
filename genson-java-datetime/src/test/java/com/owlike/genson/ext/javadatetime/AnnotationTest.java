package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.ext.javadatetime.annotation.JsonTimestampFormat;
import com.owlike.genson.ext.javadatetime.annotation.JsonZoneId;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AnnotationTest extends JavaDateTimeTestBase {

	@Test
	public void testCustomFormat(){
		//Ser/Deser a zoneId using the pattern provided by the annotation (years only)
		//The remaining fields (month, date etc) will be defaulted
		Genson formatterGenson = createFormatterGenson();
		Genson timestampGenson = createTimestampGenson(ZonedDateTime.class, TimestampFormat.NANOS);
		PojoWithCustomFormat pojo = new PojoWithCustomFormat();
		pojo.dateTime = ZonedDateTime.of(2011, 2, 3, 4, 0, 0, 0, defaultZoneId);
		ZonedDateTime expectedDeserialized = pojo.dateTime.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withNano(0);
		String formatterJson = formatterGenson.serialize(pojo);
		String timestampJson = timestampGenson.serialize(pojo);
		String expectedJson = "{\"dateTime\":\"2011\"}";
		Assert.assertEquals(expectedJson, formatterJson);
		Assert.assertEquals(expectedJson, timestampJson);
		Assert.assertEquals(expectedDeserialized, formatterGenson.deserialize(expectedJson, PojoWithCustomFormat.class).dateTime);
		Assert.assertEquals(expectedDeserialized, timestampGenson.deserialize(expectedJson, PojoWithCustomFormat.class).dateTime);
	}

	@Test
	public void testCustomZoneId(){
		//Create a ZonedDateTime using toronto zoneId and a genson with a JavaDateTimeBundle that uses the torontoZoneId
		//Ser/Deser using a formatter that doesn't include ZoneId and expect the result to use the London zoneId provided by the annotation
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss.nnnnnnnnn");
		Genson formatterGenson = createFormatterGenson(formatter, ZonedDateTime.class, torontoZoneId);
		PojoWithCustomZoneId pojo = new PojoWithCustomZoneId();
		pojo.dateTime = ZonedDateTime.of(2011, 2, 3, 4, 0, 0, 0, torontoZoneId);
		ZonedDateTime expectedDeserialized = pojo.dateTime.withZoneSameInstant(londonZoneId);
		String formatterJson = formatterGenson.serialize(pojo);
		String expectedJson = "{\"dateTime\":\"" +formatter.format(expectedDeserialized) + "\"}";
		Assert.assertEquals(expectedJson, formatterJson);
		Assert.assertEquals(expectedDeserialized, formatterGenson.deserialize(expectedJson, PojoWithCustomZoneId.class).dateTime);
	}

	@Test
	public void testCustomTimestampFormatNanos(){
		//Create genson which uses millis as default timestamp format and expect the annotation to override
		Genson timestampGenson = createTimestampGenson(ZonedDateTime.class, TimestampFormat.MILLIS);
		PojoWithCustomTimestampFormatNanos pojo = new PojoWithCustomTimestampFormatNanos();
		pojo.dateTime = ZonedDateTime.of(2011, 2, 3, 4, 0, 0, 0, defaultZoneId);
		Long totalNanos = DateTimeUtil.getNanos(pojo.dateTime.toEpochSecond(), pojo.dateTime.getNano());
		String timestampJson = timestampGenson.serialize(pojo);
		String expectedJson = "{\"dateTime\":" + totalNanos + "}";
		Assert.assertEquals(expectedJson, timestampJson);
		Assert.assertEquals(pojo.dateTime, timestampGenson.deserialize(expectedJson, PojoWithCustomTimestampFormatNanos.class).dateTime);
	}

	@Test
	public void testCustomTimestampFormatArray(){
		//Create genson which uses millis as default timestamp format and expect the annotation to override
		Genson timestampGenson = createTimestampGenson(ZonedDateTime.class, TimestampFormat.MILLIS);
		PojoWithCustomTimestampFormatArray pojo = new PojoWithCustomTimestampFormatArray();
		pojo.dateTime = ZonedDateTime.of(2011, 2, 3, 4, 0, 0, 0, defaultZoneId);
		String timestampJson = timestampGenson.serialize(pojo);
		String expectedJson = "{\"dateTime\":" + toJsonArray(2011, 2, 3, 4, 0, 0, 0, toJsonQuotedString(defaultZoneId.toString())) + "}";
		Assert.assertEquals(expectedJson, timestampJson);
		Assert.assertEquals(pojo.dateTime, timestampGenson.deserialize(expectedJson, PojoWithCustomTimestampFormatArray.class).dateTime);
	}

	@Test
	public void testCustomTimestampFormatObject(){
		//Create genson which uses millis as default timestamp format and expect the annotation to override
		Genson timestampGenson = createTimestampGenson(ZonedDateTime.class, TimestampFormat.MILLIS);
		PojoWithCustomTimestampFormatObject pojo = new PojoWithCustomTimestampFormatObject();
		pojo.dateTime = ZonedDateTime.of(2011, 2, 3, 4, 0, 0, 0, defaultZoneId);
		String timestampJson = timestampGenson.serialize(pojo);
		String expectedJson = "{\"dateTime\":{\"year\":2011,\"month\":2,\"day\":3,\"hour\":4,\"minute\":0,\"second\":0,\"nano\":0,\"zoneId\":\"" + defaultZoneId.getId() +  "\"}}";
		Assert.assertEquals(expectedJson, timestampJson);
		Assert.assertEquals(pojo.dateTime, timestampGenson.deserialize(expectedJson, PojoWithCustomTimestampFormatObject.class).dateTime);
	}



	static class PojoWithCustomFormat{
		@JsonDateFormat("uuuu")
		ZonedDateTime dateTime;
	}

	static class PojoWithCustomZoneId{
		@JsonZoneId("Europe/London")
		ZonedDateTime dateTime;
	}

	static class PojoWithCustomTimestampFormatNanos {
		@JsonTimestampFormat(TimestampFormat.NANOS)
		ZonedDateTime dateTime;
	}

	static class PojoWithCustomTimestampFormatArray {
		@JsonTimestampFormat(TimestampFormat.ARRAY)
		ZonedDateTime dateTime;
	}

	static class PojoWithCustomTimestampFormatObject {
		@JsonTimestampFormat(TimestampFormat.OBJECT)
		ZonedDateTime dateTime;
	}
}
