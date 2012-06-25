package org.genson.deserialization;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genson.Context;
import org.genson.Factory;
import org.genson.TransformationException;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ValueType;


public class DefaultDeserializers {	
	public static class StringDeserializer implements Deserializer<String> {
		@Override
		public String deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return reader.valueAsString();
		}
	}
	
	public static class BooleanDeserializerFactory implements Factory<Deserializer<?>> {
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
			return reader.valueAsBoolean();
		}
	}
	
	public static class UntypedDeserializerFactory implements Factory<Deserializer<Object>> {
		private final UntypedDeserializer deser = new UntypedDeserializer();
		@Override
		public Deserializer<Object> create(Type type) {
			if ( TypeUtil.match(type, Object.class, true) ) {
				return deser;
			}
			return null;
		}
	}
	
	public static class UntypedDeserializer implements Deserializer<Object> {
		@Override
		public Object deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return ctx.genson.deserialize(reader.getValueType().toClass(), reader, ctx);
		}	
	}
	
	public static class NumberDeserializerFactory implements Factory<Deserializer<?>> {
		private final IntegerDeserializer intDeser = new IntegerDeserializer();
		private final LongDeserializer longDeser = new LongDeserializer();
		private final DoubleDeserializer doubleDeser = new DoubleDeserializer();
		private final NumberDeserializer numberDeser = new NumberDeserializer();
		
		@Override
		public Deserializer<?> create(Type type) {
			if ( type instanceof Class<?> ) {
				Class<?> forClass = (Class<?>) type;
				if ( Integer.class.equals(forClass) ) return intDeser;
				if (int.class == forClass) return new Deserializer<Integer>() {

					@Override
					public Integer deserialize(Type type, ObjectReader reader, Context ctx)
							throws TransformationException, IOException {
						return reader.valueAsInt();
					}
				};
				if ( Long.class.equals(forClass) || long.class == forClass ) return longDeser;
				if ( Double.class.equals(forClass) ) return doubleDeser;
				if (double.class == forClass) return new Deserializer<Double>() {

					@Override
					public Double deserialize(Type type, ObjectReader reader, Context ctx)
							throws TransformationException, IOException {
						return reader.valueAsDouble();
					}
				};
				else if ( Number.class.equals(forClass) ) return numberDeser;
			}
			return null;
		}
	}
	
	public static class NumberDeserializer implements Deserializer<Number> {
		@Override
		public Number deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return ctx.genson.deserialize(reader.getValueType().toClass(), reader, ctx);
		}
		
	}
	
	public static class IntegerDeserializer implements Deserializer<Integer> {
		@Override
		public Integer deserialize(Type type, ObjectReader reader, Context ctx) throws NumberFormatException, IOException {
			if (ValueType.STRING.equals(reader.getValueType())) {
				String value = reader.valueAsString();
				return "".equals(value) ? null : Integer.valueOf(value);
			}
			return reader.valueAsInt();
		}
	}
	
	public static class LongDeserializer implements Deserializer<Long> {
		@Override
		public Long deserialize(Type type, ObjectReader reader, Context ctx) throws NumberFormatException, IOException {
			return Long.valueOf(reader.valueAsString());
		}
	}
	
	public static class DoubleDeserializer implements Deserializer<Double> {
		@Override
		public Double deserialize(Type type, ObjectReader reader, Context ctx) throws NumberFormatException, IOException {
			if (ValueType.STRING.equals(reader.getValueType())) {
				String value = reader.valueAsString();
				return "".equals(value) ? null : Double.valueOf(value);
			}
			return reader.valueAsDouble();
		}
	}
	
	public static class CollectionDeserializer implements Deserializer<Collection<? extends Object>> {
		private Map<Type, Type> _componentTypeCache = new HashMap<Type, Type>();
		
		@Override
		public Collection<? super Object> deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException {
			reader.beginArray();
			
			Collection<Object> col = new ArrayList<Object>();
			Type cType = _componentTypeCache.get(type);
			if ( cType == null ) {
				cType = TypeUtil.getCollectionType(type);
				_componentTypeCache.put(type, cType);
			}
			for ( ; reader.hasNext(); ) {
				reader.next();
				Object o = ctx.genson.deserialize(cType, reader, ctx);
				col.add(o);
			}
			
			reader.endArray();
			return col;
		}
	}
	
	public static class MapDeserializer implements Deserializer<Map<?, ?>> {
		private Map<Type, Type> _valueTypeCache = new HashMap<Type, Type>();
		
		@Override
		public Map<?, ?> deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException {
			reader.beginObject();
			
			Type valueType = _valueTypeCache.get(type);
			if ( valueType == null ) {
				valueType = TypeUtil.expand(TypeUtil.typeOf(1, type), null);
				_valueTypeCache.put(type, valueType);
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			for ( ; reader.hasNext(); ) {
				reader.next();
				String name = reader.name();
				Object o = ctx.genson.deserialize(valueType, reader, ctx);
				map.put(name, o);
			}
			
			reader.endObject();
			return map;
		}
	}
	
	public static class ArrayDeserializerFactory implements Factory<Deserializer<Object>> {
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
		private Map<Type, MapPair> _componentTypeCache = new HashMap<Type, MapPair>();
		
		@Override
		public Object deserialize(Type type, ObjectReader reader, Context ctx) throws TransformationException, IOException {
			reader.beginArray();
			
			MapPair cType = _componentTypeCache.get(type);
			if ( cType == null ) {
				Type colType = TypeUtil.getCollectionType(type);
				Class<?> rawType = TypeUtil.getRawClass(colType);
				cType = new MapPair(colType, rawType);
				_componentTypeCache.put(type, cType);
			}
			List<Object> list = new ArrayList<Object>();
			
			for ( ; reader.hasNext(); ) {
				reader.next();
				Object o = ctx.genson.deserialize(cType.element, reader, ctx);
				list.add(o);
			}
			
			reader.endArray();
			
			Object array = Array.newInstance(cType.component, list.size());
			for (int i = 0; i < list.size(); i++) {
		      Array.set(array, i, list.get(i));
		    }
			 
			return array;
		}
		
		private final class MapPair {
			final Type element;
			final Class<?> component;
			public MapPair(Type type, Class<?> rawType) {
				this.element = type;
				this.component = rawType;
			}
		}
	}
	
	public static class DateDeserializer implements Deserializer<Date> {
		private DateFormat dateFormat;
		
		public DateDeserializer() {
			this(SimpleDateFormat.getDateInstance());
		}
		
		public DateDeserializer(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
		}
		
		@Override
		public Date deserialize(Type type, ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			try {
				return read(reader.valueAsString());
			} catch (ParseException e) {
				throw new TransformationException("Could not parse date " + reader.valueAsString(), e);
			}
		}
		
		protected synchronized Date read(String dateString) throws ParseException {
			return dateFormat.parse(dateString);
		}
	}
}
