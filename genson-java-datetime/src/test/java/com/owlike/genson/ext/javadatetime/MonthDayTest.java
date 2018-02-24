package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

public class MonthDayTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerialization() {
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		MonthDay year = MonthDay.of(12, 2);
		Assert.assertEquals(toJsonArray(12, 2), genson.serialize(year));
	}

	@Test
	public void testTimestampDeserialization() {
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		MonthDay year = MonthDay.of(1, 11);
		Assert.assertEquals(year, genson.deserialize(toJsonArray(1, 11), MonthDay.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsMonthHyphenDay(){
		Genson genson = createFormatterGenson();
		MonthDay dt = MonthDay.now();
		String formattedValue = DateTimeFormatter.ofPattern("MM-dd").format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsMonthHyphenDay(){
		Genson genson = createFormatterGenson();
		MonthDay dt = MonthDay.now();
		String formattedValue = DateTimeFormatter.ofPattern("MM-dd").format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), MonthDay.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
		Genson genson = createFormatterGenson(formatter, MonthDay.class);
		MonthDay dt = MonthDay.of(10, 1);
		Assert.assertEquals(toJsonQuotedString("01/10"), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
		Genson genson = createFormatterGenson(formatter, MonthDay.class);
		MonthDay dt = MonthDay.of(10, 1);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("01/10"), MonthDay.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDayDefaulted(){
		//Day should default to 1 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
		Genson genson = createFormatterGenson(formatter, MonthDay.class);
		MonthDay dt = MonthDay.of(9, 1);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("09"), MonthDay.class));
	}

	@Test
	public void testCustomFormatDeserializationWithMonthDefaulted(){
		//Month should default to 1 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
		Genson genson = createFormatterGenson(formatter, MonthDay.class);
		MonthDay dt = MonthDay.of(1, 14);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("14"), MonthDay.class));
	}

}
