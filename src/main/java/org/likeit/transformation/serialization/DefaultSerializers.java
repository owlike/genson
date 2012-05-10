package org.likeit.transformation.serialization;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.stream.ObjectWriter;

public class DefaultSerializers {
	
	public final static List<Serializer<?>> createDefaultSerializers() {
		List<Serializer<?>> serializers = new ArrayList<Serializer<?>>();
		serializers.add(new DefaultSerializers.StringSerializer());
		serializers.add(new DefaultSerializers.CollectionSerializer());
		serializers.add(new DefaultSerializers.MapSerializer());
		
		return serializers;
	}
	
	public final static List<SerializerFactory<? extends Serializer<?>>> createDefaultSerializerFactories() {
		List<SerializerFactory<? extends Serializer<?>>> serializerFactories = new ArrayList<SerializerFactory<? extends Serializer<?>>>();
		serializerFactories.add(new DefaultSerializers.ArraySerializerFactory());
		serializerFactories.add(new DefaultSerializers.NumberSerializerFactory());
		serializerFactories.add(new DefaultSerializers.BooleanSerializerFactory());
		return serializerFactories;
	}
	
	public final static ChainedSerializer createDefaultDynamicSerializer() {
		ChainedSerializer dynaSerializer = new BeanViewSerializer();
		dynaSerializer.withNext(new BeanSerializer());
		return dynaSerializer;
	}
	
	public static class BooleanSerializerFactory implements SerializerFactory<Serializer<Boolean>> {
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
	
	public static class NumberSerializerFactory implements SerializerFactory<Serializer<Number>> {
		private final NumberSerializer serializer = new NumberSerializer();
		
		@Override
		public Serializer<Number> create(Type forType) {
			if ( forType instanceof Class<?> ) {
				Class<?> forClass = (Class<?>) forType;
				if ( Integer.class.equals(forClass) || int.class == forClass
					|| Long.class.equals(forClass) || long.class == forClass
					|| Double.class.equals(forClass) || double.class == forClass) {
					return serializer;
				}
			}
			return null;
		}
		
	}
	
	public static class ArraySerializer implements Serializer<Object> {

		@Override
		public void serialize(Object obj, Type type, ObjectWriter writer,
				Context ctx) throws TransformationException, IOException {
			Object[] array = (Object[]) obj;
			
			writer.beginArray();
			for ( Object o : array ) {
				ctx.serialize(o, o.getClass(), writer);
			}
			writer.endArray();
		}
	}
	
	public static class ArraySerializerFactory implements SerializerFactory<ArraySerializer> {

		@Override
		public ArraySerializer create(Type forType) {
			if ( forType instanceof GenericArrayType 
    				|| (forType instanceof Class<?> && ((Class<?>) forType).isArray()) )
				return new ArraySerializer();
				
			return null;
		}
		
	}
	
	public static class StringSerializer implements Serializer<String> {

		@Override
		public void serialize(String value, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.value(value);
		}
		
	}
	
	public static class BooleanSerializer implements Serializer<Boolean> {

		@Override
		public void serialize(Boolean value, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.value(value);
		}
		
	}
	
	public static class NumberSerializer implements Serializer<Number> {

		@Override
		public void serialize(Number num, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.value(num);
		}
		
	}
	
	public static class DateSerializer implements Serializer<Date> {

		@Override
		public void serialize(Date obj, Type type, ObjectWriter writer,
				Context ctx) throws TransformationException, IOException {
			
		}
		
	}
	
	public static class CollectionSerializer implements Serializer<Collection<Object>> {

		@Override
		public void serialize(Collection<Object> obj, Type type, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.beginArray();
			for ( Object o : obj ) {
				ctx.serialize(o, o.getClass(), writer);
			}
			writer.endArray();
		}
		
	}
	
	public static class MapSerializer implements Serializer<Map<String, Object>> {

		@Override
		public void serialize(Map<String, Object> obj, Type type, ObjectWriter writer,
				Context ctx) throws TransformationException, IOException {
			
			writer.beginObject();
			for ( Map.Entry<String, Object> entry : obj.entrySet() ) {
				writer.name(entry.getKey());
				ctx.serialize(entry.getValue(), entry.getValue().getClass(), writer);
			}
			writer.endObject();
		}
	}
}
