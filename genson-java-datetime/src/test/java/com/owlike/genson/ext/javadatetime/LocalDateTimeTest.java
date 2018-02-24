package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeTest extends JavaDateTimeTestBase {
	@Test
	public void testMillisSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId);
		Assert.assertEquals(millis.toString(), genson.serialize(dt));
	}

	@Test
	public void testNanosSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoAdjustment), defaultZoneId);
		Assert.assertEquals(totalNanos.toString(), genson.serialize(dt));
	}

	@Test
	public void testMillisDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId);
		Assert.assertEquals(dt, genson.deserialize(millis.toString(), LocalDateTime.class));
	}

	@Test
	public void testNanosDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoAdjustment), defaultZoneId);
		Assert.assertEquals(dt, genson.deserialize(totalNanos.toString(), LocalDateTime.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		LocalDateTime dt = LocalDateTime.now();
		String formattedValue = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		LocalDateTime dt = LocalDateTime.now();
		String formattedValue = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), LocalDateTime.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss");
		Genson genson = createFormatterGenson(formatter, LocalDateTime.class);
		LocalDateTime dt = LocalDateTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, LocalDateTime.class);
		LocalDateTime dt = LocalDateTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), LocalDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults1(){
		//Verify that time of day gets defaulted to 00:00:00.000000000 if not parsed by the formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu");
		Genson genson = createFormatterGenson(formatter, LocalDateTime.class);
		LocalDateTime dt = LocalDateTime.now();
		String formattedValue = formatter.format(dt);
		LocalDateTime dtWithDefaults = dt.withHour(0).withMinute(0).withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), LocalDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults2(){
		//Verify that date gets defaulted to 2000-01-01 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, LocalDateTime.class);
		LocalDateTime dt = LocalDateTime.now();
		String formattedValue = formatter.format(dt);
		LocalDateTime dtWithDefaults = dt.withYear(2000).withMonth(1).withDayOfMonth(1);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), LocalDateTime.class));
	}
}
