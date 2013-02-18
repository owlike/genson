package com.owlike.genson.ext.jaxb;

import java.io.IOException;

import javax.xml.bind.annotation.XmlTransient;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;

public class XMLTransientTest {
	private ObjectMapper om = new ObjectMapper();
	private Genson genson = new Genson.Builder().withJaxbSupport(true).create();

	@Before
	public void setUp() {
		om.registerModule(new JaxbAnnotationModule());
	}

	@Test
	public void testXmlTransientSerialization() throws TransformationException, IOException {
		XmlTransientContainer container = new XmlTransientContainer();
		container.bean = new XmlTransientBean();
		assertEquals("{}", genson.serialize(container));
	}

	@Test
	public void testXmlTransientDeserialization() throws TransformationException, IOException {
		XmlTransientContainer container = genson.deserialize("{\"a\":1,\"b\":2,\"bean\":{}}",
				XmlTransientContainer.class);
		assertEquals(0, container.a);
		assertEquals(0, container.b);
		assertEquals(null, container.bean);
	}

	public static class XmlTransientContainer {
		@XmlTransient
		public int a;
		public XmlTransientBean bean;
		private int b;

		public int getB() {
			return b;
		}

		@XmlTransient
		public void setB(int b) {
			this.b = b;
		}
	}

	@XmlTransient
	public static class XmlTransientBean {

	}
}
