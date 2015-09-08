package com.owlike.genson;

import com.owlike.genson.convert.*;
import com.owlike.genson.ext.GensonBundle;
import com.owlike.genson.reflect.*;
import com.owlike.genson.reflect.BeanDescriptorProvider.CompositeBeanDescriptorProvider;
import com.owlike.genson.reflect.AbstractBeanDescriptorProvider.ContextualFactoryDecorator;
import com.owlike.genson.reflect.AbstractBeanDescriptorProvider.ContextualConverterFactory;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Use the GensonBuilder class when you want to create a custom Genson instance. This class allows you
 * for example to register custom converters/serializers/deserializers
 * {@link #withConverters(Converter...)} or custom converter Factories
 * {@link #withConverterFactory(Factory)}.
 * <p/>
 * This class combines the GensonBuilder design pattern with template pattern providing handy
 * configuration and extensibility. All its public methods are intended to be used in the
 * GensonBuilder "style" and its protected methods are part of the template. When you call
 * {@link #create()} method, it will start assembling all the configuration and build all the
 * required components by using the protected methods. For example if you wish to use in your
 * projects a GensonBuilder that will always create some custom
 * {@link com.owlike.genson.reflect.BeanDescriptorProvider BeanDescriptorProvider} you have to
 * extend {@link #createBeanDescriptorProvider()}, or imagine that you implemented some
 * Converters that you always want to register then override {@link #getDefaultConverters()}.
 *
 * @author eugen
 */
public class GensonBuilder {
  private final Map<Type, Serializer<?>> serializersMap = new HashMap<Type, Serializer<?>>();
  private final Map<Type, Deserializer<?>> deserializersMap = new HashMap<Type, Deserializer<?>>();
  private final List<Factory<?>> converterFactories = new ArrayList<Factory<?>>();
  private final List<ContextualFactory<?>> contextualFactories = new ArrayList<ContextualFactory<?>>();
  private final List<BeanPropertyFactory> beanPropertyFactories = new ArrayList<BeanPropertyFactory>();

  private boolean skipNull = false;
  private boolean htmlSafe = false;
  private boolean withClassMetadata = false;
  private boolean throwExcOnNoDebugInfo = false;
  private boolean useGettersAndSetters = true;
  private boolean useFields = true;
  private boolean withBeanViewConverter = false;
  private boolean useRuntimeTypeForSerialization = false;
  private boolean withDebugInfoPropertyNameResolver = false;
  private boolean strictDoubleParse = false;
  private boolean indent = false;
  private boolean metadata = false;
  private boolean failOnMissingProperty = false;
  private boolean permissiveParsing = false;

  private List<GensonBundle> _bundles = new ArrayList<GensonBundle>();

  private PropertyNameResolver propertyNameResolver;
  private BeanMutatorAccessorResolver mutatorAccessorResolver;
  private VisibilityFilter propertyFilter = VisibilityFilter.PACKAGE_PUBLIC;
  private VisibilityFilter methodFilter = VisibilityFilter.PACKAGE_PUBLIC;
  private VisibilityFilter constructorFilter = VisibilityFilter.PACKAGE_PUBLIC;

  private ClassLoader classLoader = null;
  private BeanDescriptorProvider beanDescriptorProvider;
  private Converter<Object> nullConverter;
  private DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
  private boolean useDateAsTimestamp = true;
  private boolean classMetadataWithStaticType = true;

  // for the moment we don't allow to override
  private BeanViewDescriptorProvider beanViewDescriptorProvider;

  private final Map<String, Class<?>> withClassAliases = new HashMap<String, Class<?>>();
  private final Map<Class<?>, BeanView<?>> registeredViews = new HashMap<Class<?>, BeanView<?>>();

  private ChainedFactory customFactoryChain;

  public GensonBuilder() {
  }

  /**
   * Alias used in serialized class metadata instead of the full class name. See
   * {@link com.owlike.genson.convert.ClassMetadataConverter ClassMetadataConverter} for more
   * metadata. If you add an alias, it will automatically enable the class metadata feature,
   * as if you used {@link #useClassMetadata(boolean)}.
   *
   * @param alias
   * @param forClass
   * @return a reference to this builder.
   */
  public GensonBuilder addAlias(String alias, Class<?> forClass) {
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
  public GensonBuilder withConverters(Converter<?>... converter) {
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
   * @param type      of objects this converter handles
   * @return a reference to this builder.
   */
  public <T> GensonBuilder withConverter(Converter<T> converter, Class<? extends T> type) {
    registerConverter(converter, type);
    return this;
  }

  /**
   * Register converter by mapping it to the parameterized type of type argument.
   *
   * @param converter to register
   * @param type      of objects this converter handles
   * @return a reference to this builder.
   */
  public <T> GensonBuilder withConverter(Converter<T> converter, GenericType<? extends T> type) {
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

  public GensonBuilder withSerializers(Serializer<?>... serializer) {
    for (Serializer<?> s : serializer) {
      Type typeOfConverter = TypeUtil.typeOf(0,
        TypeUtil.lookupGenericType(Serializer.class, s.getClass()));
      typeOfConverter = TypeUtil.expandType(typeOfConverter, s.getClass());
      registerSerializer(s, typeOfConverter);
    }
    return this;
  }

  public <T> GensonBuilder withSerializer(Serializer<T> serializer, Class<? extends T> type) {
    registerSerializer(serializer, type);
    return this;
  }

  public <T> GensonBuilder withSerializer(Serializer<T> serializer, GenericType<? extends T> type) {
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

  public GensonBuilder withDeserializers(Deserializer<?>... deserializer) {
    for (Deserializer<?> d : deserializer) {
      Type typeOfConverter = TypeUtil.typeOf(0,
        TypeUtil.lookupGenericType(Deserializer.class, d.getClass()));
      typeOfConverter = TypeUtil.expandType(typeOfConverter, d.getClass());
      registerDeserializer(d, typeOfConverter);
    }
    return this;
  }

  public <T> GensonBuilder withDeserializer(Deserializer<T> deserializer, Class<? extends T> type) {
    registerDeserializer(deserializer, type);
    return this;
  }

  public <T> GensonBuilder withDeserializer(Deserializer<T> deserializer,
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
  public GensonBuilder withConverterFactory(Factory<? extends Converter<?>> factory) {
    converterFactories.add(factory);
    return this;
  }

  /**
   * Registers serializer factories.
   *
   * @param factory to register
   * @return a reference to this builder.
   */
  public GensonBuilder withSerializerFactory(Factory<? extends Serializer<?>> factory) {
    converterFactories.add(factory);
    return this;
  }

  /**
   * Registers deserializer factories.
   *
   * @param factory to register
   * @return a reference to this builder.
   */
  public GensonBuilder withDeserializerFactory(Factory<? extends Deserializer<?>> factory) {
    converterFactories.add(factory);
    return this;
  }

  /**
   * ContextualFactory is actually in a beta status, it will not be removed, but might be
   * refactored.
   */
  public GensonBuilder withContextualFactory(ContextualFactory<?>... factories) {
    contextualFactories.addAll(Arrays.asList(factories));
    return this;
  }

  /**
   * A ChainedFactory provides a way to use custom Converters that have access to the default Converters.
   * An example of use is to wrap incoming/outgoing json in a root object and delegate then the ser/de to the default
   * Converter.
   *
   * This mechanism is internally used by Genson to decorate the different Converters with additional behaviour
   * (null handling, ser/de of polymorphic types with class info, runtime type based ser/de, etc).
   *
   * Note that you can't use it in situations where you want to start reading/writing some partial infos and want to
   * delegate the rest to the default Converter.
   */
  public GensonBuilder withConverterFactory(ChainedFactory chainedFactory) {
    if (customFactoryChain == null) customFactoryChain = chainedFactory;
    else {
      customFactoryChain.append(chainedFactory);
    }
    return this;
  }

  /**
   * Allows you to register new BeanPropertyFactory responsible of creating BeanProperty
   * accessors, mutators and BeanCreators. This is a very low level feature, you probably
   * don't need it.
   */
  public GensonBuilder withBeanPropertyFactory(BeanPropertyFactory... factories) {
    beanPropertyFactories.addAll(Arrays.asList(factories));
    return this;
  }

  /**
   * Register some genson bundles. For example to enable JAXB support:
   * <p/>
   * <pre>
   * builder.withBundle(new JAXBExtension());
   * </pre>
   *
   * <b>All bundles should be registered before any other customization.</b>
   *
   * @see GensonBundle
   */
  public GensonBuilder withBundle(GensonBundle... bundles) {
    for (GensonBundle bundle : bundles) {
      bundle.configure(this);
      _bundles.add(bundle);
    }
    return this;
  }

  /**
   * Override the default classloader
   *
   * @param loader classloader which will be used to load classes while deserializing
   * @return a reference to this builder
   */
  public GensonBuilder withClassLoader(ClassLoader loader) {
    classLoader = loader;
    return this;
  }


  /**
   * Replaces default {@link com.owlike.genson.reflect.BeanMutatorAccessorResolver
   * BeanMutatorAccessorResolver} by the specified one.
   *
   * @param resolver
   * @return a reference to this builder.
   */
  public GensonBuilder set(BeanMutatorAccessorResolver resolver) {
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
  public GensonBuilder set(PropertyNameResolver resolver) {
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
  public GensonBuilder with(BeanMutatorAccessorResolver... resolvers) {
    if (mutatorAccessorResolver == null)
      mutatorAccessorResolver = createBeanMutatorAccessorResolver();
    if (mutatorAccessorResolver instanceof BeanMutatorAccessorResolver.CompositeResolver)
      ((BeanMutatorAccessorResolver.CompositeResolver) mutatorAccessorResolver).add(resolvers);
    else
      throw new IllegalStateException(
        "You can not add multiple resolvers if the base resolver is not of type "
          + BeanMutatorAccessorResolver.CompositeResolver.class.getName());
    return this;
  }

  public Map<Type, Serializer<?>> getSerializersMap() {
    return Collections.unmodifiableMap(serializersMap);
  }

  public Map<Type, Deserializer<?>> getDeserializersMap() {
    return Collections.unmodifiableMap(deserializersMap);
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  /**
   * Registers the specified resolvers in the order they were defined and before the standard
   * ones.
   *
   * @param resolvers
   * @return a reference to this builder.
   */
  public GensonBuilder with(PropertyNameResolver... resolvers) {
    if (propertyNameResolver == null) propertyNameResolver = createPropertyNameResolver();
    if (propertyNameResolver instanceof PropertyNameResolver.CompositePropertyNameResolver)
      ((PropertyNameResolver.CompositePropertyNameResolver) propertyNameResolver).add(resolvers);
    else
      throw new IllegalStateException(
        "You can not add multiple resolvers if the base resolver is not of type "
          + PropertyNameResolver.CompositePropertyNameResolver.class.getName());
    return this;
  }

  /**
   * Renames all fields named field to toName.
   */
  public GensonBuilder rename(String field, String toName) {
    return rename(field, null, toName, null);
  }

  /**
   * Renames all fields of type fieldOfType to toName.
   */
  public GensonBuilder rename(Class<?> fieldOfType, String toName) {
    return rename(null, null, toName, fieldOfType);
  }

  /**
   * Renames all fields named field declared in class fromClass to toName.
   */
  public GensonBuilder rename(String field, Class<?> fromClass, String toName) {
    return rename(field, fromClass, toName, null);
  }

  /**
   * Renames all fields named field and of type fieldOfType to toName.
   */
  public GensonBuilder rename(String field, String toName, Class<?> fieldOfType) {
    return rename(field, null, toName, fieldOfType);
  }

  /**
   * Renames all fields named field, of type fieldOfType and declared in fromClass to toName.
   */
  public GensonBuilder rename(final String field, final Class<?> fromClass, final String toName,
                              final Class<?> ofType) {
    return with(new RenamingPropertyNameResolver(field, fromClass, ofType, toName));
  }

  public GensonBuilder exclude(String field) {
    return filter(field, null, null, true);
  }

  public GensonBuilder exclude(Class<?> fieldOfType) {
    return filter(null, null, fieldOfType, true);
  }

  public GensonBuilder exclude(String field, Class<?> fromClass) {
    return filter(field, fromClass, null, true);
  }

  public GensonBuilder exclude(String field, Class<?> fromClass, Class<?> ofType) {
    return filter(field, fromClass, ofType, true);
  }

  public GensonBuilder include(String field) {
    return filter(field, null, null, false);
  }

  public GensonBuilder include(Class<?> fieldOfType) {
    return filter(null, null, fieldOfType, false);
  }

  public GensonBuilder include(String field, Class<?> fromClass) {
    return filter(field, fromClass, null, false);
  }

  public GensonBuilder include(String field, Class<?> fromClass, Class<?> ofType) {
    return filter(field, fromClass, ofType, false);
  }

  private GensonBuilder filter(final String field, final Class<?> declaringClass,
                               final Class<?> ofType, final boolean exclude) {
    return with(new PropertyFilter(exclude, field, declaringClass, ofType));
  }

  /**
   * If true will not serialize null values
   *
   * @param skipNull indicates whether null values should be serialized or not.
   * @return a reference to this builder.
   */
  public GensonBuilder setSkipNull(boolean skipNull) {
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
  public GensonBuilder setHtmlSafe(boolean htmlSafe) {
    this.htmlSafe = htmlSafe;
    return this;
  }

  public boolean isHtmlSafe() {
    return htmlSafe;
  }

  /**
   * Indicates whether class metadata should be serialized and used during deserialization.
   *
   * @see com.owlike.genson.convert.ClassMetadataConverter ClassMetadataConverter
   */
  public GensonBuilder useClassMetadata(boolean enabled) {
    this.withClassMetadata = enabled;
    this.metadata = true;
    return this;
  }

  /**
   * Specifies the data format that should be used for java.util.Date serialization and
   * deserialization.
   *
   * @param dateFormat
   * @return a reference to this builder.
   */
  public GensonBuilder useDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
    return this;
  }

  public boolean isThrowExceptionOnNoDebugInfo() {
    return throwExcOnNoDebugInfo;
  }

  /**
   * Used in conjunction with {@link #useConstructorWithArguments(boolean)}. If true
   * an exception will be thrown when a class has been compiled without debug informations.
   *
   * @param throwExcOnNoDebugInfo
   * @return a reference to this builder.
   * @see com.owlike.genson.reflect.ASMCreatorParameterNameResolver
   * ASMCreatorParameterNameResolver
   */
  public GensonBuilder setThrowExceptionIfNoDebugInfo(boolean throwExcOnNoDebugInfo) {
    this.throwExcOnNoDebugInfo = throwExcOnNoDebugInfo;
    return this;
  }

  /**
   * If true, getters and setters would be used during serialization/deserialization in favor
   * of fields. If there is not getter/setter for a field then the field will be used, except
   * if you specified that fields should not be used with {@link #useFields(boolean)}. By
   * default getters, setters and fields will be used.
   */
  public GensonBuilder useMethods(boolean enabled) {
    this.useGettersAndSetters = enabled;
    return this;
  }

  public GensonBuilder useMethods(boolean enabled, VisibilityFilter visibility) {
    useMethods(enabled);
    return setMethodFilter(visibility);
  }

  /**
   * If true, fields will be used when no getter/setter is available, except if you specified
   * that no getter/setter should be used with {@link #useMethods(boolean)}, in
   * that case only fields will be used. By default getters, setters and fields will be used.
   */
  public GensonBuilder useFields(boolean enabled) {
    this.useFields = enabled;
    return this;
  }

  public GensonBuilder useFields(boolean enabled, VisibilityFilter visibility) {
    useFields(enabled);
    return setFieldFilter(visibility);
  }

  /**
   * If true {@link BeanView} mechanism will be enabled.
   */
  public GensonBuilder useBeanViews(boolean enabled) {
    this.withBeanViewConverter = enabled;
    return this;
  }

  /**
   * If true the concrete type of the serialized object will always be used. So if you have
   * List<Number> type it will not use the Number serializer but the one for the concrete type
   * of the current value.
   *
   * @param enabled
   * @return a reference to this builder.
   */
  public GensonBuilder useRuntimeType(boolean enabled) {
    this.useRuntimeTypeForSerialization = enabled;
    return this;
  }

  /**
   * If true constructor and method arguments name will be resolved from the generated debug
   * symbols during compilation. It is a very powerful feature from Genson, you should have a
   * look at {@link com.owlike.genson.reflect.ASMCreatorParameterNameResolver
   * ASMCreatorParameterNameResolver}.
   *
   * @param enabled
   * @return a reference to this builder.
   * @see #setThrowExceptionIfNoDebugInfo(boolean)
   */
  public GensonBuilder useConstructorWithArguments(boolean enabled) {
    this.withDebugInfoPropertyNameResolver = enabled;
    return this;
  }

  /**
   * Sets the null converter that should be used to handle null object values. If the
   * converter is called you are guaranteed that the value is null (for both, ser and deser).
   *
   * @param nullConverter
   * @return a reference to this builder.
   */
  public GensonBuilder setNullConverter(Converter<Object> nullConverter) {
    this.nullConverter = nullConverter;
    return this;
  }

  public GensonBuilder setFieldFilter(VisibilityFilter propertyFilter) {
    this.propertyFilter = propertyFilter;
    return this;
  }

  public GensonBuilder setMethodFilter(VisibilityFilter methodFilter) {
    this.methodFilter = methodFilter;
    return this;
  }

  public GensonBuilder setConstructorFilter(VisibilityFilter constructorFilter) {
    this.constructorFilter = constructorFilter;
    return this;
  }

  public GensonBuilder useStrictDoubleParse(boolean strictDoubleParse) {
    this.strictDoubleParse = strictDoubleParse;
    return this;
  }

  /**
   * If true outputed json will be indented using two spaces, otherwise (by default) all is
   * printed on same line.
   */
  public GensonBuilder useIndentation(boolean indent) {
    this.indent = indent;
    return this;
  }

  public GensonBuilder useDateAsTimestamp(boolean enabled) {
    this.useDateAsTimestamp = enabled;
    return this;
  }

  public GensonBuilder useMetadata(boolean metadata) {
    this.metadata = metadata;
    return this;
  }

  public GensonBuilder useByteAsInt(boolean enable) {
    if (enable) {
      withConverters(DefaultConverters.ByteArrayAsIntArrayConverter.instance);
    }
    return this;
  }

  /**
   * If set to true, Genson will throw a JsonBindingException when it encounters a property in the incoming json that does not match
   * a property in the class.
   * False by default.
   * @param enable
   * @return
   */
  public GensonBuilder failOnMissingProperty(boolean enable) {
    this.failOnMissingProperty = enable;
    return this;
  }

  /**
   * If set to false, during serialization class metadata will be serialized only for types where the runtime type differs from the static one.
   * Ex:
   *
   * <pre>
   * class Person {
   *   public Address address;
   * }
   * </pre>
   *
   * Here if the concrete instance of address is Address then this type will not be serialized as metadata, but if they differ then
   * it is serialized. By default this option is true, all types are serialized.
   * @param enable
   * @return
   */
  public GensonBuilder useClassMetadataWithStaticType(boolean enable) {
    this.classMetadataWithStaticType = enable;
    return this;
  }

  /**
   * When enabled tells Genson to be less strict on the incoming json and the mapping to Java types.
   * It allows to
   *  - Parse multiple root json values (not enclosed in an array) with same ObjectReader.
   *  - Wrap a single value into a list when a list is expected. Useful when dealing with APIs that unwrap
   *  arrays containing a single value.
   *
   * Disabled by default.
   */
  public GensonBuilder usePermissiveParsing(boolean enable) {
    this.permissiveParsing = enable;
    if (enable) withConverterFactory(DefaultConverters.SingleValueAsListFactory.instance);
    return this;
  }

  /**
   * Will wrap all the root objects under outputKey during serializaiton and unwrap the content under
   * inputKey during deserializaiton. For example:
   *
   * <code>
   *   Genson genson = new GensonBuilder().wrapRootValues("request", "response").create();
   *
   *   // would produce: {"response": {... person properties ...}}
   *   genson.serialize(person);
   *
   *   Person p = genson.deserialize("{\"request\":{...}}", Person.class);
   * </code>
   *
   * If you need this mechanism only for some types or using different root keys, then you can register JaxbBundle with
   * wrapRootValues(true) and annotate the specific classes with XmlRootElement.
   */
  public GensonBuilder wrapRootValues(final String inputKey, final String outputKey) {
    return withConverterFactory(new ChainedFactory() {
      @Override
      protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {

        return new DefaultConverters.WrappedRootValueConverter<Object>(
            inputKey,
            outputKey,
            (Converter<Object>) nextConverter
        );
      }
    });
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

    List<ContextualFactory<?>> defaultContextualFactories = new ArrayList<ContextualFactory<?>>();
    addDefaultContextualFactories(defaultContextualFactories);
    contextualFactories.addAll(defaultContextualFactories);

    beanDescriptorProvider = createBeanDescriptorProvider();

    if (withBeanViewConverter) {
      List<BeanMutatorAccessorResolver> resolvers = new ArrayList<BeanMutatorAccessorResolver>();
      resolvers.add(new BeanViewDescriptorProvider.BeanViewMutatorAccessorResolver());
      resolvers.add(mutatorAccessorResolver);
      beanViewDescriptorProvider = new BeanViewDescriptorProvider(
        new AbstractBeanDescriptorProvider.ContextualConverterFactory(contextualFactories), registeredViews,
        createBeanPropertyFactory(),
        new BeanMutatorAccessorResolver.CompositeResolver(resolvers),
        getPropertyNameResolver());
    }

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
        Type typeOfConverter = TypeUtil.typeOf(0, TypeUtil.lookupGenericType(Deserializer.class, deserializer.getClass()));
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
    return new Genson(converterFactory, getBeanDescriptorProvider(), nullConverter,
      isSkipNull(), isHtmlSafe(), classAliases, withClassMetadata,
      strictDoubleParse, indent, metadata, failOnMissingProperty, permissiveParsing);
  }

  /**
   * You should override this method if you want to add custom
   * {@link com.owlike.genson.convert.ChainedFactory ChainedFactory} or if you need to chain
   * them differently.
   *
   * @return the converter <u>factory instance that will be used to resolve
   * <strong>ALL</strong> converters</u>.
   */
  protected Factory<Converter<?>> createConverterFactory() {
    ChainedFactory chainHead = new CircularClassReferenceConverterFactory();

    chainHead.append(new NullConverter.NullConverterFactory());

    if (useRuntimeTypeForSerialization) chainHead.append(new RuntimeTypeConverter.RuntimeTypeConverterFactory());

    chainHead.append(new ClassMetadataConverter.ClassMetadataConverterFactory(classMetadataWithStaticType));

    if (customFactoryChain != null) chainHead.append(customFactoryChain);

    if (withBeanViewConverter) chainHead.append(new BeanViewConverter.BeanViewConverterFactory(
        getBeanViewDescriptorProvider()));

    ContextualFactoryDecorator ctxFactoryDecorator = new ContextualFactoryDecorator(
      new BasicConvertersFactory(getSerializersMap(), getDeserializersMap(),
        getFactories(), getBeanDescriptorProvider()));

    chainHead.append(ctxFactoryDecorator);

    return chainHead;
  }

  protected BeanMutatorAccessorResolver createBeanMutatorAccessorResolver() {
    List<BeanMutatorAccessorResolver> resolvers = new ArrayList<BeanMutatorAccessorResolver>();
    resolvers.add(new BeanMutatorAccessorResolver.GensonAnnotationsResolver());

    resolvers.add(new BeanMutatorAccessorResolver.StandardMutaAccessorResolver(propertyFilter,
      methodFilter, constructorFilter));

    return new BeanMutatorAccessorResolver.CompositeResolver(resolvers);
  }

  /**
   * You can override this method if you want to change the
   * {@link com.owlike.genson.reflect.PropertyNameResolver PropertyNameResolver} that are
   * registered by default. You can also simply replace the default PropertyNameResolver by
   * setting another one with {@link #set(PropertyNameResolver)}.
   *
   * @return the property name resolver to be used. It should be an instance of
   * {@link com.owlike.genson.reflect.PropertyNameResolver.CompositePropertyNameResolver
   * PropertyNameResolver.CompositePropertyNameResolver}, otherwise you will not be
   * able to add others PropertyNameResolvers using
   * {@link #with(PropertyNameResolver...)} method.
   */
  protected PropertyNameResolver createPropertyNameResolver() {
    List<PropertyNameResolver> resolvers = new ArrayList<PropertyNameResolver>();
    resolvers.add(new PropertyNameResolver.AnnotationPropertyNameResolver());
    resolvers.add(new PropertyNameResolver.ConventionalBeanPropertyNameResolver());
    if (withDebugInfoPropertyNameResolver)
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
    converters.add(DefaultConverters.ShortConverter.instance);
    converters.add(DefaultConverters.FloatConverter.instance);
    converters.add(DefaultConverters.NumberConverter.instance);
    converters.add(DefaultConverters.CharConverter.instance);
    converters.add(DefaultConverters.ByteConverter.instance);
    converters.add(new DefaultConverters.DateConverter(dateFormat, useDateAsTimestamp));
    converters.add(DefaultConverters.URLConverter.instance);
    converters.add(DefaultConverters.URIConverter.instance);
    converters.add(DefaultConverters.TimestampConverter.instance);
    converters.add(DefaultConverters.BigDecimalConverter.instance);
    converters.add(DefaultConverters.BigIntegerConverter.instance);
    converters.add(DefaultConverters.UUIDConverter.instance);
    converters.add(DefaultConverters.FileConverter.instance);
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
    factories.add(new DefaultConverters.CalendarConverterFactory(
      new DefaultConverters.DateConverter(dateFormat, useDateAsTimestamp)
    ));
  }

  protected void addDefaultContextualFactories(List<ContextualFactory<?>> factories) {
    factories.add(new DefaultConverters.DateContextualFactory());
    factories.add(new DefaultConverters.PropertyConverterFactory());
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

    ContextualConverterFactory contextualConverterFactory = new ContextualConverterFactory(contextualFactories);
    BeanPropertyFactory beanPropertyFactory = createBeanPropertyFactory();

    List<BeanDescriptorProvider> providers = new ArrayList<BeanDescriptorProvider>();
    for (GensonBundle bundle : _bundles) {
      BeanDescriptorProvider provider = bundle.createBeanDescriptorProvider(contextualConverterFactory,
        beanPropertyFactory,
        getMutatorAccessorResolver(),
        getPropertyNameResolver(), this);

      if (provider != null) providers.add(provider);
    }

    providers.add(new BaseBeanDescriptorProvider(
      new AbstractBeanDescriptorProvider.ContextualConverterFactory(contextualFactories),
      createBeanPropertyFactory(), getMutatorAccessorResolver(), getPropertyNameResolver(),
      useGettersAndSetters, useFields, true));

    return new CompositeBeanDescriptorProvider(providers);
  }

  protected BeanPropertyFactory createBeanPropertyFactory() {
    if (withBeanViewConverter)
      beanPropertyFactories.add(new BeanViewDescriptorProvider.BeanViewPropertyFactory(
        registeredViews));
    beanPropertyFactories.add(new BeanPropertyFactory.StandardFactory());
    return new BeanPropertyFactory.CompositeFactory(beanPropertyFactories);
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

  public final boolean isDateAsTimestamp() {
    return useDateAsTimestamp;
  }
}
