package com.owlike.genson.functional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;
import static org.junit.Assert.*;

import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;

public class DefaultConvertersTest {
	private Genson genson = new Genson();

	@Test
	public void testUUIDConverter() throws TransformationException, IOException {
		UUID uuid = UUID.randomUUID();
		String json = genson.serialize(uuid);
		assertEquals(uuid, genson.deserialize(json, UUID.class));
	}

	@Test
	public void testDateConverter() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().setDateFormat(
				new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.FRENCH)).create();
		Date date = new Date();
		String json = genson.serialize(date);
		Date dateDeserialized = genson.deserialize(json, Date.class);
		assertEquals(date.toString(), dateDeserialized.toString());

	}

	@Test
	public void testCalendarConverter() throws TransformationException, IOException {
		Genson genson = new Genson.Builder().withTimeInMillis(true).create();
		Calendar cal = Calendar.getInstance();
		String json = genson.serialize(cal);
		Calendar cal2 = genson.deserialize(json, Calendar.class);
		assertEquals(cal.getTime(), cal2.getTime());

	}
}
