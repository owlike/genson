package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Period;

public class PeriodTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerialization1() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.MILLIS);
		Period period = Period.ofYears(14);
		Assert.assertEquals(toJsonArray(14, 0, 0), genson.serialize(period));
	}

	@Test
	public void testTimestampSerialization2() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.MILLIS);
		Period period = Period.of(1, 2, 3);
		Assert.assertEquals(toJsonArray(1, 2, 3), genson.serialize(period));
	}

	@Test
	public void testTimestampDeserialization1() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.MILLIS);
		Period period = Period.ofYears(14);
		Assert.assertEquals(period, genson.deserialize(toJsonArray(14, 0, 0), Period.class));
	}

	@Test
	public void testTimestampDeserialization2() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.MILLIS);
		Period period = Period.of(1, 2, 3);
		Assert.assertEquals(period, genson.deserialize(toJsonArray(1, 2, 3), Period.class));
	}

	@Test
	public void testStringSerialization() {
		Genson genson = createFormatterGenson();
		Period period = Period.of(1, 2, 3);
		Assert.assertEquals(toJsonQuotedString(period.toString()), genson.serialize(period));
	}

	@Test
	public void testStringDeserialization() {
		Genson genson = createFormatterGenson();
		Period period = Period.of(1, 2, 3);
		Assert.assertEquals(period, genson.deserialize(period.toString(), Period.class));
	}
}
