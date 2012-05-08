package org.likeit.transformation.deserialization;

import java.io.IOException;
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
	
	public static class StringDeserializer implements Deserializer<String> {
		@Override
		public String deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return reader.value();
		}
	}
	
	public static class IntegerDeserializer implements Deserializer<Integer> {
		@Override
		public Integer deserialize(Type type, ObjectReader reader, Context ctx) {
			return Integer.parseInt(reader.value());
		}
	}
	public static class DoubleDeserializer implements Deserializer<Double> {
		@Override
		public Double deserialize(Type type, ObjectReader reader, Context ctx) {
			return Double.parseDouble(reader.value());
		}
	}
	public static class CollectionDeserializer implements Deserializer<Collection<?>> {
		@Override
		public Collection<? super Object> deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException {
			reader.beginArray();
			
			Collection<? super Object> col = new ArrayList<Object>();
			Type cType = IntrospectionUtils.getCollectionType(type);
			for ( ; reader.hasNext(); reader.next() ) {
				Object o = ctx.deserialize(cType, reader);
				col.add(o);
			}
			
			reader.endArray();
			return col;
		}
	}
	public static class ArrayDeserializer implements Deserializer<Object[]> {
		@Override
		public Object[] deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException {
			reader.beginArray();
			
			List<Object> arr = new ArrayList<Object>();
			Type cType = IntrospectionUtils.getCollectionType(type);
			for ( ; reader.hasNext(); reader.next() ) {
				Object o = ctx.deserialize(cType, reader);
				arr.add(o);
			}
			
			reader.endArray();
			return arr.toArray(new Object[arr.size()]);
		}
	}
}
