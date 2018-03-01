package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Period;

public class PeriodTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerializationArray() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.ARRAY);
		Period period = Period.ofYears(14);
		Assert.assertEquals(toJsonArray(14, 0, 0), genson.serialize(period));
	}

	@Test
	public void testTimestampSerializationObject() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.OBJECT);
		Period period = Period.of(1, 2, 3);
		String json = "{\"years\":1,\"months\":2,\"days\":3}";
		Assert.assertEquals(json, genson.serialize(period));
	}

	@Test
	public void testTimestampDeserializationArray() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.ARRAY);
		Period period = Period.ofYears(14);
		Assert.assertEquals(period, genson.deserialize(toJsonArray(14, 0, 0), Period.class));
	}

	@Test
	public void testTimestampDeserializationObject() {
		Genson genson = createTemporalAmountTimestampGenson(Period.class, TimestampFormat.OBJECT);
		Period period = Period.of(1, 2, 3);
		String json = "{\"years\":1,\"months\":2,\"days\":3}";
		Assert.assertEquals(period, genson.deserialize(json, Period.class));
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
