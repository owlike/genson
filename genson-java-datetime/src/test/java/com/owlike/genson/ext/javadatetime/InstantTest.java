package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantTest extends JavaDateTimeTestBase {
	@Test
	public void testMillisSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		Instant dt = Instant.ofEpochMilli(millis);
		Assert.assertEquals(millis.toString(), genson.serialize(dt));
	}

	@Test
	public void testNanosSerialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		Assert.assertEquals(totalNanos.toString(), genson.serialize(dt));
	}

	@Test
	public void testMillisDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		Instant dt = Instant.ofEpochMilli(millis);
		Assert.assertEquals(dt, genson.deserialize(millis.toString(), Instant.class));
	}

	@Test
	public void testNanosDeserialization(){
		Genson genson = createTimestampGenson(TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		Assert.assertEquals(dt, genson.deserialize(totalNanos.toString(), Instant.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		Instant dt = Instant.now();
		String formattedValue = DateTimeFormatter.ISO_INSTANT.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		Instant dt = Instant.now();
		String formattedValue = DateTimeFormatter.ISO_INSTANT.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), Instant.class));
	}
}
