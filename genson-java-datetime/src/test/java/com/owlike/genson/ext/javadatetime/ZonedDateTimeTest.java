package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeTest extends JavaDateTimeTestBase {
	@Test
	public void testMillisSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId);
		Assert.assertEquals(millis.toString(), genson.serialize(dt));
	}

	@Test
	public void testNanosSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoAdjustment), defaultZoneId);
		Assert.assertEquals(totalNanos.toString(), genson.serialize(dt));
	}

	@Test
	public void testMillisDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId);
		Assert.assertEquals(dt, genson.deserialize(millis.toString(), ZonedDateTime.class));
	}

	@Test
	public void testNanosDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoAdjustment), defaultZoneId);
		Assert.assertEquals(dt, genson.deserialize(totalNanos.toString(), ZonedDateTime.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), ZonedDateTime.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss");
		Genson genson = createFormatterGenson(formatter, ZonedDateTime.class);
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, ZonedDateTime.class);
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), ZonedDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults1(){
		//Verify that time of day gets defaulted to 00:00:00.000000000 if not parsed by the formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu");
		Genson genson = createFormatterGenson(formatter, ZonedDateTime.class);
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = formatter.format(dt);
		ZonedDateTime dtWithDefaults = dt.withHour(0).withMinute(0).withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), ZonedDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults2(){
		//Verify that date gets defaulted to 2000-01-01 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, ZonedDateTime.class);
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = formatter.format(dt);
		ZonedDateTime dtWithDefaults = dt.withYear(2000).withMonth(1).withDayOfMonth(1);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), ZonedDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithCustomZoneId(){
		//Verify that the zoneId defaults to the one configured in the bundle if it cannot be parsed by the formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
		Genson londonGenson = createFormatterGenson(formatter, ZonedDateTime.class, londonZoneId);
		Genson torontoGenson = createFormatterGenson(formatter, ZonedDateTime.class, torontoZoneId);
		ZonedDateTime dt = ZonedDateTime.now();
		String formattedValue = formatter.format(dt);
		ZonedDateTime londonDt = londonGenson.deserialize(toJsonQuotedString(formattedValue), ZonedDateTime.class);
		ZonedDateTime torontoDt = torontoGenson.deserialize(toJsonQuotedString(formattedValue), ZonedDateTime.class);
		Assert.assertEquals(londonZoneId, londonDt.getZone());
		Assert.assertEquals(torontoZoneId, torontoDt.getZone());
	}
}
