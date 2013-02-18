package com.owlike.genson.ext.jaxb;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.annotation.JsonProperty;

public class XmlAccessorTypeTest {
	private ObjectMapper om = new ObjectMapper();
	private Genson genson = new Genson.Builder().withJaxbSupport(true).create();

	@Before
	public void setUp() {
		om.registerModule(new JaxbAnnotationModule());
	}

	@Test
	public void testFieldAccess() throws TransformationException, IOException {
		System.out.println(om.writeValueAsString(new XmlAccessorTypeField()));
		String json = genson.serialize(new XmlAccessorTypeField());
		assertEquals("{\"a\":1}", json);
	}

	@Test
	public void testPackageAccess() {

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class XmlAccessorTypeField {
		private int a = 1;
		private transient int transientField;

		public void setB(int b) {
			transientField = b;
		}

		public int getB() {
			return transientField;
		}

		@JsonProperty
		@com.fasterxml.jackson.annotation.JsonProperty
		public int getC() {
			return 2;
		}
	}
}
