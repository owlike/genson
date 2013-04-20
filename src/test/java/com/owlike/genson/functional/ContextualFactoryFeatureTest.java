package com.owlike.genson.functional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.annotation.JsonDateFormat;

public class ContextualFactoryFeatureTest {
	private final Genson genson = new Genson();

	@Test
	public void testJsonDateFormat() throws TransformationException, IOException {
		ABean bean = new ABean();
		bean.milis = new Date();
		bean.date = new Date();

		assertEquals("{\"date\":\"" + new SimpleDateFormat("dd/mm/yyyy").format(bean.date)
				+ "\",\"milis\":" + bean.milis.getTime() + "}", genson.serialize(bean));
	}

	static class ABean {
		@JsonDateFormat(asTimeInMillis = true)
		public Date milis;
		@JsonDateFormat("dd/mm/yyyy")
		public Date date;
	}
}
