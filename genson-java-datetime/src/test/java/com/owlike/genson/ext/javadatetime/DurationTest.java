package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class DurationTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerializationArray() {
		Genson genson = createTemporalAmountTimestampGenson(Duration.class, TimestampFormat.ARRAY);
		long seconds = 321L;
		long nanos = 123456789;
		Duration duration = Duration.ofSeconds(seconds, nanos);
		Assert.assertEquals(toJsonArray(seconds, nanos), genson.serialize(duration));
	}

	@Test
	public void testTimestampDeserializationArray() {
		Genson genson = createTemporalAmountTimestampGenson(Duration.class, TimestampFormat.ARRAY);
		long seconds = 321L;
		long nanos = 123456789;
		Duration duration = Duration.ofSeconds(seconds, nanos);
		Assert.assertEquals(duration, genson.deserialize(toJsonArray(seconds, nanos), Duration.class));
	}

	@Test
	public void testTimestampSerializationObject() {
		Genson genson = createTemporalAmountTimestampGenson(Duration.class, TimestampFormat.OBJECT);
		long seconds = 321L;
		long nanos = 123456789;
		Duration duration = Duration.ofSeconds(seconds, nanos);
		String json = "{\"seconds\":321,\"nanos\":123456789}";
		Assert.assertEquals(json, genson.serialize(duration));
	}

	@Test
	public void testTimestampDeserializationObject() {
		Genson genson = createTemporalAmountTimestampGenson(Duration.class, TimestampFormat.OBJECT);
		long seconds = 321L;
		long nanos = 123456789;
		Duration duration = Duration.ofSeconds(seconds, nanos);
		String json = "{\"seconds\":321,\"nanos\":123456789}";
		Assert.assertEquals(duration, genson.deserialize(json, Duration.class));
	}


	@Test
	public void testStringSerialization() {
		Genson genson = createFormatterGenson();
		Duration duration = Duration.ofSeconds(123, 456);
		Assert.assertEquals(toJsonQuotedString(duration.toString()), genson.serialize(duration));
	}

	@Test
	public void testStringDeserialization() {
		Genson genson = createFormatterGenson();
		Duration duration = Duration.ofSeconds(123, 456);
		Assert.assertEquals(duration, genson.deserialize(duration.toString(), Duration.class));
	}
}
