package com.owlike.genson.functional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.annotation.JsonConverter;
import com.owlike.genson.annotation.JsonDateFormat;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

public class ContextualFactoryFeatureTest {
	private final Genson genson = new Genson();

	@Test
	public void testJsonDateFormat() throws TransformationException, IOException {
		ABean bean = new ABean();
		bean.milis = new Date();
		bean.date = new Date();

		String json = genson.serialize(bean);

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
		assertEquals(
				"{\"date\":\"" + dateFormat.format(bean.date) + "\",\"milis\":"
						+ bean.milis.getTime() + "}", json);

		ABean bean2 = genson.deserialize(json, ABean.class);
		assertEquals(bean.milis, bean2.milis);
		assertEquals(dateFormat.format(bean.date), dateFormat.format(bean2.date));
	}

	@Test
	public void testPropertyConverter() throws TransformationException, IOException {
		assertEquals("{\"value\":1}", genson.serialize(new BBean()));
		assertEquals("1", genson.deserialize("{\"value\":1}", BBean.class).value);
	}

	@Test(expected = ClassCastException.class)
	public void testExceptionWhenPropertyTypeDoesNotMatch() throws TransformationException,
			IOException {
		genson.serialize(new ExceptionBean());
	}

	static class ABean {
		@JsonDateFormat(asTimeInMillis = true)
		public Date milis;
		@JsonDateFormat("dd/mm/yyyy")
		public Date date;
	}

	static class ExceptionBean {
		@JsonConverter(DummyConverter.class)
		Object value;
	}

	static class BBean {
		@JsonConverter(DummyConverter.class)
		String value = "foo";
	}

	public static class DummyConverter implements Converter<String> {
		@Override
		public void serialize(String object, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(1);
		}

		@Override
		public String deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return reader.valueAsString();
		}

	}
}
