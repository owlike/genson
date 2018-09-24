package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class YearMonthTest extends JavaDateTimeTestBase {
	@Test
	public void testNumericSerialization() {
		Genson genson = createTimestampGenson(YearMonth.class, TimestampFormat.MILLIS);
		YearMonth yearMonth = YearMonth.of(1971, 1);
		Assert.assertEquals("12", genson.serialize(yearMonth));
	}

	@Test
	public void testNumericDeserialization() {
		Genson genson = createTimestampGenson(YearMonth.class, TimestampFormat.MILLIS);
		YearMonth yearMonth = YearMonth.of(1971, 1);
		Assert.assertEquals(yearMonth, genson.deserialize("12", YearMonth.class));
	}

	@Test
	public void testArraySerialization() {
		Genson genson = createTimestampGenson(YearMonth.class, TimestampFormat.ARRAY);
		YearMonth yearMonth = YearMonth.of(2000, 10);
		Assert.assertEquals(toJsonArray(2000, 10), genson.serialize(yearMonth));
	}

	@Test
	public void testArrayDeserialization() {
		Genson genson = createTimestampGenson(YearMonth.class, TimestampFormat.ARRAY);
		YearMonth yearMonth = YearMonth.of(2000, 10);
		Assert.assertEquals(yearMonth, genson.deserialize(toJsonArray(2000, 10), YearMonth.class));
	}

	@Test
	public void testObjectSerialization() {
		Genson genson = createTimestampGenson(YearMonth.class, TimestampFormat.OBJECT);
		YearMonth yearMonth = YearMonth.of(2010, 11);
		String json = "{\"year\":2010,\"month\":11}";
		Assert.assertEquals(json, genson.serialize(yearMonth));
	}

	@Test
	public void testObjectDeserialization() {
		Genson genson = createTimestampGenson(YearMonth.class, TimestampFormat.OBJECT);
		YearMonth yearMonth = YearMonth.of(2010, 11);
		String json = "{\"year\":2010,\"month\":11}";
		Assert.assertEquals(yearMonth, genson.deserialize(json, YearMonth.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsYearHyphenMonth(){
		Genson genson = createFormatterGenson();
		YearMonth dt = YearMonth.now();
		String formattedValue = DateTimeFormatter.ofPattern("uuuu-MM").format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsYearHyphenMonth(){
		Genson genson = createFormatterGenson();
		YearMonth dt = YearMonth.now();
		String formattedValue = DateTimeFormatter.ofPattern("uuuu-MM").format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), YearMonth.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/uuuu");
		Genson genson = createFormatterGenson(formatter, YearMonth.class);
		YearMonth dt = YearMonth.of(1999, 1);
		Assert.assertEquals(toJsonQuotedString("01/1999"), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/uuuu");
		Genson genson = createFormatterGenson(formatter, YearMonth.class);
		YearMonth dt = YearMonth.of(1999, 1);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("01/1999"), YearMonth.class));
	}

	@Test
	public void testCustomFormatDeserializationWithYearDefaulted(){
		//Year should default to 2000 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM");
		Genson genson = createFormatterGenson(formatter, YearMonth.class);
		YearMonth dt = YearMonth.of(2000, 1);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("01"), YearMonth.class));
	}

	@Test
	public void testCustomFormatDeserializationWithMonthDefaulted(){
		//Month should default to 1 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu");
		Genson genson = createFormatterGenson(formatter, YearMonth.class);
		YearMonth dt = YearMonth.of(2011, 1);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("2011"), YearMonth.class));
	}

}
