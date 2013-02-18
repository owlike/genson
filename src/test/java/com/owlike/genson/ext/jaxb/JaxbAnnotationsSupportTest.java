package com.owlike.genson.ext.jaxb;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;

public class JaxbAnnotationsSupportTest {
	private ObjectMapper om;
	private Genson genson;

	@Before
	public void setUp() {
		om = new ObjectMapper().registerModule(new JaxbAnnotationModule()).configure(
				SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		genson = new Genson.Builder().with(new JaxbConfigurer()).create();
	}

	// @Test
	public void testXmlAccessorType() throws JsonProcessingException {

		System.out.println(om.writeValueAsString(new XmlAccessorTypeBean()));
	}

	@Test
	public void testXmlAttributeSerialization() throws IOException, TransformationException {
		assertEquals("{\"a\":0,\"c\":1,\"d\":null}", genson.serialize(new XmlAttributeBean()));
	}

	@Test
	public void testXmlAttributeDeserialization() throws IOException, TransformationException {
		XmlAttributeBean bean = genson.deserialize("{\"a\":2,\"d\":3,\"c\":4}",
				XmlAttributeBean.class);
		assertEquals(2, bean.a);
		assertEquals(3, bean.b.intValue());
		assertEquals(4, bean.c);
	}

	public static class XmlAttributeBean {
		@XmlAttribute
		private int a;
		@XmlAttribute(name = "d", required = true)
		private Integer b;
		public transient int c = 1;

		@XmlAttribute
		private int getC() {
			return c;
		}

		@XmlAttribute
		private void setC(int c) {
			this.c = c;
		}
	}

	@Test
	public void testXmlElementSerialization() throws IOException, TransformationException {
		assertEquals("{\"b\":0,\"bean\":{\"a\":0},\"c\":0}", genson.serialize(new XmlElementBean()));
	}

	@Test
	public void testXmlElementDeserialization() throws TransformationException, IOException {
		XmlElementBean bean = genson.deserialize("{\"b\":1,\"bean\":{\"a\":2},\"c\":3}",
				XmlElementBean.class);
		assertEquals(1, bean.b);
		assertEquals(2, ((EmptyBean) bean.a).a);
		assertEquals(3, bean.c);
	}

	public static class XmlElementBean {
		@XmlElement(name = "bean", type = EmptyBean.class)
		private Object a = new EmptyBean();
		@XmlElement(name = "c")
		private int c;
		private transient int b;

		@XmlElement
		private int getB() {
			return b;
		}

		@XmlElement
		private void setB(int b) {
			this.b = b;
		}
	}

	public static class EmptyBean {
		public int a;
	}

	// @Test
	public void testXmlElementRef() throws JsonProcessingException {
		System.out.println(om.writeValueAsString(new XmlElementRefBean()));
	}

	// @Test
	public void testXmlEnumValue() {
		System.out.println("to be done!!!!!!!");
	}

	// @Test
	public void testXmlJavaTypeAdapter() throws JsonProcessingException {
		System.out.println(om.writeValueAsString(new XmlJavaTypeAdapterBean()));
	}

	public static class XmlJavaTypeAdapterBean {
		@XmlJavaTypeAdapter(MyXmlAdapter.class)
		public int v;
	}

	public static class MyXmlAdapter extends XmlAdapter<XmlAccessorTypeBean, Integer> {

		@Override
		public Integer unmarshal(XmlAccessorTypeBean v) throws Exception {
			System.out.println("===");
			return null;
		}

		@Override
		public XmlAccessorTypeBean marshal(Integer v) throws Exception {
			// TODO Auto-generated method stub

			System.out.println("yyyyyyy");
			return new XmlAccessorTypeBean();
		}

	}

	public static class XmlEnumValueBean {
		// @XmlEnumValue()
	}

	public static class XmlElementRefBean {
		@XmlElementRef
		private XmlRootElementBean a;
		@XmlElementRef
		private XmlRootElementDefaultBean b;
	}

	@XmlRootElement
	public static class XmlRootElementDefaultBean {

	}

	@XmlRootElement(name = "oo")
	public static class XmlRootElementBean {

	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class XmlAccessorTypeBean {
		private int a = 1;
		private transient int transientField = 2;

		public int getB() {
			return 3;
		}
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
