package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeTest extends JavaDateTimeTestBase {
	@Test
	public void testMillisSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		LocalTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(4534654564653L), defaultZoneId).toLocalTime();
		Long millis = DateTimeUtil.getMillis(dt.toSecondOfDay(), dt.getNano());
		Assert.assertEquals(millis.toString(), genson.serialize(dt));
	}

	@Test
	public void testNanosSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		LocalTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(321L, 123456789L), defaultZoneId).toLocalTime();
		Long totalNanos = DateTimeUtil.getNanos(dt.toSecondOfDay(), dt.getNano());
		Assert.assertEquals(totalNanos.toString(), genson.serialize(dt));
	}

	@Test
	public void testMillisDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		LocalTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(5357647337L), defaultZoneId).toLocalTime();
		Long millis = DateTimeUtil.getMillis(dt.toSecondOfDay(), dt.getNano());
		Assert.assertEquals(dt, genson.deserialize(millis.toString(), LocalTime.class));
	}

	@Test
	public void testNanosDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		LocalTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(321L, 123456789L), defaultZoneId).toLocalTime();
		Long totalNanos = DateTimeUtil.getNanos(dt.toSecondOfDay(), dt.getNano());
		Assert.assertEquals(dt, genson.deserialize(totalNanos.toString(), LocalTime.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		LocalTime dt = LocalTime.now();
		String formattedValue = DateTimeFormatter.ISO_LOCAL_TIME.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		LocalTime dt = LocalTime.now();
		String formattedValue = DateTimeFormatter.ISO_LOCAL_TIME.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), LocalTime.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		Genson genson = createFormatterGenson(formatter, LocalTime.class);
		LocalTime dt = LocalTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, LocalTime.class);
		LocalTime dt = LocalTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), LocalTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults1(){
		//Verify that seconds and nanos gets defaulted to 0 if not parsed by formater
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		Genson genson = createFormatterGenson(formatter, LocalTime.class);
		LocalTime dt = LocalTime.now().withSecond(32).withNano(34765);
		String formattedValue = formatter.format(dt);
		LocalTime dtWithDefaults = dt.withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), LocalTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults2(){
		//Verify that minute gets defaulted to 0 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
		Genson genson = createFormatterGenson(formatter, LocalTime.class);
		LocalTime dt = LocalTime.now().withMinute(34);
		String formattedValue = formatter.format(dt);
		LocalTime dtWithDefaults = dt.withMinute(0).withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), LocalTime.class));
	}
}
