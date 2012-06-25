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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genson.deserialization.BeanDeserializer;
import org.genson.deserialization.BeanViewDeserializer;
import org.genson.deserialization.ChainedDeserializer;
import org.genson.deserialization.DefaultDeserializers;
import org.genson.deserialization.Deserializer;
import org.genson.deserialization.DeserializerProvider;
import org.genson.deserialization.DefaultDeserializers.ArrayDeserializerFactory;
import org.genson.deserialization.DefaultDeserializers.BooleanDeserializerFactory;
import org.genson.deserialization.DefaultDeserializers.CollectionDeserializer;
import org.genson.deserialization.DefaultDeserializers.MapDeserializer;
import org.genson.deserialization.DefaultDeserializers.NumberDeserializerFactory;
import org.genson.deserialization.DefaultDeserializers.UntypedDeserializerFactory;
import org.genson.reflect.ASMCreatorParameterNameResolver;
import org.genson.reflect.BaseBeanDescriptorProvider;
import org.genson.reflect.BeanDescriptorProvider;
import org.genson.reflect.BeanMutatorAccessorResolver;
import org.genson.reflect.BeanViewDescriptorProvider;
import org.genson.reflect.PropertyNameResolver;
import org.genson.reflect.PropertyNameResolver.CompositePropertyNameResolver;
import org.genson.serialization.BeanSerializer;
import org.genson.serialization.BeanViewSerializer;
import org.genson.serialization.ChainedSerializer;
import org.genson.serialization.DefaultSerializers;
import org.genson.serialization.Serializer;
import org.genson.serialization.SerializerProvider;
import org.genson.stream.JsonReader;
import org.genson.stream.JsonWriter;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;
import org.genson.stream.ValueType;


/**
 * <p>
 * Main class of the library. Instances of this class are intended to be reused. You can instantiate
 * it multiple times but it is better to have an instance per configuration so you can benefit of
 * better performances. This class is immutable and thread safe.
 * 
 * <p>
 * To create a new instance you can use the default constructor {@link #Genson} or the
 * {@link Builder} class to have control over its configuration. <br>
 * For example, if you want to register a custom Serializer : <br>
 * <pre>Genson genson = new Genson.Builder().with(mySerializer, anotherSerializer).create();
 * </pre>
 * Use the {@link #serialize} and {@link #deserialize} methods to convert java to json and json to
 * java. The Serializers and Deserializers take as an argument an instance of {@link Context} class,
 * this object will be passed through all the chain and is statefull, so you can store inside some
 */
public final class Genson {
	/**
	 * Default genson configuration, the default configuration (sers, desers, etc)
	 * will be shared accros all default Genson instances.
	 */
	private final static Genson _default = new Builder().create();

	private final SerializerProvider serializerProvider;
	private final DeserializerProvider deserializerProvider;
	private final BeanDescriptorProvider beanDescriptorProvider;
	private final Map<Class<?>, String> classAliasMap;
	private final Map<String, Class<?>> aliasClassMap;

	private boolean skipNull;
	private boolean htmlSafe;
	private boolean withClassMetadata;

	/**
	 * The default constructor will use the default configuration provided by the {@link Builder}.
	 * In most cases using this default constructor will suffice.
	 */
	public Genson() {
		this(_default.serializerProvider, _default.deserializerProvider, _default.beanDescriptorProvider, _default.skipNull,
				_default.htmlSafe, _default.aliasClassMap, _default.withClassMetadata);
	}

	/**
	 * The configurable constructor, the {@link Builder} should be used instead of this constructor directly.
	 * @param serializerProvider
	 * @param deserializerProvider
	 * @param skipNull
	 * @param htmlSafe
	 * @param classAliases
	 * @param withClassMetadata indicates wether class information should be written/readen to/from json.
	 * If true the deserializers will try to use it to determine the type of the object being deserialized.
	 */
	public Genson(SerializerProvider serializerProvider,
			DeserializerProvider deserializerProvider, BeanDescriptorProvider beanDescriptorProvider, boolean skipNull, boolean htmlSafe,
			Map<String, Class<?>> classAliases, boolean withClassMetadata) {
		this.serializerProvider = serializerProvider;
		this.deserializerProvider = deserializerProvider;
		this.beanDescriptorProvider = beanDescriptorProvider;
		this.skipNull = skipNull;
		this.htmlSafe = htmlSafe;
		this.aliasClassMap = classAliases;
		this.withClassMetadata = withClassMetadata;
		this.classAliasMap = new HashMap<Class<?>, String>(classAliases.size());
		for (Map.Entry<String, Class<?>> entry : classAliases.entrySet()) {
			classAliasMap.put(entry.getValue(), entry.getKey());
		}
	}

	public <T> String serialize(T o) throws TransformationException, IOException {
		JsonWriter writer = new JsonWriter(new StringWriter(), skipNull, htmlSafe);

		serialize(o, o.getClass(), writer, new Context(this));
		writer.flush();
		return writer.unwrap().toString();
	}

	public <T> String serialize(T o, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		JsonWriter writer = new JsonWriter(new StringWriter(), skipNull, htmlSafe);
		serialize(o, o.getClass(), writer,
				new Context(this, Arrays.asList(withViews)));
		writer.flush();
		return writer.unwrap().toString();
	}

	public <T> void serialize(T o, ObjectWriter writer, Class<? extends BeanView<?>>... withViews)
			throws TransformationException, IOException {
		writer.flush();
		serialize(o, o.getClass(), writer, new Context(this, Arrays.asList(withViews)));
	}

	public <T> void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		if (obj != null) {
			if (String.class.equals(type) )
				writer.writeValue((String) obj);
			else {
				Serializer<T> ser = serializerProvider.provide(type);
				ser.serialize(obj, type, writer, ctx);
			}
		} else
			writer.writeNull();
	}

	public <T> T deserialize(String fromSource, Class<T> toClass) throws TransformationException, IOException {
		return deserialize(toClass, new JsonReader(new StringReader(fromSource)), new Context(this));
	}

	public <T> T deserialize(String fromSource, GenericType<T> toType) throws TransformationException, IOException {
		return deserialize(toType.getType(), new JsonReader(new StringReader(fromSource)), new Context(this));
	}
	
	public <T> T deserialize(Reader reader, Type toType) throws TransformationException, IOException {
		return deserialize(toType, new JsonReader(reader), new Context(this));
	}
	
	public <T> T deserialize(String fromSource, Type toType) throws TransformationException, IOException {
		StringReader reader = new StringReader(fromSource);
		return deserialize(toType, new JsonReader(reader),  new Context(this, null));
	}

	public <T> T deserialize(String fromSource, Type toType, Class<? extends BeanView<?>>... withViews) throws TransformationException, IOException {
		StringReader reader = new StringReader(fromSource);
		return deserialize(toType, new JsonReader(reader),  new Context(this, Arrays.asList(withViews)));
	}
	
	public <T> T deserialize(Type type, Reader reader, Class<? extends BeanView<?>>... withViews) throws TransformationException, IOException {
		return deserialize(type, new JsonReader(reader), new Context(this, Arrays.asList(withViews)));
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		if (!ValueType.NULL.equals(reader.getValueType())) {
			if ( String.class.equals(type) ) 
				return (T) reader.valueAsString();
			else {
				if (isWithClassMetadata() && ValueType.OBJECT.equals(reader.getValueType())) {
    				String className = reader.nextObjectMetadata().metadata("class");
    				if (className != null) {
    					try {
        					type = classFor(className);
    					} catch (ClassNotFoundException e) {
    						throw new TransformationException("Could not use @class metadata, no such class: "
    								+ className);
    					}
    				}
				}
    			Deserializer<T> deser = deserializerProvider.provide(type);
    			return deser.deserialize(type, reader, ctx);
			}
		} else return (T) handleNull(type);
	}

	protected Object handleNull(Type type) {
		if (!(type instanceof Class))
			return null;
		Class<?> clazz = (Class<?>) type;
		if(clazz.isPrimitive()) {
			if (clazz == int.class) return 0;
			if (clazz == double.class) return 0d;
			if (clazz == boolean.class) return false;
			if (clazz == long.class) return 0l;
			if (clazz == float.class) return 0f;
			if (clazz == short.class) return 0;
		}
		// its an object
		return null;
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
	
	public SerializerProvider getSerializerProvider() {
		return serializerProvider;
	}

	public DeserializerProvider getDeserializerProvider() {
		return deserializerProvider;
	}

	public BeanDescriptorProvider getBeanDescriptorProvider() {
		return beanDescriptorProvider;
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
	
	/**
	 * Builder en interface Builder<T extends Builder<T>> pour pouvoir etre sous classe
	 */
	public static class Builder {
		private final List<Serializer<?>> serializers = new ArrayList<Serializer<?>>();
		private final List<Factory<? extends Serializer<?>>> serializerFactories = new ArrayList<Factory<? extends Serializer<?>>>();;
		private ChainedSerializer dynaSerializer;
		private boolean skipNull = false;
		private boolean htmlSafe = false;
		private boolean withClassMetadata = false;
		private DateFormat dateFormat = null;
		private boolean throwExcOnNoDebugInfo = true;
		private boolean useGettersAndSetters = true;
		private boolean useFields = true;

		private final List<Deserializer<?>> deserializers = new ArrayList<Deserializer<?>>();
		private final List<Factory<? extends Deserializer<?>>> deserializerFactories = new ArrayList<Factory<? extends Deserializer<?>>>();
		private ChainedDeserializer dynaDeserializer;

		private PropertyNameResolver propertyNameResolver;
		private BeanMutatorAccessorResolver mutatorAccessorResolver;
		private BeanDescriptorProvider beanDescriptorProvider;

		// for the moment we don't allow to override
		private BeanViewDescriptorProvider beanViewDescriptorProvider;
		
		private final Map<String, Class<?>> withClassAliases = new HashMap<String, Class<?>>();

		public Builder() {

		}

		public Builder addAlias(String alias, Class<?> forClass) {
			 withClassMetadata = true;
    		 withClassAliases.put(alias, forClass);
    		 return this;
		 }

		public Builder with(Converter<?>... converter) {
			List<Converter<?>> converters =  Arrays.asList(converter);
			serializers.addAll(converters);
			deserializers.addAll(converters);
			return this;
		}
		
		public Builder with(Serializer<?>... serializer) {
			serializers.addAll(Arrays.asList(serializer));
			return this;
		}

		public Builder withSerializerFactory(Factory<? extends Serializer<?>>... factory) {
			serializerFactories.addAll(Arrays.asList(factory));
			return this;
		}

		public Builder set(ChainedSerializer serializer) {
			dynaSerializer = serializer;
			return this;
		}

		public Builder setSkipNull(boolean skipNull) {
			this.skipNull = skipNull;
			return this;
		}

		public Builder setHtmlSafe(boolean htmlSafe) {
			this.htmlSafe = htmlSafe;
			return this;
		}

		public Builder with(Deserializer<?>... deserializer) {
			deserializers.addAll(Arrays.asList(deserializer));
			return this;
		}

		public Builder withDeserializerFactory(Factory<? extends Deserializer<?>>... factory) {
			deserializerFactories.addAll(Arrays.asList(factory));
			return this;
		}

		public Builder set(ChainedDeserializer deserializer) {
			dynaDeserializer = deserializer;
			return this;
		}

		public Builder set(BeanDescriptorProvider provider) {
			this.beanDescriptorProvider = provider;
			return this;
		}

		public Builder set(BeanMutatorAccessorResolver resolver) {
			mutatorAccessorResolver = resolver;
			return this;
		}

		public Builder set(PropertyNameResolver resolver) {
			propertyNameResolver = resolver;
			return this;
		}

		public Builder with(PropertyNameResolver[] resolvers) {
			if (propertyNameResolver == null)
				propertyNameResolver = createPropertyNameResolver();
			if (propertyNameResolver instanceof CompositePropertyNameResolver)
				((CompositePropertyNameResolver) propertyNameResolver).add(resolvers);
			else
				throw new IllegalStateException(
						"You can not add multiple resolvers if the base resolver if not of type "
								+ CompositePropertyNameResolver.class.getName());
			return this;
		}
		
		public boolean isWithClassMetadata() {
			return withClassMetadata;
		}

		public Builder setWithClassMetadata(boolean withClassMetadata) {
			this.withClassMetadata = withClassMetadata;
			return this;
		}

		public DateFormat getDateFormat() {
			return dateFormat;
		}

		public Builder setDateFormat(DateFormat dateFormat) {
			this.dateFormat = dateFormat;
			return this;
		}

		public boolean isThrowExcOnNoDebugInfo() {
			return throwExcOnNoDebugInfo;
		}

		public Builder setThrowExcOnNoDebugInfo(boolean throwExcOnNoDebugInfo) {
			this.throwExcOnNoDebugInfo = throwExcOnNoDebugInfo;
			return this;
		}

		public boolean isUseGettersAndSetters() {
			return useGettersAndSetters;
		}

		public Builder setUseGettersAndSetters(boolean useGettersAndSetters) {
			this.useGettersAndSetters = useGettersAndSetters;
			return this;
		}

		public boolean isUseFields() {
			return useFields;
		}

		public Builder setUseFields(boolean useFields) {
			this.useFields = useFields;
			return this;
		}

		public Genson create() {
			if (propertyNameResolver == null)
				propertyNameResolver = createPropertyNameResolver();
			if (mutatorAccessorResolver == null)
				mutatorAccessorResolver = createBeanMutatorAccessorResolver();

			if (beanDescriptorProvider == null)
				beanDescriptorProvider = createBeanDescriptorProvider();

			beanViewDescriptorProvider = new BeanViewDescriptorProvider(new BeanViewDescriptorProvider.BeanViewMutatorAccessorResolver(), getPropertyNameResolver());
			
			addDefaultSerializers(serializers);
			addDefaultSerializerFactories(serializerFactories);
			if (dynaSerializer != null)
				dynaSerializer.withNext(createDefaultDynamicSerializer());
			else
				dynaSerializer = createDefaultDynamicSerializer();

			addDefaultDeserializers(deserializers);
			addDefaultDeserializerFactories(deserializerFactories);

			if (dynaDeserializer != null)
				dynaDeserializer.withNext(createDefaultDynamicDeserializer());
			else
				dynaDeserializer = createDefaultDynamicDeserializer();

			return create(
					createSerializerProvider(serializers, serializerFactories, dynaSerializer),
					createDeserializerProvider(deserializers, deserializerFactories,
							dynaDeserializer), skipNull, htmlSafe, withClassAliases, withClassMetadata);
		}

		protected Genson create(SerializerProvider serializerProvider,
				DeserializerProvider deserializerProvider, boolean skipNull, boolean htmlSafe,
				Map<String, Class<?>> classAliases, boolean withClassMetadata) {
			return new Genson(serializerProvider, deserializerProvider, beanDescriptorProvider, skipNull, htmlSafe,
					classAliases, withClassMetadata);
		}

		protected BeanMutatorAccessorResolver createBeanMutatorAccessorResolver() {
			return new BeanMutatorAccessorResolver.ConventionalBeanResolver();
		}

		protected PropertyNameResolver createPropertyNameResolver() {
			List<PropertyNameResolver> resolvers = new ArrayList<PropertyNameResolver>();
			resolvers.add(new PropertyNameResolver.AnnotationPropertyNameResolver());
			resolvers.add(new PropertyNameResolver.ConventionalBeanPropertyNameResolver());
			resolvers.add(new ASMCreatorParameterNameResolver(isThrowExcOnNoDebugInfo()));

			return new PropertyNameResolver.CompositePropertyNameResolver(resolvers);
		}

		protected void addDefaultSerializers(List<Serializer<?>> serializers) {
			serializers.add(new DefaultSerializers.CollectionSerializer());
			serializers.add(new DefaultSerializers.MapSerializer());
			if (getDateFormat() != null) serializers.add(new DefaultSerializers.DateSerializer(getDateFormat()));
			else serializers.add(new DefaultSerializers.DateSerializer());
		}

		protected void addDefaultSerializerFactories(
				List<Factory<? extends Serializer<?>>> serializerFactories) {
			serializerFactories.add(new DefaultSerializers.ArraySerializerFactory());
			serializerFactories.add(new DefaultSerializers.NumberSerializerFactory());
			serializerFactories.add(new DefaultSerializers.BooleanSerializerFactory());
		}

		protected ChainedSerializer createDefaultDynamicSerializer() {
			return new BeanViewSerializer(getBeanViewDescriptorProvider(), new BeanSerializer(getBeanDescriptorProvider()));
		}

		protected SerializerProvider createSerializerProvider(List<Serializer<?>> serializers,
				List<Factory<? extends Serializer<?>>> serializerFactories,
				Serializer<?> dynamicSerializer) {
			return new SerializerProvider.BaseSerializerProvider(serializers, serializerFactories, dynamicSerializer);
		}

		protected void addDefaultDeserializers(List<Deserializer<?>> deserializers) {
			deserializers.add(new CollectionDeserializer());
			deserializers.add(new MapDeserializer());
			if(getDateFormat() != null) deserializers.add(new DefaultDeserializers.DateDeserializer(getDateFormat()));
			else deserializers.add(new DefaultDeserializers.DateDeserializer());
		}

		protected void addDefaultDeserializerFactories(
				List<Factory<? extends Deserializer<?>>> deserializerFactories) {
			deserializerFactories.add(new NumberDeserializerFactory());
			deserializerFactories.add(new BooleanDeserializerFactory());
			deserializerFactories.add(new ArrayDeserializerFactory());
			deserializerFactories.add(new UntypedDeserializerFactory());
		}

		protected ChainedDeserializer createDefaultDynamicDeserializer() {
			return new BeanViewDeserializer(getBeanViewDescriptorProvider(), new BeanDeserializer(getBeanDescriptorProvider()));
		}

		protected DeserializerProvider createDeserializerProvider(
				List<Deserializer<?>> deserializers,
				List<Factory<? extends Deserializer<?>>> deserializerFactories,
				Deserializer<?> dynamicDeserializer) {
			return new DeserializerProvider.BaseDeserializerProvider(deserializers, deserializerFactories,
					dynamicDeserializer);
		}

		protected BeanDescriptorProvider createBeanDescriptorProvider() {
			return new BeanDescriptorProvider.CompositeBeanDescriptorProvider(Arrays.asList(new BaseBeanDescriptorProvider(
					getMutatorAccessorResolver(), getPropertyNameResolver(), isUseGettersAndSetters(), isUseFields())));
		}

		protected PropertyNameResolver getPropertyNameResolver() {
			return propertyNameResolver;
		}

		protected BeanMutatorAccessorResolver getMutatorAccessorResolver() {
			return mutatorAccessorResolver;
		}

		protected BeanDescriptorProvider getBeanDescriptorProvider() {
			return beanDescriptorProvider;
		}
		
		protected BeanViewDescriptorProvider getBeanViewDescriptorProvider() {
			return beanViewDescriptorProvider;
		}
	}
}
