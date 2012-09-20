package com.owlike.genson.convert;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.TransformationRuntimeException;
import com.owlike.genson.annotation.HandleClassMetadata;
import com.owlike.genson.annotation.HandleNull;
import com.owlike.genson.annotation.WithoutBeanView;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import com.owlike.genson.stream.ValueType;

/**
 * This class contains all default converters and their factories. You can read the source code <a
 * href=
 * "http://code.google.com/p/genson/source/browse/src/main/java/com/owlike/genson/convert/DefaultConverters.java"
 * >here</a> as example on how to implement custom converters and factories.
 * 
 * @author eugen
 * 
 */
public final class DefaultConverters {
	@HandleClassMetadata
	public static class CollectionConverter<E> implements Converter<Collection<E>> {

		@SuppressWarnings("unused")
		private final Class<E> eClass;
		private final Converter<E> elementConverter;

		public CollectionConverter(Class<E> eClass, Converter<E> elementConverter) {
			this.eClass = eClass;
			this.elementConverter = elementConverter;
		}

		public Collection<E> deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			reader.beginArray();
			Collection<E> col = new ArrayList<E>();
			for (; reader.hasNext();) {
				reader.next();
				E e = elementConverter.deserialize(reader, ctx);
				col.add(e);
			}
			reader.endArray();
			return col;
		}

		public void serialize(Collection<E> array, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.beginArray();
			for (E e : array) {
				elementConverter.serialize(e, writer, ctx);
			}
			writer.endArray();
		}

		public Converter<E> getElementConverter() {
			return elementConverter;
		}
	}

	public final static class CollectionConverterFactory implements
			Factory<Converter<Collection<?>>> {
		public final static CollectionConverterFactory instance = new CollectionConverterFactory();

		private CollectionConverterFactory() {
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Converter<Collection<?>> create(Type forType, Genson genson) {
			Converter<?> elementConverter = genson.provideConverter(TypeUtil
					.getCollectionType(forType));
			return new CollectionConverter(
					TypeUtil.getRawClass(TypeUtil.getCollectionType(forType)), elementConverter);
		}
	};

	@HandleClassMetadata
	public static class ArrayConverter<E> implements Converter<Object> {
		private final Class<E> eClass;
		private final Converter<E> elementConverter;

		public ArrayConverter(Class<E> eClass, Converter<E> elementConverter) {
			this.eClass = eClass;
			this.elementConverter = elementConverter;
		}

		public void serialize(Object array, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.beginArray();
			int len = Array.getLength(array);
			for (int i = 0; i < len; i++) {
				@SuppressWarnings("unchecked")
				E e = (E) Array.get(array, i);
				elementConverter.serialize(e, writer, ctx);
			}
			writer.endArray();
		}

		public Object deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			List<E> list = new ArrayList<E>();
			reader.beginArray();
			for (; reader.hasNext();) {
				reader.next();
				list.add(elementConverter.deserialize(reader, ctx));
			}
			reader.endArray();

			int size = list.size();
			Object array = Array.newInstance(eClass, size);
			for (int i = 0; i < size; i++)
				Array.set(array, i, list.get(i));
			// int idx = 0;
			// for (; reader.hasNext();) {
			// reader.next();
			// if (idx >= size) {
			// size = size * 2 + 1;
			// array = expandArray(array, idx, size);
			// }
			// Array.set(array, idx++, elementConverter.deserialize(reader, ctx));
			// }
			// reader.endArray();
			// if (idx < size) {
			// array = expandArray(array, idx, idx);
			// }

			return array;
		}

		private Object expandArray(Object array, int len, int size) {
			Object tmpArray = Array.newInstance(eClass, size);
			System.arraycopy(array, 0, tmpArray, 0, len);
			return tmpArray;
		}
	}

	public final static class ArrayConverterFactory implements Factory<Converter<Object>> {
		public final static ArrayConverterFactory instance = new ArrayConverterFactory();

		private ArrayConverterFactory() {
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Converter<Object> create(Type forType, Genson genson) {
			if (forType instanceof GenericArrayType
					|| (forType instanceof Class<?> && ((Class<?>) forType).isArray())) {
				Converter<?> elementConverter = genson.provideConverter(TypeUtil
						.getCollectionType(forType));
				return new ArrayConverter(
						TypeUtil.getRawClass(TypeUtil.getCollectionType(forType)), elementConverter);
			}
			return null;
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public final static class StringConverter implements Converter<String> {
		public final static StringConverter instance = new StringConverter();

		private StringConverter() {
		}

		public void serialize(String value, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(value);
		}

		public String deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return reader.valueAsString();
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public final static class BooleanConverter implements Converter<Boolean> {
		public final static BooleanConverter instance = new BooleanConverter();

		private BooleanConverter() {
		}

		public void serialize(Boolean obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(obj.booleanValue());
		}

		public Boolean deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			if (ValueType.STRING.equals(reader.getValueType())) {
				String value = reader.valueAsString();
				return "".equals(value) ? null : Boolean.valueOf(value);
			}
			return reader.valueAsBoolean();
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public final static class IntegerConverter implements Converter<Integer> {
		public final static IntegerConverter instance = new IntegerConverter();

		private IntegerConverter() {
		}

		public void serialize(Integer obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(obj.intValue());
		}

		public Integer deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			if (ValueType.STRING.equals(reader.getValueType())) {
				String value = reader.valueAsString();
				return "".equals(value) ? null : Integer.valueOf(value);
			}
			return reader.valueAsInt();
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public final static class LongConverter implements Converter<Long> {
		public final static LongConverter instance = new LongConverter();

		private LongConverter() {
		}

		public Long deserialize(ObjectReader reader, Context ctx) throws NumberFormatException,
				IOException {
			if (ValueType.STRING.equals(reader.getValueType())) {
				String value = reader.valueAsString();
				return "".equals(value) ? null : Long.valueOf(value);
			}
			return reader.valueAsLong();
		}

		public void serialize(Long obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(obj.longValue());
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public final static class DoubleConverter implements Converter<Double> {
		public final static DoubleConverter instance = new DoubleConverter();

		private DoubleConverter() {
		}

		public Double deserialize(ObjectReader reader, Context ctx) throws NumberFormatException,
				IOException {
			if (ValueType.STRING.equals(reader.getValueType())) {
				String value = reader.valueAsString();
				return "".equals(value) ? null : Double.valueOf(value);
			}
			return reader.valueAsDouble();
		}

		public void serialize(Double obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(obj.doubleValue());
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public final static class NumberConverter implements Converter<Number> {
		public final static NumberConverter instance = new NumberConverter();

		private NumberConverter() {
		}

		public Number deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			ValueType vt = reader.getValueType();
			if (ValueType.INTEGER.equals(vt))
				return reader.valueAsInt();
			else if (ValueType.DOUBLE.equals(vt))
				return reader.valueAsDouble();
			else {
				String value = reader.valueAsString();
				return "".equals(value) ? null : parse(value, vt);
			}
		}

		public void serialize(Number obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(obj);
		}

		private Number parse(String value, ValueType valueType) {
			try {
				if (value.indexOf('.') >= 0) {
					return Double.parseDouble(value);
				}
				long longValue = Long.parseLong(value);
				if (longValue <= Integer.MAX_VALUE && longValue >= Integer.MIN_VALUE) {
					return Integer.valueOf((int) longValue);
				}
				return Long.valueOf(value);
			} catch (NumberFormatException nfe) {
				throw new TransformationRuntimeException("Could not convert input value " + value
						+ " of type " + valueType.toClass() + " to a Number type.", nfe);
			}
		}
	};

	public final static class PrimitiveConverterFactory implements Factory<Converter<?>> {
		public final static PrimitiveConverterFactory instance = new PrimitiveConverterFactory();

		private PrimitiveConverterFactory() {
		}

		public Converter<?> create(Type type, Genson genson) {
			Class<?> rawClass = TypeUtil.getRawClass(type);
			if (rawClass.isPrimitive()) {
				if (rawClass.equals(boolean.class)) return booleanConverter.instance;
				if (rawClass.equals(int.class)) return intConverter.instance;
				if (rawClass.equals(double.class)) return doubleConverter.instance;
				if (rawClass.equals(long.class)) return longConverter.instance;
			}
			return null;
		}

		@HandleClassMetadata
		@HandleNull
		@WithoutBeanView
		public final static class booleanConverter implements Converter<Boolean> {
			public final static booleanConverter instance = new booleanConverter();

			private booleanConverter() {
			}

			public void serialize(Boolean obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeValue(obj.booleanValue());
			}

			public Boolean deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return ValueType.NULL.equals(reader.getValueType()) ? false : reader
						.valueAsBoolean();
			}
		};

		@HandleClassMetadata
		@HandleNull
		@WithoutBeanView
		public final static class intConverter implements Converter<Integer> {
			public final static intConverter instance = new intConverter();

			private intConverter() {
			}

			public void serialize(Integer obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeValue(obj);
			}

			public Integer deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return ValueType.NULL.equals(reader.getValueType()) ? 0 : reader.valueAsInt();
			}
		};

		@HandleClassMetadata
		@HandleNull
		@WithoutBeanView
		public final static class doubleConverter implements Converter<Double> {
			public final static doubleConverter instance = new doubleConverter();

			private doubleConverter() {
			}

			public void serialize(Double obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeValue(obj);
			}

			public Double deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return ValueType.NULL.equals(reader.getValueType()) ? 0d : reader.valueAsDouble();
			}
		};

		@HandleClassMetadata
		@HandleNull
		@WithoutBeanView
		public final static class longConverter implements Converter<Long> {
			public final static longConverter instance = new longConverter();

			private longConverter() {
			}

			public void serialize(Long obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				writer.writeValue(obj);
			}

			public Long deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				return ValueType.NULL.equals(reader.getValueType()) ? 0l : reader.valueAsLong();
			}
		};
	};

	@HandleClassMetadata
	public static class MapConverter<V> implements Converter<Map<String, V>> {
		private final Class<V> vClass;
		private final Converter<V> valueConverter;

		public MapConverter(Class<V> vClass, Converter<V> valueConverter) {
			this.vClass = vClass;
			this.valueConverter = valueConverter;
		}

		public Map<String, V> deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			reader.beginObject();
			Map<String, V> map = new HashMap<String, V>();
			for (; reader.hasNext();) {
				reader.next();
				String name = reader.name();
				V e = valueConverter.deserialize(reader, ctx);
				map.put(name, e);
			}
			reader.endObject();
			return map;
		}

		public void serialize(Map<String, V> obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.beginObject();
			for (Map.Entry<String, V> entry : obj.entrySet()) {
				writer.writeName(entry.getKey());
				V value = entry.getValue();
				if (value != null) {
					if (vClass.isInstance(value))
						valueConverter.serialize(value, writer, ctx);
					else
						valueConverter.serialize(value, writer, ctx);
				} else
					writer.writeNull();
			}
			writer.endObject();
		}
	}

	public final static class MapConverterFactory implements Factory<Converter<Map<?, ?>>> {
		public final static MapConverterFactory instance = new MapConverterFactory();

		private MapConverterFactory() {
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Converter<Map<?, ?>> create(Type type, Genson genson) {
			return new MapConverter(TypeUtil.getRawClass(type), genson.provideConverter(TypeUtil
					.typeOf(1, type)));
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public static class DateConverter implements Converter<Date> {
		private DateFormat dateFormat;

		public DateConverter() {
			this(SimpleDateFormat.getDateInstance());
		}

		public DateConverter(DateFormat dateFormat) {
			if (dateFormat == null) dateFormat = SimpleDateFormat.getDateInstance();
			this.dateFormat = dateFormat;
		}

		public void serialize(Date obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeUnsafeValue(format(obj));
		}

		protected synchronized String format(Date date) {
			return dateFormat.format(date);
		}

		public Date deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			try {
				return read(reader.valueAsString());
			} catch (ParseException e) {
				throw new TransformationException("Could not parse date " + reader.valueAsString(),
						e);
			}
		}

		protected synchronized Date read(String dateString) throws ParseException {
			return dateFormat.parse(dateString);
		}
	}

	public final static class UntypedConverterFactory implements Factory<Converter<Object>> {
		public final static UntypedConverterFactory instance = new UntypedConverterFactory();

		private UntypedConverterFactory() {
		}

		private final static class UntypedConverter implements Converter<Object> {
			final static UntypedConverter instance = new UntypedConverter();

			private UntypedConverter() {
			}

			public Object deserialize(ObjectReader reader, Context ctx)
					throws TransformationException, IOException {
				if (ValueType.OBJECT.equals(reader.getValueType()))
					return ctx.genson.deserialize(Map.class, reader, ctx);
				return ctx.genson.deserialize(reader.getValueType().toClass(), reader, ctx);
			}

			public void serialize(Object obj, ObjectWriter writer, Context ctx)
					throws TransformationException, IOException {
				if (Object.class.equals(obj.getClass()))
					throw new UnsupportedOperationException(
							"Serialization of type Object is not supported by default serializers.");
				ctx.genson.serialize(obj, obj.getClass(), writer, ctx);
			}
		};

		public Converter<Object> create(Type type, Genson genson) {
			if (TypeUtil.match(type, Object.class, true)) {
				return UntypedConverter.instance;
			}
			return null;
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public static class EnumConverter<T extends Enum<T>> implements Converter<T> {
		private final Class<T> eClass;

		public EnumConverter(Class<T> eClass) {
			this.eClass = eClass;
		}

		public void serialize(T obj, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeUnsafeValue(obj.name());
		}

		public T deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return Enum.valueOf(eClass, reader.valueAsString());
		}
	}

	public final static class EnumConverterFactory implements Factory<Converter<? extends Enum<?>>> {
		public final static EnumConverterFactory instance = new EnumConverterFactory();

		private EnumConverterFactory() {
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Converter<Enum<?>> create(Type type, Genson genson) {
			Class<?> rawClass = TypeUtil.getRawClass(type);
			return rawClass.isEnum() || Enum.class.isAssignableFrom(rawClass) ? new EnumConverter(
					rawClass) : null;
		}
	};

	@HandleClassMetadata
	@WithoutBeanView
	public static class URLConverter implements Converter<URL> {
		public final static URLConverter instance = new URLConverter();

		private URLConverter() {
		}

		public URL deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return new URL(reader.valueAsString());
		}

		public void serialize(URL object, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(object.toExternalForm());
		}
	}

	@HandleClassMetadata
	@WithoutBeanView
	public static class URIConverter implements Converter<URI> {
		public final static URIConverter instance = new URIConverter();

		private URIConverter() {
		}

		public void serialize(URI object, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeUnsafeValue(object.toString());
		}

		public URI deserialize(ObjectReader reader, Context ctx) throws TransformationException,
				IOException {
			return URI.create(reader.valueAsString());
		}
	}

	@HandleClassMetadata
	@WithoutBeanView
	public static class BigDecimalConverter implements Converter<BigDecimal> {
		public final static BigDecimalConverter instance = new BigDecimalConverter();

		private BigDecimalConverter() {
		}

		@Override
		public BigDecimal deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return new BigDecimal(reader.valueAsString());
		}

		@Override
		public void serialize(BigDecimal object, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(object);
		}
	}

	@HandleClassMetadata
	@WithoutBeanView
	public static class BigIntegerConverter implements Converter<BigInteger> {
		public final static BigIntegerConverter instance = new BigIntegerConverter();

		private BigIntegerConverter() {
		}

		@Override
		public BigInteger deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return new BigInteger(reader.valueAsString());
		}

		@Override
		public void serialize(BigInteger object, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(object);
		}
	}

	@HandleClassMetadata
	@WithoutBeanView
	public static class TimestampConverter implements Converter<Timestamp> {
		public final static TimestampConverter instance = new TimestampConverter();

		private TimestampConverter() {
		}

		@Override
		public Timestamp deserialize(ObjectReader reader, Context ctx)
				throws TransformationException, IOException {
			return Timestamp.valueOf(reader.valueAsString());
		}

		@Override
		public void serialize(Timestamp object, ObjectWriter writer, Context ctx)
				throws TransformationException, IOException {
			writer.writeValue(object.toString());
		}
	}
}
