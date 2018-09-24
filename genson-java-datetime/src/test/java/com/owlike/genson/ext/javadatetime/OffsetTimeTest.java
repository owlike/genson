package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class OffsetTimeTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerialization(){
		Genson genson = createTimestampGenson();
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(4534654564653L), defaultZoneId);
		OffsetTime dt = ldt.toLocalTime().atOffset(defaultZoneId.getRules().getOffset(ldt));
		String jsonArray = toJsonArray(dt.getHour(), dt.getMinute(), dt.getSecond(), dt.getNano(), dt.getOffset().getTotalSeconds());
		Assert.assertEquals(jsonArray, genson.serialize(dt));
	}

	@Test
	public void testTimestampDeserialization(){
		Genson genson = createTimestampGenson();
		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(5357647337L), defaultZoneId);
		OffsetTime dt = ldt.toLocalTime().atOffset(defaultZoneId.getRules().getOffset(ldt));
		String jsonArray = toJsonArray(dt.getHour(), dt.getMinute(), dt.getSecond(), dt.getNano(), dt.getOffset().getTotalSeconds());
		Assert.assertEquals(dt, genson.deserialize(jsonArray, OffsetTime.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		OffsetTime dt = OffsetTime.now();
		String formattedValue = DateTimeFormatter.ISO_OFFSET_TIME.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		OffsetTime dt = OffsetTime.now();
		String formattedValue = DateTimeFormatter.ISO_OFFSET_TIME.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), OffsetTime.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		Genson genson = createFormatterGenson(formatter, OffsetTime.class);
		OffsetTime dt = OffsetTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, OffsetTime.class);
		OffsetTime dt = OffsetTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), OffsetTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults1(){
		//Verify that seconds and nanos gets defaulted to 0 if not parsed by formater
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		Genson genson = createFormatterGenson(formatter, OffsetTime.class);
		OffsetTime dt = OffsetTime.now().withSecond(32).withNano(34765);
		String formattedValue = formatter.format(dt);
		OffsetTime dtWithDefaults = dt.withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), OffsetTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults2(){
		//Verify that minute gets defaulted to 0 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH");
		Genson genson = createFormatterGenson(formatter, OffsetTime.class);
		OffsetTime dt = OffsetTime.now().withMinute(34);
		String formattedValue = formatter.format(dt);
		OffsetTime dtWithDefaults = dt.withMinute(0).withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), OffsetTime.class));
	}
}
