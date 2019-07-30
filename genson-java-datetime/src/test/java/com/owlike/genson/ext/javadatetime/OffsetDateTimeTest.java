package com.owlike.genson.ext.javadatetime;

import com.owlike.genson.Genson;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeTest extends JavaDateTimeTestBase {
	@Test
	public void testMillisSerialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		OffsetDateTime dt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId);
		Assert.assertEquals(millis.toString(), genson.serialize(dt));
	}

	@Test
	public void testNanosSerialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		OffsetDateTime dt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoAdjustment), defaultZoneId);
		Assert.assertEquals(totalNanos.toString(), genson.serialize(dt));
	}

	@Test
	public void testArraySerialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.ARRAY, torontoZoneId);
		OffsetDateTime dt = OffsetDateTime.ofInstant(ZonedDateTime.of(2011, 11, 10, 9, 8,7, 1223, torontoZoneId).toInstant(), torontoZoneId);
		String expectedJson = toJsonArray(2011, 11, 10, 9, 8, 7, 1223, "-18000");
		Assert.assertEquals(expectedJson, genson.serialize(dt));
	}

	@Test
	public void testObjectSerialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.OBJECT, torontoZoneId);
		OffsetDateTime dt = OffsetDateTime.ofInstant(ZonedDateTime.of(2011, 11, 10, 9, 8,7, 1223, torontoZoneId).toInstant(), torontoZoneId);
		String expectedJson = "{\"year\":2011,\"month\":11,\"day\":10,\"hour\":9,\"minute\":8,\"second\":7,\"nano\":1223,\"offsetSeconds\":-18000}";
		Assert.assertEquals(expectedJson, genson.serialize(dt));
	}

	@Test
	public void testMillisDeserialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.MILLIS);
		Long millis = 4534654564653L;
		OffsetDateTime dt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), defaultZoneId);
		Assert.assertEquals(dt, genson.deserialize(millis.toString(), OffsetDateTime.class));
	}

	@Test
	public void testNanosDeserialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.NANOS);
		Long seconds = 321L;
		Long nanoAdjustment = 123456789L;
		Long totalNanos = DateTimeUtil.getNanos(seconds, nanoAdjustment);
		OffsetDateTime dt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds, nanoAdjustment), defaultZoneId);
		Assert.assertEquals(dt, genson.deserialize(totalNanos.toString(), OffsetDateTime.class));
	}

	@Test
	public void testArrayDeserialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.ARRAY, torontoZoneId);
		OffsetDateTime dt = OffsetDateTime.ofInstant(ZonedDateTime.of(2011, 11, 10, 9, 8,7, 1223, torontoZoneId).toInstant(), torontoZoneId);
		String json = "[2011, 11, 10, 9, 8, 7, 1223, -18000]";
		Assert.assertEquals(dt, genson.deserialize(json, OffsetDateTime.class));
	}

	@Test
	public void testObjectDeserialization(){
		Genson genson = createTimestampGenson(OffsetDateTime.class, TimestampFormat.OBJECT, torontoZoneId);
		OffsetDateTime dt = OffsetDateTime.ofInstant(ZonedDateTime.of(2011, 11, 10, 9, 8,7, 1223, torontoZoneId).toInstant(), torontoZoneId);
		String json = "{\"year\":2011,\"month\":11,\"day\":10,\"hour\":9,\"minute\":8,\"second\":7,\"nano\":1223,\"offsetSeconds\":-18000}";
		Assert.assertEquals(dt, genson.deserialize(json, OffsetDateTime.class));
	}

	@Test
	public void testDefaultFormattedSerializationIsISO(){
		Genson genson = createFormatterGenson();
		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testDefaultFormattedDeserializationIsISO(){
		Genson genson = createFormatterGenson();
		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), OffsetDateTime.class));
	}

	@Test
	public void testCustomFormatSerialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss");
		Genson genson = createFormatterGenson(formatter, OffsetDateTime.class);
		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(toJsonQuotedString(formattedValue), genson.serialize(dt));
	}

	@Test
	public void testCustomFormatDeserialization(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, OffsetDateTime.class);
		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = formatter.format(dt);
		Assert.assertEquals(dt, genson.deserialize(toJsonQuotedString(formattedValue), OffsetDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults1(){
		//Verify that time of day gets defaulted to 00:00:00.000000000 if not parsed by the formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/uuuu");
		Genson genson = createFormatterGenson(formatter, OffsetDateTime.class);
		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = formatter.format(dt);
		OffsetDateTime dtWithDefaults = dt.withHour(0).withMinute(0).withSecond(0).withNano(0);
		Assert.assertEquals(dtWithDefaults, genson.deserialize(toJsonQuotedString(formattedValue), OffsetDateTime.class));
	}

	@Test
	public void testCustomFormatDeserializationWithDefaults2(){
		//Verify that date gets defaulted to 2000-01-01 if not parsed by formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.nnnnnnnnn");
		Genson genson = createFormatterGenson(formatter, OffsetDateTime.class);

		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = formatter.format(dt);

		OffsetDateTime dtWithDefaults = dt.withYear(2000).withMonth(1).withDayOfMonth(1).withOffsetSameLocal(ZoneOffset.UTC);
		OffsetDateTime actualDate = genson.deserialize(toJsonQuotedString(formattedValue), OffsetDateTime.class)
				.withOffsetSameLocal(ZoneOffset.UTC);

		Assert.assertEquals(dtWithDefaults, actualDate);
	}

	@Test
	public void testCustomFormatDeserializationWithCustomZoneId(){
		//Verify that the zoneId defaults to the one configured in the bundle if it cannot be parsed by the formatter
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
		Genson londonGenson = createFormatterGenson(formatter, OffsetDateTime.class, londonZoneId);
		Genson torontoGenson = createFormatterGenson(formatter, OffsetDateTime.class, torontoZoneId);
		OffsetDateTime dt = OffsetDateTime.now();
		String formattedValue = formatter.format(dt);
		OffsetDateTime londonDt = londonGenson.deserialize(toJsonQuotedString(formattedValue), OffsetDateTime.class);
		OffsetDateTime torontoDt = torontoGenson.deserialize(toJsonQuotedString(formattedValue), OffsetDateTime.class);
		Assert.assertEquals(londonZoneId.getRules().getOffset(dt.toInstant()), londonDt.getOffset());
		Assert.assertEquals(torontoZoneId.getRules().getOffset(dt.toInstant()), torontoDt.getOffset());
	}
}
