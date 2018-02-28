package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantTest extends JavaDateTimeTestBase {
	@Test
	public void testMillisSerialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		Instant dt = Instant.ofEpochMilli(millis);
		Assert.assertEquals(millis.toString(), genson.serialize(dt));
	}

	@Test
	public void testNanosSerialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		Assert.assertEquals(totalNanos.toString(), genson.serialize(dt));
	}

	@Test
	public void testMillisDeserialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		Instant dt = Instant.ofEpochMilli(millis);
		Assert.assertEquals(dt, genson.deserialize(millis.toString(), Instant.class));
	}

	@Test
	public void testNanosDeserialization(){
		Long seconds = 321L;
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.NANOS);
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		Assert.assertEquals(dt, genson.deserialize(totalNanos.toString(), Instant.class));
	}

	@Test
	public void testArraySerialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.ARRAY);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		Assert.assertEquals(toJsonArray(seconds, nanoAdjustment), genson.serialize(dt));
	}

	@Test
	public void testObjectSerialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.OBJECT);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		String expectedJson = "{\"second\":321,\"nano\":123456789}";
		Assert.assertEquals(expectedJson, genson.serialize(dt));
	}

	@Test
	public void testArrayDeserialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.ARRAY, londonZoneId);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		Assert.assertEquals(dt, genson.deserialize(toJsonArray(seconds, nanoAdjustment), Instant.class));
	}

	@Test
	public void testObjectDeserialization(){
		Genson genson = createTimestampGenson(Instant.class, TimestampFormat.OBJECT);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Instant dt = Instant.ofEpochSecond(seconds, nanoAdjustment);
		String json = "{\"second\":321, \"nano\":123456789}";
		Assert.assertEquals(dt, genson.deserialize(json, Instant.class));
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
