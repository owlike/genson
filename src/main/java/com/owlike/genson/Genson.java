package com.owlike.genson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.owlike.genson.convert.BasicConvertersFactory;
import com.owlike.genson.convert.BeanViewConverter;
import com.owlike.genson.convert.ChainedFactory;
import com.owlike.genson.convert.CircularClassReferenceConverterFactory;
import com.owlike.genson.convert.ClassMetadataConverter;
import com.owlike.genson.convert.DefaultConverters;
import com.owlike.genson.convert.NullConverter;
import com.owlike.genson.convert.RuntimeTypeConverter;
import com.owlike.genson.reflect.ASMCreatorParameterNameResolver;
import com.owlike.genson.reflect.BaseBeanDescriptorProvider;
import com.owlike.genson.reflect.BeanDescriptorProvider;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import com.owlike.genson.reflect.BeanViewDescriptorProvider;
import com.owlike.genson.reflect.PropertyNameResolver;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.reflect.VisibilityFilter;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver.CompositeResolver;
import com.owlike.genson.reflect.PropertyNameResolver.CompositePropertyNameResolver;
import com.owlike.genson.stream.JsonReader;
import com.owlike.genson.stream.JsonWriter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

/**
 * <p>
 * Main class of the library. Instances of this class are intended to be reused. You can instantiate
 * it multiple times but it is better to have an instance per configuration so you can benefit of
 * better performances. This class is immutable and thread safe.
 * 
 * For more examples have a look at the <a
 * href="http://code.google.com/p/genson/wiki/GettingStarted">wiki</a>.
 * 
 * <p>
 * To create a new instance of Genson you can use the default no arg constructor or the
 * {@link Builder} class to have control over Gensons configuration. <br>
 * A basic usage of Genson would be:
 * 
 * <pre>
 * Genson genson = new Genson();
 * String json = genson.serialize(new int[] { 1, 2, 3 });
 * int[] arrayOfInt = genson.deserialize(json, int[].class);
 * // (multi-dimensional arrays are also supported)
 * 
 * // genson can also deserialize primitive types without class information
 * Number[] arrayOfNumber = genson.deserialize(&quot;[1, 2.03, 8877463]&quot;, Number[].class);
 * // or even
 * Object[] arrayOfUnknownTypes = genson
 * 		.deserialize(&quot;[1, false, null, 4.05, \&quot;hey this is a string!\&quot;]&quot;);
 * // it can also deserialize json objects of unknown type to maps!
 * Map&lt;String, Object&gt; map = (Map&lt;String, Object&gt;) genson.deserialize(&quot;{\&quot;foo\&quot;:1232}&quot;, Object.class);
 * </pre>
 * 
 * Every object serialized with Genson, can be deserialized back into its concrete type! Have a look
 * at {@link com.owlike.genson.convert.ClassMetadataConverter ClassMetadataConverter} for an
 * explanation and examples.
 * 
 * @author eugen
 */
public final class Genson {
	/**
	 * Default genson configuration, the default configuration (sers, desers, etc) will be shared
	 * accros all default Genson instances.
	 */
	private final static Genson _default = new Builder().create();

	private final ConcurrentHashMap<Type, Converter<?>> converterCache = new ConcurrentHashMap<Type, Converter<?>>();
	private final Factory<Converter<?>> converterFactory;
	private final BeanDescriptorProvider beanDescriptorFactory;
	private final Converter<Object> nullConverter;

	private final Map<Class<?>, String> classAliasMap;
	private final Map<String, Class<?>> aliasClassMap;

	private final boolean skipNull;
	private final boolean htmlSafe;
	private final boolean withClassMetadata;
	private final boolean strictDoubleParse;
	private final String indentation;

	/**
	 * The default constructor will use the default configuration provided by the {@link Builder}.
	 * In most cases using this default constructor will suffice.
	 */
	public Genson() {
		this(_default.converterFactory, _default.beanDescriptorFactory, _default.nullConverter,
				_default.skipNull, _default.htmlSafe, _default.aliasClassMap,
				_default.withClassMetadata, _default.strictDoubleParse, _default.indentation);
	}

	/**
	 * Instead of using this constructor you should use {@link Builder}.
	 * 
	 * @param converterFactory
	 *            providing instance of converters.
	 * @param beanDescProvider
	 *            providing instance of {@link com.owlike.genson.reflect.BeanDescriptor
	 *            BeanDescriptor} used during bean serialization and deserialization.
	 * @param nullConverter
	 *            handling null objects. If its serialize/deserialize methods are called you are
	 *            sure that it is a null value. This converter is used in
	 *            {@link com.owlike.genson.convert.NullConverter NullConverter}
	 * @param skipNull
	 *            indicates whether null values should be serialized. False by default, null values
	 *            will be serialized.
	 * @param htmlSafe
	 *            indicates whether \,<,>,&,= characters should be replaced by their Unicode
	 *            representation.
	 * @param classAliases
	 *            association map between classes and their aliases, used if withClassMetadata is
	 *            true.
	 * @param withClassMetadata
	 *            indicates whether class name should be serialized and used during deserialization
	 *            to determine the type. False by default.
	 * @param strictDoubleParse
	 *            indicates whether to use or not double approximation. If false (by default) it
	 *            enables Genson custom double parsing algorithm, that is an approximation of
	 *            Double.parse but is a lot faster. If true, Double.parse method will be usead
	 *            instead. In most cases you should be fine with Genson algorithm, but if for some
	 *            reason you need to have 100% match with Double.parse, then enable strict parsing.
	 * @param indentation
	 *            the characters to use for indentation, for example "  " will use an indentation of
	 *            two spaces.
	 */
	public Genson(Factory<Converter<?>> converterFactory, BeanDescriptorProvider beanDescProvider,
			Converter<Object> nullConverter, boolean skipNull, boolean htmlSafe,
			Map<String, Class<?>> classAliases, boolean withClassMetadata,
			boolean strictDoubleParse, String indentation) {
		this.converterFactory = converterFactory;
		this.beanDescriptorFactory = beanDescProvider;
		this.nullConverter = nullConverter;
		this.skipNull = skipNull;
		this.htmlSafe = htmlSafe;
		this.aliasClassMap = classAliases;
		this.withClassMetadata = withClassMetadata;
		this.classAliasMap = new HashMap<Class<?>, String>(classAliases.size());
		for (Map.Entry<String, Class<?>> entry : classAliases.entrySet()) {
			classAliasMap.put(entry.getValue(), entry.getKey());
		}
		this.strictDoubleParse = strictDoubleParse;
		this.indentation = indentation;
	}

	/**
	 * Provides an instance of Converter capable of handling objects of type forType.
	 * 
	 * @param forType
	 *            the type for which a converter is needed.
	 * @return the converter instance.
	 */
	@SuppressWarnings("unchecked")
	public <T> Converter<T> provideConverter(Type forType) {
		Converter<T> converter = (Converter<T>) converterCache.get(forType);
		if (converter == null) {
			converter = (Converter<T>) converterFactory.create(forType, this);
			if (converter == null)
				throw new TransformationRuntimeException("No converter found for type " + forType);
			converterCache.putIfAbsent(forType, converter);
		}
		return converter;
	}

	/**
	 * Serializes the object into a string.
	 * 
	 * @param o
	 *            object to be serialized.
	 * @return the serialized object as a string.
	 * @throws TransformationException
	 *             if there was any kind of error during serialization.
	 * @throws IOException
	 *             if there was a problem during writing of the object to the output.
	 */
	public <T> String serialize(T o) throws TransformationException, IOException {
		StringWriter sw = new StringWriter();
		ObjectWriter writer = createWriter(sw);
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, o.getClass(), writer, new Context(this));
		writer.flush();
		return sw.toString();
	}

	/**
	 * Serializes the object using its GenericType instead of using its runtime type.
	 * 
	 * @param o
	 *            object to be serialized.
	 * @param type
	 *            the type of the object to be serialized.
	 * @return its string representation.
	 * @throws TransformationException
	 * @throws IOException
	 */
	public <T> String serialize(T o, GenericType<T> type) throws TransformationException,
			IOException {
		StringWriter sw = new StringWriter();
		ObjectWriter writer = createWriter(sw);
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, type.getType(), writer, new Context(this));
		writer.flush();
		return sw.toString();
	}

	/**
	 * Serializes the object using the specified BeanViews.
	 * 
	 * @see BeanView
	 * @param o
	 * @param withViews
	 *            the BeanViews to apply during this serialization.
	 * @return
	 * @throws TransformationException
	 * @throws IOException
	 */
	public <T> String serialize(T o, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		StringWriter sw = new StringWriter();
		ObjectWriter writer = createWriter(sw);
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, o.getClass(), writer, new Context(this, Arrays.asList(withViews)));
		writer.flush();
		return sw.toString();
	}

	/**
	 * Serializes this object and writes its representation to writer.
	 * 
	 * @param o
	 * @param writer
	 *            into which to write the serialized object.
	 * @param withViews
	 * @throws TransformationException
	 * @throws IOException
	 */
	public <T> void serialize(T o, ObjectWriter writer, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, o.getClass(), writer, new Context(this, Arrays.asList(withViews)));
		writer.flush();
	}

	public <T> void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		Serializer<T> ser = provideConverter(type);
		ser.serialize(obj, writer, ctx);
	}

	/**
	 * Deserializes fromSource String into an instance of toClass.
	 * 
	 * @param fromSource
	 *            source from which to deserialize.
	 * @param toClass
	 *            type into which to deserialize.
	 * @return
	 * @throws TransformationException
	 * @throws IOException
	 */
	public <T> T deserialize(String fromSource, Class<T> toClass) throws TransformationException,
			IOException {
		return deserialize(toClass, createReader(new StringReader(fromSource)), new Context(this));
	}

	public <T> T deserialize(String fromSource, GenericType<T> toType)
			throws TransformationException, IOException {
		return deserialize(toType.getType(), createReader(new StringReader(fromSource)),
				new Context(this));
	}

	public <T> T deserialize(Reader reader, Type toType) throws TransformationException,
			IOException {
		return deserialize(toType, createReader(reader), new Context(this));
	}

	public <T> T deserialize(String fromSource, Type toType) throws TransformationException,
			IOException {
		StringReader reader = new StringReader(fromSource);
		return deserialize(toType, createReader(reader), new Context(this, null));
	}

	public <T> T deserialize(String fromSource, Type toType,
			Class<? extends BeanView<?>>... withViews) throws TransformationException, IOException {
		StringReader reader = new StringReader(fromSource);
		return deserialize(toType, createReader(reader),
				new Context(this, Arrays.asList(withViews)));
	}

	public <T> T deserialize(Type type, Reader reader, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		return deserialize(type, createReader(reader), new Context(this, Arrays.asList(withViews)));
	}

	public <T> T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		Deserializer<T> deser = provideConverter(type);
		return deser.deserialize(reader, ctx);
	}

	public <T> String aliasFor(Class<T> clazz) {
		String alias = classAliasMap.get(clazz);
		if (alias == null) {
			alias = clazz.getName();
			classAliasMap.put(clazz, alias);
		}
		return alias;
	}

	public Class<?> classFor(String alias) throws ClassNotFoundException {
		Class<?> clazz = aliasClassMap.get(alias);
		if (clazz == null) {
			clazz = Class.forName(alias);
			aliasClassMap.put(alias, clazz);
		}
		return clazz;
	}

	public ObjectWriter createWriter(OutputStream os) {
		return new JsonWriter(new OutputStreamWriter(os), skipNull, htmlSafe, indentation);
	}

	public ObjectReader createReader(InputStream is) {
		return new JsonReader(new InputStreamReader(is), strictDoubleParse, withClassMetadata);
	}

	public ObjectWriter createWriter(Writer writer) {
		return new JsonWriter(writer, skipNull, htmlSafe, indentation);
	}

	public ObjectReader createReader(Reader reader) {
		return new JsonReader(reader, strictDoubleParse, withClassMetadata);
	}

	public boolean isSkipNull() {
		return skipNull;
	}

	public boolean isHtmlSafe() {
		return htmlSafe;
	}

	public boolean isWithClassMetadata() {
		return withClassMetadata;
	}

	public BeanDescriptorProvider getBeanDescriptorFactory() {
		return beanDescriptorFactory;
	}

	public Converter<Object> getNullConverter() {
		return nullConverter;
	}

	/**
	 * Use the Builder class when you want to create a custom Genson instance. This class allows you
	 * for example to register custom converters/serializers/deserializers
	 * {@link #withConverters(Converter...)} or custom converter Factories
	 * {@link #withConverterFactory(Factory)}.
	 * 
	 * This class combines the builder design pattern with template pattern providing handy
	 * configuration and extensibility. All its public methods are intended to be used in the
	 * builder "style" and its protected methods are part of the template. When you call
	 * {@link #create()} method, it will start assembling all the configuration and build all the
	 * required components by using the protected methods. For example if you wish to use in your
	 * projects a Builder that will always create some custom
	 * {@link com.owlike.genson.reflect.BeanDescriptorProvider BeanDescriptorProvider} you have to
	 * extend {@link #createBeanDescriptorProvider()}, or imagine that you implemented some
	 * Converters that you always want to register then override {@link #getDefaultConverters()}.
	 * 
	 * @author eugen
	 * 
	 */
	public static class Builder {
		private final Map<Type, Serializer<?>> serializersMap = new HashMap<Type, Serializer<?>>();
		private final Map<Type, Deserializer<?>> deserializersMap = new HashMap<Type, Deserializer<?>>();
		private final List<Factory<?>> converterFactories = new ArrayList<Factory<?>>();

		private boolean skipNull = false;
		private boolean htmlSafe = false;
		private boolean withClassMetadata = false;
		private DateFormat dateFormat = null;
		private boolean throwExcOnNoDebugInfo = false;
		private boolean useGettersAndSetters = true;
		private boolean useFields = true;
		private boolean withBeanViewConverter = false;
		private boolean useRuntimeTypeForSerialization = false;
		private boolean withDebugInfoPropertyNameResolver = false;
		private boolean strictDoubleParse = false;
		private String indentation = null;

		private PropertyNameResolver propertyNameResolver;
		private BeanMutatorAccessorResolver mutatorAccessorResolver;
		private VisibilityFilter propertyFilter;
		private VisibilityFilter methodFilter;
		private VisibilityFilter constructorFilter;

		private BeanDescriptorProvider beanDescriptorProvider;
		private Converter<Object> nullConverter;

		// for the moment we don't allow to override
		private BeanViewDescriptorProvider beanViewDescriptorProvider;

		private final Map<String, Class<?>> withClassAliases = new HashMap<String, Class<?>>();

		public Builder() {
		}

		/**
		 * Alias used in serialized class metadata instead of the full class name. See
		 * {@link com.owlike.genson.convert.ClassMetadataConverter ClassMetadataConverter} for more
		 * metadata. If you add an alias, it will automatically enable the class metadata feature,
		 * as if you used {@link #setWithClassMetadata(boolean)}.
		 * 
		 * @param alias
		 * @param forClass
		 * @return a reference to this builder.
		 */
		public Builder addAlias(String alias, Class<?> forClass) {
			withClassMetadata = true;
			withClassAliases.put(alias, forClass);
			return this;
		}

		/**
		 * Registers converters mapping them to their corresponding parameterized type.
		 * 
		 * @param converter
		 * @return a reference to this builder.
		 */
		public Builder withConverters(Converter<?>... converter) {
			for (Converter<?> c : converter) {
				Type typeOfConverter = TypeUtil.typeOf(0,
						TypeUtil.lookupGenericType(Converter.class, c.getClass()));
				typeOfConverter = TypeUtil.expandType(typeOfConverter, c.getClass());
				registerConverter(c, typeOfConverter);
			}
			return this;
		}

		/**
		 * Register converter by mapping it to type argument.
		 * 
		 * @param converter
		 *            to register
		 * @param type
		 *            of objects this converter handles
		 * @return a reference to this builder.
		 */
		public <T> Builder withConverter(Converter<T> converter, Class<? extends T> type) {
			registerConverter(converter, type);
			return this;
		}

		/**
		 * Register converter by mapping it to the parameterized type of type argument.
		 * 
		 * @param converter
		 *            to register
		 * @param type
		 *            of objects this converter handles
		 * @return a reference to this builder.
		 */
		public <T> Builder withConverter(Converter<T> converter, GenericType<? extends T> type) {
			registerConverter(converter, type.getType());
			return this;
		}

		private <T> void registerConverter(Converter<T> converter, Type type) {
			if (serializersMap.containsKey(type))
				throw new IllegalStateException("Can not register converter "
						+ converter.getClass()
						+ ". A custom serializer is already registered for type " + type);
			if (deserializersMap.containsKey(type))
				throw new IllegalStateException("Can not register converter "
						+ converter.getClass()
						+ ". A custom deserializer is already registered for type " + type);
			serializersMap.put(type, converter);
			deserializersMap.put(type, converter);
		}

		public Builder withSerializers(Serializer<?>... serializer) {
			for (Serializer<?> s : serializer) {
				Type typeOfConverter = TypeUtil.typeOf(0,
						TypeUtil.lookupGenericType(Serializer.class, s.getClass()));
				typeOfConverter = TypeUtil.expandType(typeOfConverter, s.getClass());
				registerSerializer(s, typeOfConverter);
			}
			return this;
		}

		public <T> Builder withSerializer(Serializer<T> serializer, Class<? extends T> type) {
			registerSerializer(serializer, type);
			return this;
		}

		public <T> Builder withSerializer(Serializer<T> serializer, GenericType<? extends T> type) {
			registerSerializer(serializer, type.getType());
			return this;
		}

		private <T> void registerSerializer(Serializer<T> serializer, Type type) {
			if (serializersMap.containsKey(type))
				throw new IllegalStateException("Can not register serializer "
						+ serializer.getClass()
						+ ". A custom serializer is already registered for type " + type);
			serializersMap.put(type, serializer);
		}

		public Builder withDeserializers(Deserializer<?>... deserializer) {
			for (Deserializer<?> d : deserializer) {
				Type typeOfConverter = TypeUtil.typeOf(0,
						TypeUtil.lookupGenericType(Deserializer.class, d.getClass()));
				typeOfConverter = TypeUtil.expandType(typeOfConverter, d.getClass());
				registerDeserializer(d, typeOfConverter);
			}
			return this;
		}

		public <T> Builder withDeserializer(Deserializer<T> deserializer, Class<? extends T> type) {
			registerDeserializer(deserializer, type);
			return this;
		}

		public <T> Builder withDeserializer(Deserializer<T> deserializer,
				GenericType<? extends T> type) {
			registerDeserializer(deserializer, type.getType());
			return this;
		}

		private <T> void registerDeserializer(Deserializer<T> deserializer, Type type) {
			if (deserializersMap.containsKey(type))
				throw new IllegalStateException("Can not register deserializer "
						+ deserializer.getClass()
						+ ". A custom deserializer is already registered for type " + type);
			deserializersMap.put(type, deserializer);
		}

		/**
		 * Registers converter factories.
		 * 
		 * @param factory
		 *            to register
		 * @return a reference to this builder.
		 */
		public Builder withConverterFactory(Factory<? extends Converter<?>> factory) {
			converterFactories.add(factory);
			return this;
		}

		/**
		 * Registers serializer factories.
		 * 
		 * @param factory
		 *            to register
		 * @return a reference to this builder.
		 */
		public Builder withSerializerFactory(Factory<? extends Serializer<?>> factory) {
			converterFactories.add(factory);
			return this;
		}

		/**
		 * Registers deserializer factories.
		 * 
		 * @param factory
		 *            to register
		 * @return a reference to this builder.
		 */
		public Builder withDeserializerFactory(Factory<? extends Deserializer<?>> factory) {
			converterFactories.add(factory);
			return this;
		}

		/**
		 * Renames all fields named field to toName.
		 */
		public Builder rename(String field, String toName) {
			return rename(field, null, toName, null);
		}

		/**
		 * Renames all fields of type fieldOfType to toName.
		 */
		public Builder rename(Class<?> fieldOfType, String toName) {
			return rename(null, null, toName, fieldOfType);
		}

		/**
		 * Renames all fields named field declared in class fromClass to toName.
		 */
		public Builder rename(String field, Class<?> fromClass, String toName) {
			return rename(field, fromClass, toName, null);
		}

		/**
		 * Renames all fields named field and of type fieldOfType to toName.
		 */
		public Builder rename(String field, String toName, Class<?> fieldOfType) {
			return rename(field, null, toName, fieldOfType);
		}

		/**
		 * Renames all fields named field, of type fieldOfType and declared in fromClass to toName.
		 */
		public Builder rename(final String field, final Class<?> fromClass, final String toName,
				final Class<?> ofType) {
			return with(new PropertyNameResolver() {

				@Override
				public String resolve(int parameterIdx, Constructor<?> fromConstructor) {
					return null;
				}

				@Override
				public String resolve(int parameterIdx, Method fromMethod) {
					return null;
				}

				@Override
				public String resolve(Field fromField) {
					return tryToRename(fromField.getName(), fromField.getDeclaringClass(),
							fromField.getType());
				}

				@Override
				public String resolve(Method fromMethod) {
					String name = fromMethod.getName();
					if (name.startsWith("is") && name.length() > 2) {
						return tryToRename(name.substring(2), fromMethod.getDeclaringClass(),
								fromMethod.getReturnType());
					}
					if (name.length() > 3) {
						if (name.startsWith("get"))
							return tryToRename(name.substring(3), fromMethod.getDeclaringClass(),
									fromMethod.getReturnType());
						if (name.startsWith("set"))
							return tryToRename(name.substring(3), fromMethod.getDeclaringClass(),
									fromMethod.getParameterTypes()[0]);
					}
					return null;
				}

				private String tryToRename(String actualName, Class<?> declaringClass,
						Class<?> propertyType) {
					if ((field == null || actualName.equalsIgnoreCase(field))
							&& (fromClass == null || fromClass.isAssignableFrom(declaringClass))
							&& (ofType == null || ofType.isAssignableFrom(propertyType)))
						return toName;
					return null;
				}
			});
		}

		public Builder exclude(String field) {
			return filter(field, null, null, true);
		}

		public Builder exclude(Class<?> fieldOfType) {
			return filter(null, null, fieldOfType, true);
		}

		public Builder exclude(String field, Class<?> fromClass) {
			return filter(field, fromClass, null, true);
		}

		public Builder exclude(String field, Class<?> fromClass, Class<?> ofType) {
			return filter(field, fromClass, ofType, true);
		}

		public Builder include(String field) {
			return filter(field, null, null, false);
		}

		public Builder include(Class<?> fieldOfType) {
			return filter(null, null, fieldOfType, false);
		}

		public Builder include(String field, Class<?> fromClass) {
			return filter(field, fromClass, null, false);
		}

		public Builder include(String field, Class<?> fromClass, Class<?> ofType) {
			return filter(field, fromClass, ofType, false);
		}

		private Builder filter(final String field, final Class<?> declaringClass,
				final Class<?> ofType, final boolean exclude) {
			return with(new BeanMutatorAccessorResolver.BaseResolver() {
				@Override
				public Trilean isAccessor(Field field, Class<?> fromClass) {
					return filter(field.getName(), fromClass, field.getType(), exclude);
				}

				@Override
				public Trilean isMutator(Field field, Class<?> fromClass) {
					return filter(field.getName(), fromClass, field.getType(), exclude);
				}

				@Override
				public Trilean isAccessor(Method method, Class<?> fromClass) {
					String name = method.getName();
					if (name.startsWith("is") && name.length() > 2) {
						return filter(name.substring(2), method.getDeclaringClass(),
								method.getReturnType(), exclude);
					}
					if (name.length() > 3) {
						if (name.startsWith("get"))
							return filter(name.substring(3), method.getDeclaringClass(),
									method.getReturnType(), exclude);
					}
					return Trilean.UNKNOWN;
				}

				@Override
				public Trilean isMutator(Method method, Class<?> fromClass) {
					String name = method.getName();
					if (name.length() > 3) {
						if (name.startsWith("set"))
							return filter(name.substring(3), method.getDeclaringClass(),
									method.getParameterTypes()[0], exclude);
					}
					return Trilean.UNKNOWN;
				}

				private Trilean filter(String actualName, Class<?> fromClass,
						Class<?> propertyType, boolean exclude) {
					if ((field == null || actualName.equalsIgnoreCase(field))
							&& (declaringClass == null || declaringClass
									.isAssignableFrom(fromClass))
							&& (ofType == null || ofType.isAssignableFrom(propertyType)))
						return exclude ? Trilean.FALSE : Trilean.TRUE;
					return Trilean.UNKNOWN;
				}
			});
		}

		/**
		 * If true will not serialize null values
		 * 
		 * @param skipNull
		 *            indicates whether null values should be serialized or not.
		 * @return a reference to this builder.
		 */
		public Builder setSkipNull(boolean skipNull) {
			this.skipNull = skipNull;
			return this;
		}

		public boolean isSkipNull() {
			return skipNull;
		}

		/**
		 * If true \,<,>,&,= characters will be replaced by \u0027, \u003c, \u003e, \u0026, \u003d
		 * 
		 * @param htmlSafe
		 *            indicates whether serialized data should be html safe.
		 * @return a reference to this builder.
		 */
		public Builder setHtmlSafe(boolean htmlSafe) {
			this.htmlSafe = htmlSafe;
			return this;
		}

		public boolean isHtmlSafe() {
			return htmlSafe;
		}

		/**
		 * Replaces default {@link com.owlike.genson.reflect.BeanMutatorAccessorResolver
		 * BeanMutatorAccessorResolver} by the specified one.
		 * 
		 * @param resolver
		 * @return a reference to this builder.
		 */
		public Builder set(BeanMutatorAccessorResolver resolver) {
			mutatorAccessorResolver = resolver;
			return this;
		}

		/**
		 * Replaces default {@link com.owlike.genson.reflect.PropertyNameResolver
		 * PropertyNameResolver} by the specified one.
		 * 
		 * @param resolver
		 * @return a reference to this builder.
		 */
		public Builder set(PropertyNameResolver resolver) {
			propertyNameResolver = resolver;
			return this;
		}

		/**
		 * Register additional BeanMutatorAccessorResolver that will be used before the standard
		 * ones.
		 * 
		 * @param resolvers
		 * @return a reference to this builder.
		 */
		public Builder with(BeanMutatorAccessorResolver... resolvers) {
			if (mutatorAccessorResolver == null)
				mutatorAccessorResolver = createBeanMutatorAccessorResolver();
			if (mutatorAccessorResolver instanceof CompositeResolver)
				((CompositeResolver) mutatorAccessorResolver).add(resolvers);
			else
				throw new IllegalStateException(
						"You can not add multiple resolvers if the base resolver is not of type "
								+ CompositeResolver.class.getName());
			return this;
		}

		/**
		 * Registers the specified resolvers in the order they were defined and before the standard
		 * ones.
		 * 
		 * @param resolvers
		 * @return a reference to this builder.
		 */
		public Builder with(PropertyNameResolver... resolvers) {
			if (propertyNameResolver == null) propertyNameResolver = createPropertyNameResolver();
			if (propertyNameResolver instanceof CompositePropertyNameResolver)
				((CompositePropertyNameResolver) propertyNameResolver).add(resolvers);
			else
				throw new IllegalStateException(
						"You can not add multiple resolvers if the base resolver is not of type "
								+ CompositePropertyNameResolver.class.getName());
			return this;
		}

		public boolean isWithClassMetadata() {
			return withClassMetadata;
		}

		/**
		 * Indicates whether class metadata should be serialized and used during deserialization.
		 * 
		 * @see com.owlike.genson.convert.ClassMetadataConverter ClassMetadataConverter
		 * @param withClassMetadata
		 * @return a reference to this builder.
		 */
		public Builder setWithClassMetadata(boolean withClassMetadata) {
			this.withClassMetadata = withClassMetadata;
			return this;
		}

		public DateFormat getDateFormat() {
			return dateFormat;
		}

		/**
		 * Specifies the data format that should be used for java.util.Date serialization and
		 * deserialization.
		 * 
		 * @param dateFormat
		 * @return a reference to this builder.
		 */
		public Builder setDateFormat(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
			return this;
		}

		public boolean isThrowExceptionOnNoDebugInfo() {
			return throwExcOnNoDebugInfo;
		}

		/**
		 * Used in conjunction with {@link #setWithDebugInfoPropertyNameResolver(boolean)}. If true
		 * an exception will be thrown when a class has been compiled without debug informations.
		 * 
		 * @see com.owlike.genson.reflect.ASMCreatorParameterNameResolver
		 *      ASMCreatorParameterNameResolver
		 * @param throwExcOnNoDebugInfo
		 * @return a reference to this builder.
		 */
		public Builder setThrowExceptionIfNoDebugInfo(boolean throwExcOnNoDebugInfo) {
			this.throwExcOnNoDebugInfo = throwExcOnNoDebugInfo;
			return this;
		}

		public boolean isUseGettersAndSetters() {
			return useGettersAndSetters;
		}

		/**
		 * If true, getters and setters would be used during serialization/deserialization in favor
		 * of fields. If there is not getter/setter for a field then the field will be used, except
		 * if you specified that fields should not be used with {@link #setUseFields(boolean)}. By
		 * default getters, setters and fields will be used.
		 * 
		 * @param useGettersAndSetters
		 * @return a reference to this builder.
		 */
		public Builder setUseGettersAndSetters(boolean useGettersAndSetters) {
			this.useGettersAndSetters = useGettersAndSetters;
			return this;
		}

		public boolean isUseFields() {
			return useFields;
		}

		/**
		 * If true, fields will be used when no getter/setter is available, except if you specified
		 * that no getter/setter should be used with {@link #setUseGettersAndSetters(boolean)}, in
		 * that case only fields will be used. By default getters, setters and fields will be used.
		 * 
		 * @param useFields
		 * @return a reference to this builder.
		 */
		public Builder setUseFields(boolean useFields) {
			this.useFields = useFields;
			return this;
		}

		public boolean isWithBeanViewConverter() {
			return withBeanViewConverter;
		}

		/**
		 * If true {@link BeanView} mechanism will be enabled.
		 * 
		 * @param withBeanViewConverter
		 * @return a reference to this builder.
		 */
		public Builder setWithBeanViewConverter(boolean withBeanViewConverter) {
			this.withBeanViewConverter = withBeanViewConverter;
			return this;
		}

		public boolean isUseRuntimeTypeForSerialization() {
			return useRuntimeTypeForSerialization;
		}

		/**
		 * If true the concrete type of the serialized object will always be used. So if you have
		 * List<Number> type it will not use the Number serializer but the one for the concrete type
		 * of the current value.
		 * 
		 * @param useRuntimeTypeForSerialization
		 * @return a reference to this builder.
		 */
		public Builder setUseRuntimeTypeForSerialization(boolean useRuntimeTypeForSerialization) {
			this.useRuntimeTypeForSerialization = useRuntimeTypeForSerialization;
			return this;
		}

		public boolean isWithDebugInfoPropertyNameResolver() {
			return withDebugInfoPropertyNameResolver;
		}

		/**
		 * If true constructor and method arguments name will be resolved from the generated debug
		 * symbols during compilation. It is a very powerful feature from Genson, you should have a
		 * look at {@link com.owlike.genson.reflect.ASMCreatorParameterNameResolver
		 * ASMCreatorParameterNameResolver}.
		 * 
		 * @see #setThrowExceptionIfNoDebugInfo(boolean)
		 * @param withDebugInfoPropertyNameResolver
		 * @return a reference to this builder.
		 */
		public Builder setWithDebugInfoPropertyNameResolver(
				boolean withDebugInfoPropertyNameResolver) {
			this.withDebugInfoPropertyNameResolver = withDebugInfoPropertyNameResolver;
			return this;
		}

		public Converter<Object> getNullConverter() {
			return nullConverter;
		}

		/**
		 * Sets the null converter that should be used to handle null object values. If the
		 * converter is called you are guaranteed that the value is null (for both, ser and deser).
		 * 
		 * @param nullConverter
		 * @return a reference to this builder.
		 */
		public Builder setNullConverter(Converter<Object> nullConverter) {
			this.nullConverter = nullConverter;
			return this;
		}

		public VisibilityFilter getFieldFilter() {
			return propertyFilter;
		}

		public Builder setFieldFilter(VisibilityFilter propertyFilter) {
			this.propertyFilter = propertyFilter;
			return this;
		}

		public VisibilityFilter getMethodFilter() {
			return methodFilter;
		}

		public Builder setMethodFilter(VisibilityFilter methodFilter) {
			this.methodFilter = methodFilter;
			return this;
		}

		public VisibilityFilter getConstructorFilter() {
			return constructorFilter;
		}

		public Builder setConstructorFilter(VisibilityFilter constructorFilter) {
			this.constructorFilter = constructorFilter;
			return this;
		}

		public Map<Type, Serializer<?>> getSerializersMap() {
			return Collections.unmodifiableMap(serializersMap);
		}

		public Map<Type, Deserializer<?>> getDeserializersMap() {
			return Collections.unmodifiableMap(deserializersMap);
		}

		public boolean isStrictDoubleParse() {
			return strictDoubleParse;
		}

		public Builder setStrictDoubleParse(boolean strictDoubleParse) {
			this.strictDoubleParse = strictDoubleParse;
			return this;
		}

		public String getIndentation() {
			return indentation;
		}

		public Builder setIndentation(String indentation) {
			this.indentation = indentation; return this;
		}

		/**
		 * Creates an instance of Genson. You may use this method as many times you want. It wont
		 * change the state of the builder, in sense that the returned instance will have always the
		 * same configuration.
		 * 
		 * @return a new instance of Genson built for the current configuration.
		 */
		public Genson create() {
			if (nullConverter == null) nullConverter = new NullConverter();
			if (propertyNameResolver == null) propertyNameResolver = createPropertyNameResolver();
			if (mutatorAccessorResolver == null)
				mutatorAccessorResolver = createBeanMutatorAccessorResolver();

			beanDescriptorProvider = createBeanDescriptorProvider();

			if (withBeanViewConverter) {
				List<BeanMutatorAccessorResolver> resolvers = new ArrayList<BeanMutatorAccessorResolver>();
				resolvers.add(new BeanViewDescriptorProvider.BeanViewMutatorAccessorResolver());
				resolvers.add(mutatorAccessorResolver);
				beanViewDescriptorProvider = new BeanViewDescriptorProvider(
						new BeanMutatorAccessorResolver.CompositeResolver(resolvers),
						getPropertyNameResolver());
			}

			List<Converter<?>> converters = getDefaultConverters();
			addDefaultSerializers(converters);
			addDefaultDeserializers(converters);
			addDefaultSerializers(getDefaultSerializers());
			addDefaultDeserializers(getDefaultDeserializers());

			List<Factory<? extends Converter<?>>> convFactories = new ArrayList<Factory<? extends Converter<?>>>();
			addDefaultConverterFactories(convFactories);
			converterFactories.addAll(convFactories);

			List<Factory<? extends Serializer<?>>> serializerFactories = new ArrayList<Factory<? extends Serializer<?>>>();
			addDefaultSerializerFactories(serializerFactories);
			converterFactories.addAll(serializerFactories);

			List<Factory<? extends Deserializer<?>>> deserializerFactories = new ArrayList<Factory<? extends Deserializer<?>>>();
			addDefaultDeserializerFactories(deserializerFactories);
			converterFactories.addAll(deserializerFactories);

			return create(createConverterFactory(), withClassAliases);
		}

		private void addDefaultSerializers(List<? extends Serializer<?>> serializers) {
			if (serializers != null) {
				for (Serializer<?> serializer : serializers) {
					Type typeOfConverter = TypeUtil.typeOf(0,
							TypeUtil.lookupGenericType(Serializer.class, serializer.getClass()));
					typeOfConverter = TypeUtil.expandType(typeOfConverter, serializer.getClass());
					if (!serializersMap.containsKey(typeOfConverter))
						serializersMap.put(typeOfConverter, serializer);
				}
			}
		}

		private void addDefaultDeserializers(List<? extends Deserializer<?>> deserializers) {
			if (deserializers != null) {
				for (Deserializer<?> deserializer : deserializers) {
					Type typeOfConverter = TypeUtil
							.typeOf(0,
									TypeUtil.lookupGenericType(Deserializer.class,
											deserializer.getClass()));
					typeOfConverter = TypeUtil.expandType(typeOfConverter, deserializer.getClass());
					if (!deserializersMap.containsKey(typeOfConverter))
						deserializersMap.put(typeOfConverter, deserializer);
				}
			}
		}

		/**
		 * In theory this allows you to extend Genson class and to instantiate it, but actually you
		 * can not do it as Genson class is final. If some uses cases are discovered it may change.
		 * 
		 * @param converterFactory
		 * @param classAliases
		 * @return a new Genson instance.
		 */
		protected Genson create(Factory<Converter<?>> converterFactory,
				Map<String, Class<?>> classAliases) {
			return new Genson(converterFactory, getBeanDescriptorProvider(), getNullConverter(),
					isSkipNull(), isHtmlSafe(), classAliases, isWithClassMetadata(),
					isStrictDoubleParse(), getIndentation());
		}

		/**
		 * You should override this method if you want to add custom
		 * {@link com.owlike.genson.convert.ChainedFactory ChainedFactory} or if you need to chain
		 * them differently.
		 * 
		 * @return the converter <u>factory instance that will be used to resolve
		 *         <strong>ALL</strong> converters</u>.
		 */
		protected Factory<Converter<?>> createConverterFactory() {
			ChainedFactory chainHead = new CircularClassReferenceConverterFactory();
			ChainedFactory chainTail = chainHead;
			chainTail = chainTail.withNext(new NullConverter.NullConverterFactory()).withNext(
					new ClassMetadataConverter.ClassMetadataConverterFactory());
			if (isUseRuntimeTypeForSerialization())
				chainTail = chainTail
						.withNext(new RuntimeTypeConverter.RuntimeTypeConverterFactory());
			if (isWithBeanViewConverter())
				chainTail = chainTail.withNext(new BeanViewConverter.BeanViewConverterFactory(
						getBeanViewDescriptorProvider()));

			chainTail.withNext(new BasicConvertersFactory(getSerializersMap(),
					getDeserializersMap(), getFactories(), getBeanDescriptorProvider()));

			return chainHead;
		}

		protected BeanMutatorAccessorResolver createBeanMutatorAccessorResolver() {
			List<BeanMutatorAccessorResolver> resolvers = new ArrayList<BeanMutatorAccessorResolver>();
			VisibilityFilter propFilter = getFieldFilter();
			if (propFilter == null) propFilter = VisibilityFilter.PACKAGE_PUBLIC;
			VisibilityFilter methodFilter = getFieldFilter();
			if (methodFilter == null) methodFilter = VisibilityFilter.PACKAGE_PUBLIC;
			VisibilityFilter ctrFilter = getFieldFilter();
			if (ctrFilter == null) ctrFilter = VisibilityFilter.PACKAGE_PUBLIC;
			resolvers.add(new BeanMutatorAccessorResolver.StandardMutaAccessorResolver(propFilter,
					methodFilter, ctrFilter));
			return new BeanMutatorAccessorResolver.CompositeResolver(resolvers);
		}

		/**
		 * You can override this method if you want to change the
		 * {@link com.owlike.genson.reflect.PropertyNameResolver PropertyNameResolver} that are
		 * registered by default. You can also simply replace the default PropertyNameResolver by
		 * setting another one with {@link #set(PropertyNameResolver)}.
		 * 
		 * @return the property name resolver to be used. It should be an instance of
		 *         {@link com.owlike.genson.reflect.PropertyNameResolver.CompositePropertyNameResolver
		 *         PropertyNameResolver.CompositePropertyNameResolver}, otherwise you will not be
		 *         able to add others PropertyNameResolvers using
		 *         {@link #with(PropertyNameResolver...)} method.
		 */
		protected PropertyNameResolver createPropertyNameResolver() {
			List<PropertyNameResolver> resolvers = new ArrayList<PropertyNameResolver>();
			resolvers.add(new PropertyNameResolver.AnnotationPropertyNameResolver());
			resolvers.add(new PropertyNameResolver.ConventionalBeanPropertyNameResolver());
			if (isWithDebugInfoPropertyNameResolver())
				resolvers.add(new ASMCreatorParameterNameResolver(isThrowExceptionOnNoDebugInfo()));

			return new PropertyNameResolver.CompositePropertyNameResolver(resolvers);
		}

		/**
		 * You can override this methods if you want to change the default converters (remove some,
		 * change the order, etc).
		 * 
		 * @return the default converters list, must be not null.
		 */
		protected List<Converter<?>> getDefaultConverters() {
			List<Converter<?>> converters = new ArrayList<Converter<?>>();
			converters.add(DefaultConverters.StringConverter.instance);
			converters.add(DefaultConverters.BooleanConverter.instance);
			converters.add(DefaultConverters.IntegerConverter.instance);
			converters.add(DefaultConverters.DoubleConverter.instance);
			converters.add(DefaultConverters.LongConverter.instance);
			converters.add(DefaultConverters.NumberConverter.instance);
			converters.add(new DefaultConverters.DateConverter(getDateFormat()));
			converters.add(DefaultConverters.URLConverter.instance);
			converters.add(DefaultConverters.URIConverter.instance);
			converters.add(DefaultConverters.TimestampConverter.instance);
			converters.add(DefaultConverters.BigDecimalConverter.instance);
			converters.add(DefaultConverters.BigIntegerConverter.instance);
			return converters;
		}

		/**
		 * Override this method if you want to change the default converter factories.
		 * 
		 * @param factories
		 *            list, is not null.
		 */
		protected void addDefaultConverterFactories(List<Factory<? extends Converter<?>>> factories) {
			factories.add(DefaultConverters.ArrayConverterFactory.instance);
			factories.add(DefaultConverters.CollectionConverterFactory.instance);
			factories.add(DefaultConverters.MapConverterFactory.instance);
			factories.add(DefaultConverters.EnumConverterFactory.instance);
			factories.add(DefaultConverters.PrimitiveConverterFactory.instance);
			factories.add(DefaultConverters.UntypedConverterFactory.instance);
		}

		protected List<Serializer<?>> getDefaultSerializers() {
			return null;
		}

		protected void addDefaultSerializerFactories(
				List<Factory<? extends Serializer<?>>> serializerFactories) {
		}

		protected List<Deserializer<?>> getDefaultDeserializers() {
			return null;
		}

		protected void addDefaultDeserializerFactories(
				List<Factory<? extends Deserializer<?>>> deserializerFactories) {
		}

		/**
		 * Creates the standard BeanDescriptorProvider that will be used to provide
		 * {@link com.owlike.genson.reflect.BeanDescriptor BeanDescriptor} instances for
		 * serialization/deserialization of all types that couldn't be handled by standard and
		 * custom converters and converter factories.
		 * 
		 * @return the BeanDescriptorProvider instance.
		 */
		protected BeanDescriptorProvider createBeanDescriptorProvider() {
			return new BaseBeanDescriptorProvider(getMutatorAccessorResolver(),
					getPropertyNameResolver(), isUseGettersAndSetters(), isUseFields(), true);
		}

		protected final PropertyNameResolver getPropertyNameResolver() {
			return propertyNameResolver;
		}

		protected final BeanMutatorAccessorResolver getMutatorAccessorResolver() {
			return mutatorAccessorResolver;
		}

		protected final BeanDescriptorProvider getBeanDescriptorProvider() {
			return beanDescriptorProvider;
		}

		protected final BeanViewDescriptorProvider getBeanViewDescriptorProvider() {
			return beanViewDescriptorProvider;
		}

		public final List<Factory<?>> getFactories() {
			return Collections.unmodifiableList(converterFactories);
		}
	}
}
