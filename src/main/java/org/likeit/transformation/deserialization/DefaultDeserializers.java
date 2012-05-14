package org.likeit.transformation.deserialization;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.internal.IntrospectionUtils;
import org.likeit.transformation.stream.ObjectReader;

public class DefaultDeserializers {
	public final static List<Deserializer<?>> createDefaultDeserializers() {
		List<Deserializer<?>> deserializers = new ArrayList<Deserializer<?>>();
		deserializers.add(new StringDeserializer());
		deserializers.add(new IntegerDeserializer());
		deserializers.add(new CollectionDeserializer());
		deserializers.add(new DoubleDeserializer());
		return deserializers;
	}
	
	public final static List<DeserializerFactory<? extends Deserializer<?>>> createDefaultDeserializerFactories() {
		List<DeserializerFactory<? extends Deserializer<?>>> deserializerFactories = new ArrayList<DeserializerFactory<? extends Deserializer<?>>>();
		deserializerFactories.add(new BooleanDeserializerFactory());
		deserializerFactories.add(new ArrayDeserializerFactory());
		return deserializerFactories;
	}
	
	public final static ChainedDeserializer createDefaultDynamicDeserializer() {
		ChainedDeserializer dynaSerializer = new BeanDeserializer();
		return dynaSerializer;
	}
	
	public static class StringDeserializer implements Deserializer<String> {
		@Override
		public String deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return reader.value();
		}
	}
	
	public static class BooleanDeserializerFactory implements DeserializerFactory<Deserializer<Boolean>> {
		private final BooleanDeserializer boolDeser = new BooleanDeserializer();
		
		@Override
		public Deserializer<Boolean> create(Type type) {
			if ( type instanceof Class<?> ) {
					Class<?> forClass = (Class<?>) type;
					if ( Boolean.class.equals(forClass) || boolean.class == forClass )
				return boolDeser;
			}
			return null;
		}
	}
	
	public static class BooleanDeserializer implements Deserializer<Boolean> {
		@Override
		public Boolean deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return Boolean.parseBoolean(reader.value());
		}
	}
	
	public static class IntegerDeserializer implements Deserializer<Integer> {
		@Override
		public Integer deserialize(Type type, ObjectReader reader, Context ctx) throws NumberFormatException, IOException {
			return Integer.parseInt(reader.value());
		}
	}
	public static class DoubleDeserializer implements Deserializer<Double> {
		@Override
		public Double deserialize(Type type, ObjectReader reader, Context ctx) throws NumberFormatException, IOException {
			return Double.parseDouble(reader.value());
		}
	}
	public static class CollectionDeserializer implements Deserializer<Collection<? super Object>> {
		@Override
		public Collection<? super Object> deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException {
			reader.beginArray();
			
			Collection<? super Object> col = new ArrayList<Object>();
			Type cType = IntrospectionUtils.getCollectionType(type);
			for ( ; reader.hasNext(); ) {
				reader.next();
				Object o = ctx.deserialize(cType, reader);
				col.add(o);
			}
			
			reader.endArray();
			return col;
		}
	}
	
	public static class ArrayDeserializerFactory implements DeserializerFactory<Deserializer<Object>> {
		private final ArrayDeserializer arrDeser = new ArrayDeserializer();
		
		@Override
		public Deserializer<Object> create(Type type) {
			if ( type instanceof GenericArrayType 
    				|| (type instanceof Class<?> && ((Class<?>) type).isArray()) )
				return arrDeser;
			return null;
		}
		
	}
	
	public static class ArrayDeserializer implements Deserializer<Object> {
		@Override
		public Object deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException {
			reader.beginArray();
			
			List<Object> list = new ArrayList<Object>();
			Type cType = IntrospectionUtils.getCollectionType(type);
			for ( ; reader.hasNext(); ) {
				reader.next();
				Object o = ctx.deserialize(cType, reader);
				list.add(o);
			}
			
			reader.endArray();
			
			Object array = Array.newInstance(IntrospectionUtils.getRawType(cType), list.size());
			for (int i = 0; i < list.size(); i++) {
		      Array.set(array, i, list.get(i));
		    }
			 
			return array;
		}
	}
}
