package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class DurationTest extends JavaDateTimeTestBase {
	@Test
	public void testTimestampSerialization() {
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		long seconds = 321L;
		long nanos = 123456789;
		Duration duration = Duration.ofSeconds(seconds, nanos);
		Assert.assertEquals(toJsonArray(seconds, nanos), genson.serialize(duration));
	}

	@Test
	public void testTimestampDeserialization() {
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		long seconds = 321L;
		long nanos = 123456789;
		Duration duration = Duration.ofSeconds(seconds, nanos);
		Assert.assertEquals(duration, genson.deserialize(toJsonArray(seconds, nanos), Duration.class));
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
