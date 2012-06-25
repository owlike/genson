package org.genson.serialization;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.genson.Context;
import org.genson.Factory;
import org.genson.TransformationException;
import org.genson.stream.ObjectWriter;


public final class DefaultSerializers {
	private DefaultSerializers(){}
	
	public static class BooleanSerializerFactory implements Factory<Serializer<?>> {
		private final BooleanSerializer serializer = new BooleanSerializer();
		
		@Override
		public Serializer<Boolean> create(Type forType) {
			if ( forType instanceof Class<?> ) {
				Class<?> forClass = (Class<?>) forType;
				if ( Boolean.class.equals(forClass) || boolean.class == forClass )
					return serializer;
			}
						
			return null;
		}
		
	}
	
	public static class NumberSerializerFactory implements Factory<Serializer<?>> {
		private final NumberSerializer serializer = new NumberSerializer();
		private final IntegerSerializer intser = new IntegerSerializer();
		private final DoubleSerializer doubleser = new DoubleSerializer();
		
		@Override
		public Serializer<? extends Number> create(Type forType) {
			if ( forType instanceof Class<?> ) {
				Class<?> forClass = (Class<?>) forType;
				if ( Integer.class.equals(forClass) || int.class == forClass )
					return intser;
				if ( Long.class.equals(forClass) || long.class == forClass )
					return serializer;
				if ( Double.class.equals(forClass) || double.class == forClass )
					return doubleser;
			}
			return null;
		}
		
	}
	
	public static class IntegerSerializer implements Serializer<Integer> {
		@Override
		public void serialize(Integer obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue((int)obj);
		}
	}
	
	public static class DoubleSerializer implements Serializer<Double> {
		@Override
		public void serialize(Double obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue((double)obj);
		}
	}
	
	public static class ArraySerializer implements Serializer<Object> {

		@Override
		public void serialize(Object obj, Type type, ObjectWriter writer,
				Context ctx) throws TransformationException, IOException {
			Object[] array = (Object[]) obj;
			
			writer.beginArray();
			for ( Object o : array ) {
				if ( o != null ) ctx.genson.serialize(o, o.getClass(), writer, ctx);
				else writer.writeNull();
			}
			writer.endArray();
		}
	}
	
	public static class ArraySerializerFactory implements Factory<Serializer<Object>> {
		private final ArraySerializer arrSer = new ArraySerializer();
		@Override
		public ArraySerializer create(Type forType) {
			if ( forType instanceof GenericArrayType 
    				|| (forType instanceof Class<?> && ((Class<?>) forType).isArray()) )
				return arrSer;
				
			return null;
		}
		
	}
	
	public static class StringSerializer implements Serializer<String> {

		@Override
		public void serialize(String value, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(value);
		}
		
	}
	
	public static class BooleanSerializer implements Serializer<Boolean> {

		@Override
		public void serialize(Boolean value, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(value);
		}
		
	}
	
	public static class NumberSerializer implements Serializer<Number> {

		@Override
		public void serialize(Number num, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(num);
		}
		
	}
	
	public static class CollectionSerializer implements Serializer<Collection<Object>> {

		@Override
		public void serialize(Collection<Object> obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.beginArray();
			for ( Object o : obj ) {
				if ( o != null ) ctx.genson.serialize(o, o.getClass(), writer, ctx);
				else writer.writeNull();
			}
			writer.endArray();
		}
		
	}
	
	public static class MapSerializer implements Serializer<Map<Object, Object>> {

		@Override
		public void serialize(Map<Object, Object> obj, Type type, ObjectWriter writer,
				Context ctx) throws TransformationException, IOException {
			
			writer.beginObject();
			for ( Map.Entry<Object, Object> entry : obj.entrySet() ) {
				writer.writeName(entry.getKey().toString());
				if ( entry.getValue() != null )
					ctx.genson.serialize(entry.getValue(), entry.getValue().getClass(), writer, ctx);
				else writer.writeNull();
			}
			writer.endObject();
		}
	}
	
	public static class DateSerializer implements Serializer<Date> {
		private DateFormat dateFormat;
		
		public DateSerializer() {
			this(SimpleDateFormat.getInstance());
		}
		
		public DateSerializer(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
		}
		
		@Override
		public void serialize(Date obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeUnsafeValue(format(obj));
		}
		
		protected synchronized String format(Date date) {
			return dateFormat.format(date);
		}
	}
}
