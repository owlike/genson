package org.genson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.genson.convert.BasicConvertersFactory;
import org.genson.convert.BeanViewConverter;
import org.genson.convert.ChainedFactory;
import org.genson.convert.CircularClassReferenceConverterFactory;
import org.genson.convert.ClassMetadataConverter;
import org.genson.convert.NullConverter;
import org.genson.convert.DefaultConverters;
import org.genson.convert.RuntimeTypeConverter;
import org.genson.reflect.ASMCreatorParameterNameResolver;
import org.genson.reflect.BaseBeanDescriptorProvider;
import org.genson.reflect.BeanDescriptorProvider;
import org.genson.reflect.BeanMutatorAccessorResolver;
import org.genson.reflect.BeanViewDescriptorProvider;
import org.genson.reflect.PropertyNameResolver;
import org.genson.reflect.PropertyNameResolver.CompositePropertyNameResolver;
import org.genson.reflect.TypeUtil;
import org.genson.stream.JsonReader;
import org.genson.stream.JsonWriter;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

/**
 * <p>
 * Main class of the library. Instances of this class are intended to be reused. You can instantiate
 * it multiple times but it is better to have an instance per configuration so you can benefit of
 * better performances. This class is immutable and thread safe.
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
 * at {@link org.genson.convert.ClassMetadataConverter ClassMetadataConverter} for an explanation
 * and examples.
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

	/**
	 * The default constructor will use the default configuration provided by the {@link Builder}.
	 * In most cases using this default constructor will suffice.
	 */
	public Genson() {
		this(_default.converterFactory, _default.beanDescriptorFactory, _default.nullConverter,
				_default.skipNull, _default.htmlSafe, _default.aliasClassMap,
				_default.withClassMetadata);
	}

	/**
	 * 
	 * @param converterFactory
	 * @param beanDescProvider
	 * @param nullConverter
	 * @param skipNull
	 * @param htmlSafe
	 * @param classAliases
	 * @param withClassMetadata
	 */
	public Genson(Factory<Converter<?>> converterFactory, BeanDescriptorProvider beanDescProvider,
			Converter<Object> nullConverter, boolean skipNull, boolean htmlSafe,
			Map<String, Class<?>> classAliases, boolean withClassMetadata) {
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
	}

	@SuppressWarnings("unchecked")
	public <T> Converter<T> provideConverter(Type forType) {
		Converter<T> converter = (Converter<T>) converterCache.get(forType);
		if (converter == null) {
			converter = (Converter<T>) converterFactory.create(forType, this);
			if (converter == null)
				throw new TransformationRuntimeException("No converter found for type " + forType);
			converterCache.put(forType, converter);
		}
		return converter;
	}

	public <T> String serialize(T o) throws TransformationException, IOException {
		JsonWriter writer = new JsonWriter(new StringWriter(), skipNull, htmlSafe);
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, o.getClass(), writer, new Context(this));
		writer.flush();
		return writer.unwrap().toString();
	}
	
	public <T> String serialize(T o, GenericType<T> type) throws TransformationException, IOException {
		JsonWriter writer = new JsonWriter(new StringWriter(), skipNull, htmlSafe);
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, type.getType(), writer, new Context(this));
		writer.flush();
		return writer.unwrap().toString();
	}

	public <T> String serialize(T o, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		JsonWriter writer = new JsonWriter(new StringWriter(), skipNull, htmlSafe);
		if (o == null)
			nullConverter.serialize(null, writer, null);
		else
			serialize(o, o.getClass(), writer, new Context(this, Arrays.asList(withViews)));
		writer.flush();
		return writer.unwrap().toString();
	}

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

	public <T> T deserialize(String fromSource, Class<T> toClass) throws TransformationException,
			IOException {
		return deserialize(toClass, new JsonReader(new StringReader(fromSource)), new Context(this));
	}

	public <T> T deserialize(String fromSource, GenericType<T> toType)
			throws TransformationException, IOException {
		return deserialize(toType.getType(), new JsonReader(new StringReader(fromSource)),
				new Context(this));
	}

	public <T> T deserialize(Reader reader, Type toType) throws TransformationException,
			IOException {
		return deserialize(toType, new JsonReader(reader), new Context(this));
	}

	public <T> T deserialize(String fromSource, Type toType) throws TransformationException,
			IOException {
		StringReader reader = new StringReader(fromSource);
		return deserialize(toType, new JsonReader(reader), new Context(this, null));
	}

	public <T> T deserialize(String fromSource, Type toType,
			Class<? extends BeanView<?>>... withViews) throws TransformationException, IOException {
		StringReader reader = new StringReader(fromSource);
		return deserialize(toType, new JsonReader(reader),
				new Context(this, Arrays.asList(withViews)));
	}

	public <T> T deserialize(Type type, Reader reader, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		return deserialize(type, new JsonReader(reader),
				new Context(this, Arrays.asList(withViews)));
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
		return new JsonWriter(new OutputStreamWriter(os), skipNull, htmlSafe);
	}

	public ObjectReader createReader(InputStream is) {
		return new JsonReader(new InputStreamReader(is));
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

		private PropertyNameResolver propertyNameResolver;
		private BeanMutatorAccessorResolver mutatorAccessorResolver;
		private BeanDescriptorProvider beanDescriptorProvider;
		private Converter<Object> nullConverter;

		// for the moment we don't allow to override
		private BeanViewDescriptorProvider beanViewDescriptorProvider;

		private final Map<String, Class<?>> withClassAliases = new HashMap<String, Class<?>>();

		public Builder() {
		}

		/**
		 * Alias used in serialized class metadata instead of the full class name. See
		 * {@link org.genson.convert.ClassMetadataConverter ClassMetadataConverter} for more
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
		 * @param converter to register
		 * @param type of objects this converter handles
		 * @return a reference to this builder.
		 */
		public <T> Builder withConverter(Converter<T> converter, Class<? extends T> type) {
			registerConverter(converter, type);
			return this;
		}

		/**
		 * Register converter by mapping it to the parameterized type of type argument.
		 * 
		 * @param converter to register
		 * @param type of objects this converter handles
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
		 * @param factory to register
		 * @return a reference to this builder.
		 */
		public Builder withConverterFactory(Factory<? extends Converter<?>>... factory) {
			converterFactories.addAll(Arrays.asList(factory));
			return this;
		}

		/**
		 * Registers serializer factories.
		 * 
		 * @param factory to register
		 * @return a reference to this builder.
		 */
		public Builder withSerializerFactory(Factory<? extends Serializer<?>>... factory) {
			converterFactories.addAll(Arrays.asList(factory));
			return this;
		}

		/**
		 * Registers deserializer factories.
		 * 
		 * @param factory to register
		 * @return a reference to this builder.
		 */
		public Builder withDeserializerFactory(Factory<? extends Deserializer<?>>... factory) {
			converterFactories.addAll(Arrays.asList(factory));
			return this;
		}

		/**
		 * If true will not serialize null values
		 * 
		 * @param skipNull indicates whether null values should be serialized or not.
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
		 * @param htmlSafe indicates whether serialized data should be html safe.
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
		 * Replaces default {@link org.genson.reflect.BeanMutatorAccessorResolver
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
		 * Replaces default {@link org.genson.reflect.PropertyNameResolver PropertyNameResolver} by
		 * the specified one.
		 * 
		 * @param resolver
		 * @return a reference to this builder.
		 */
		public Builder set(PropertyNameResolver resolver) {
			propertyNameResolver = resolver;
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
			if (propertyNameResolver == null)
				propertyNameResolver = createPropertyNameResolver();
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
		 * @see org.genson.convert.ClassMetadataConverter ClassMetadataConverter
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
		 * @see org.genson.reflect.ASMCreatorParameterNameResolver ASMCreatorParameterNameResolver
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
		 * look at {@link org.genson.reflect.ASMCreatorParameterNameResolver
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

		public Map<Type, Serializer<?>> getSerializersMap() {
			return Collections.unmodifiableMap(serializersMap);
		}

		public Map<Type, Deserializer<?>> getDeserializersMap() {
			return Collections.unmodifiableMap(deserializersMap);
		}

		/**
		 * Creates an instance of Genson. You may use this method as many times you want. It wont
		 * change the state of the builder, in sense that the returned instance will have always the
		 * same configuration.
		 * 
		 * @return a new instance of Genson built for the current configuration.
		 */
		public Genson create() {
			if (nullConverter == null)
				nullConverter = new NullConverter();
			if (propertyNameResolver == null)
				propertyNameResolver = createPropertyNameResolver();
			if (mutatorAccessorResolver == null)
				mutatorAccessorResolver = createBeanMutatorAccessorResolver();

			beanDescriptorProvider = createBeanDescriptorProvider();

			if (withBeanViewConverter) {
				beanViewDescriptorProvider = new BeanViewDescriptorProvider(
						new BeanViewDescriptorProvider.BeanViewMutatorAccessorResolver(),
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
					isSkipNull(), isHtmlSafe(), classAliases, isWithClassMetadata());
		}

		/**
		 * You should override this method if you want to add custom
		 * {@link org.genson.convert.ChainedFactory ChainedFactory} or if you need to chain them
		 * differently.
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
				chainTail = chainTail.withNext(RuntimeTypeConverter.runtimeTypeConverterFactory);
			if (isWithBeanViewConverter())
				chainTail = chainTail.withNext(new BeanViewConverter.BeanViewConverterFactory(
						getBeanViewDescriptorProvider()));

			chainTail.withNext(new BasicConvertersFactory(getSerializersMap(),
					getDeserializersMap(), getFactories(), getBeanDescriptorProvider()));

			return chainHead;
		}

		protected BeanMutatorAccessorResolver createBeanMutatorAccessorResolver() {
			return new BeanMutatorAccessorResolver.StandardMutaAccessorResolver();
		}

		/**
		 * You can override this method if you want to change the
		 * {@link org.genson.reflect.PropertyNameResolver PropertyNameResolver} that are registered
		 * by default. You can also simply replace the default PropertyNameResolver by setting
		 * another one with {@link #set(PropertyNameResolver)}.
		 * 
		 * @return the property name resolver to be used. It should be an instance of
		 *         {@link org.genson.reflect.PropertyNameResolver.CompositePropertyNameResolver
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
			return converters;
		}

		/**
		 * Override this method if you want to change the default converter factories.
		 * 
		 * @param factories list, is not null.
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
		 * {@link org.genson.reflect.BeanDescriptor BeanDescriptor} instances for
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
			return converterFactories;
		}
	}
}
