package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Year;
import java.time.format.DateTimeFormatter;

public class YearTest extends JavaDateTimeTestBase {
	@Test
	public void testNumericSerialization() {
		Genson genson = createTimestampGenson(Year.class, TimestampFormat.MILLIS);
		Year year = Year.of(2000);
		Assert.assertEquals("2000", genson.serialize(year));
	}

	@Test
	public void testNumericDeserialization() {
		Genson genson = createTimestampGenson(Year.class, TimestampFormat.MILLIS);
		Year year = Year.of(2011);
		Assert.assertEquals(year, genson.deserialize("2011", Year.class));
	}

	@Test
	public void testArraySerialization() {
		Genson genson = createTimestampGenson(Year.class, TimestampFormat.ARRAY);
		Year year = Year.of(2000);
		Assert.assertEquals(toJsonArray(2000), genson.serialize(year));
	}

	@Test
	public void testArrayDeserialization() {
		Genson genson = createTimestampGenson(Year.class, TimestampFormat.ARRAY);
		Year year = Year.of(2011);
		Assert.assertEquals(year, genson.deserialize(toJsonArray(2011), Year.class));
	}

	@Test
	public void testObjectSerialization() {
		Genson genson = createTimestampGenson(Year.class, TimestampFormat.OBJECT);
		Year year = Year.of(2011);
		String json = "{\"year\":2011}";
		Assert.assertEquals(json, genson.serialize(year));
	}

	@Test
	public void testObjectDeserialization() {
		Genson genson = createTimestampGenson(Year.class, TimestampFormat.OBJECT);
		Year year = Year.of(1999);
		String json = "{\"year\":1999}";
		Assert.assertEquals(year, genson.deserialize(json, Year.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsFullYear(){
		Genson genson = createFormatterGenson();
		Year dt = Year.now();
		String formattedValue = DateTimeFormatter.ofPattern("uuuu").format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsFullYear(){
		Genson genson = createFormatterGenson();
		Year dt = Year.now();
		String formattedValue = DateTimeFormatter.ofPattern("uuuu").format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), Year.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uu");
		Genson genson = createFormatterGenson(formatter, Year.class);
		Year dt = Year.of(1999);
		Assert.assertEquals(toJsonQuotedString("99"), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		//Two digit year gets parsed in range 2000 to 2099
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uu");
		Genson genson = createFormatterGenson(formatter, Year.class);
		Year dt = Year.of(2099);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString("99"), Year.class));
	}

}
