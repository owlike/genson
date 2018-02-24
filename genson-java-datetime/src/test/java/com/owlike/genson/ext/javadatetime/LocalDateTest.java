package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		LocalDate dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId).toLocalDate();
		Assert.assertEquals("" + dt.toEpochDay(), genson.serialize(dt));
	}

	@Test
	public void testTimestampDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		LocalDate dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId).toLocalDate();
		Assert.assertEquals(dt, genson.deserialize("" + dt.toEpochDay(), LocalDate.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		LocalDate dt = LocalDate.now();
		String formattedValue = DateTimeFormatter.ISO_LOCAL_DATE.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		LocalDate dt = LocalDate.now();
		String formattedValue = DateTimeFormatter.ISO_LOCAL_DATE.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), LocalDate.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
		Genson genson = createFormatterGenson(formatter, LocalDate.class);
		LocalDate dt = LocalDate.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
		Genson genson = createFormatterGenson(formatter, LocalDate.class);
		LocalDate dt = LocalDate.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), LocalDate.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults1(){
		//Verify that year gets defaulted to 2000 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
		Genson genson = createFormatterGenson(formatter, LocalDate.class);
		LocalDate dt = LocalDate.now();
		String formattedValue = formatter.format(dt);
		LocalDate dtWithDefaults = dt.withYear(2000);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), LocalDate.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults2(){
		//Verify that date gets defaulted to 01-01 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu");
		Genson genson = createFormatterGenson(formatter, LocalDate.class);
		LocalDate dt = LocalDate.now();
		String formattedValue = formatter.format(dt);
		LocalDate dtWithDefaults = dt.withMonth(1).withDayOfMonth(1);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), LocalDate.class));
	}
}
