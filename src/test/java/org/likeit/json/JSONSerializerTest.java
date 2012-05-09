package org.likeit.json;

import java.awt.Shape;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.ObjectTransformer;
import org.likeit.transformation.annotation.JsonBeanView;
import org.likeit.transformation.serialization.BeanView;
import org.likeit.transformation.serialization.Serializer;
import org.likeit.transformation.stream.ObjectWriter;

import com.google.gson.Gson;


public class JSONSerializerTest {
	public static void main(String[] args) throws TransformationException, IOException {
		new JSONSerializerTest().serialize();
	}
	
	@SuppressWarnings("unchecked")
	public @JsonBeanView(views={MyBeanView.class, InfoView.class}) void serialize() throws TransformationException, IOException {
		ObjectTransformer json = new ObjectTransformer();
		ObjectMapper om = new ObjectMapper();
		
		List<MyBean> beans = new ArrayList<JSONSerializerTest.MyBean>();
		
		MyBean b = new MyBean();
		b.setAge(11);
		b.setNom("toto");
		Info info = new Info();
		info.setPrenom("andre");
		b.setInfo(info);
		beans.add(b);
		
		RootBean rb = new RootBean();
		rb.setBean(new MyBean[]{b, b});
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nom", "Toto");
		map.put("nom", "titi");
		map.put("bean", beans);
		map.put("root", rb);
		

//		long s = System.currentTimeMillis();
//		for ( int i = 0; i < 1000000; i++ ) {
//			String st = om.writeValueAsString(map);
//		}
//		System.out.println((System.currentTimeMillis()-s)/1000 + " s");
//
//		s = System.currentTimeMillis();
//		for ( int i = 0; i < 1000000; i++ ) {
//			String st = json.serialize(map);
//		}
//		System.out.println((System.currentTimeMillis()-s)/1000 + " s");
////		
//		Gson gson = new Gson();
//		s = System.currentTimeMillis();
//		for ( int i = 0; i < 1000000; i++ ) {
//			String st = gson.toJson(map);
//		}
//		System.out.println((System.currentTimeMillis()-s)/1000 + " s");
//		
//		
		System.out.println(json.serialize(map));
		
		ObjectTransformer ot = new ObjectTransformer.Builder().withSerializers(new MySerializer()).create();
		System.out.println(ot.serialize(map));
		
		
		System.out.println(ot.serialize(new Object[]{"ab", "tt", "io", 1, 2, 3, "4", "5"}));
		
//		System.out.println(om.writeValueAsString(map));
		
		System.out.println("aa="+json.serialize(rb));
//		System.out.println("bb="+om.writeValueAsString(rb));
		
		System.out.println(json.serialize(rb, MyBeanView.class));
		System.out.println(json.serialize(b));
//		System.out.println(om.writeValueAsString(b));

		System.out.println(json.serialize(info));
//		System.out.println(om.writeValueAsString(info));
		

		System.out.println(json.serialize(beans));
//		System.out.println(om.writeValueAsString(beans));
	}
	
	static class MySerializer implements Serializer<List<MyBean>> {

		@Override
		public void serialize(List<MyBean> obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			
			writer.beginArray();
			for ( MyBean o : obj )
				ctx.serialize(o.getAge(), Integer.class, writer);
			writer.endArray();
		}
		
	}
	
	static class Parent {
	
	}
	
	static class Child extends Parent {
		private Shape s;

		public Shape getS() {
			return s;
		}

		public void setS(Shape s) {
			this.s = s;
		}
	}
	
	static class Titi {
		Number num;
		Parent p;
		
		public Parent getP() {
			return p;
		}

		public void setP(Parent p) {
			this.p = p;
		}

		public Number getNum() {
			return num;
		}

		public void setNum(Number num) {
			this.num = num;
		}
	}
	
	public static class RootBean {
		private MyBean[] bean;
		private String nom;
		private Integer age;
		private boolean ok;
		
		public boolean isOk() {
			return ok;
		}

		public void setOk(boolean ok) {
			this.ok = ok;
		}
		private List<MyBean> beans;
		
		public RootBean() {
			nom = "arereztgfg";
			age = 123343;
			ok = true;
		}
		
		public MyBean[] getBean() {
			return bean;
		}
		public void setBean(MyBean[] bean) {
			this.bean = bean;
			this.beans = Arrays.asList(bean);
		}
		public String getRootName(){
			return "imroot";
		}
		public String getNom() {
			return nom;
		}
		public void setNom(String nom) {
			this.nom = nom;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
		public List<MyBean> getBeans() {
			return beans;
		}
		public void setBeans(List<MyBean> beans) {
			this.beans = beans;
		}
		
		
	}
	
	public static class InfoView implements BeanView<Info> {
		
	}
	
	public static class MyBean {
		private String nom;
		private Integer age;
		private Info info;
		
		public Info getInfo() {
			return info;
		}
		public void setInfo(Info info) {
			this.info = info;
		}
		public String getNom() {
			return nom;
		}
		public void setNom(String nom) {
			this.nom = nom;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
	}
	
	public static class Info {
		private String prenom;

		public String getPrenom() {
			return prenom;
		}

		public void setPrenom(String prenom) {
			this.prenom = prenom;
		}
		
	}
	
	public static class MyBeanView implements BeanView<MyBean> {
		public Info getInfo(MyBean b) {
			return b.getInfo();
		}
		
		public String getPrenom(MyBean b) {
			return b.getInfo() != null ? b.getInfo().getPrenom() : null;
		}
		
		public Integer getAnneNaissance(MyBean b) {
			Calendar c = GregorianCalendar.getInstance();
			return c.get(Calendar.YEAR) - b.getAge();
		}
	}
}
