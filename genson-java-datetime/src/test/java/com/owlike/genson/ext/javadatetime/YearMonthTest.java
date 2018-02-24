package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class YearMonthTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerialization() {
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		YearMonth year = YearMonth.of(2000, 2);
		Assert.assertEquals(toJsonArray(2000, 2), genson.serialize(year));
	}

	@Test
	public void testTimestampDeserialization() {
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		YearMonth year = YearMonth.of(2011, 11);
		Assert.assertEquals(year, genson.deserialize(toJsonArray(2011, 11), YearMonth.class));
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
