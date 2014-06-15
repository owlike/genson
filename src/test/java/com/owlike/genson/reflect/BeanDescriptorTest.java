package com.owlike.genson.reflect;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.owlike.genson.GensonBuilder;
import org.junit.Test;

import com.owlike.genson.Converter;
import com.owlike.genson.Factory;
import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonCreator;
import com.owlike.genson.annotation.JsonIgnore;
import com.owlike.genson.annotation.JsonProperty;
import com.owlike.genson.convert.BasicConvertersFactory;
import com.owlike.genson.convert.DefaultConverters;
import com.owlike.genson.convert.DefaultConverters.CollectionConverter;
import com.owlike.genson.reflect.AbstractBeanDescriptorProvider.ContextualConverterFactory;

import static org.junit.Assert.*;

public class BeanDescriptorTest {
  private Genson genson = new Genson();

  @Test
  public void testFailFastBeanDescriptorWithWrongType() {
    BeanDescriptorProvider provider = new GensonBuilder() {
      protected BeanDescriptorProvider createBeanDescriptorProvider() {
        return new BaseBeanDescriptorProvider(new ContextualConverterFactory(null),
          new BeanPropertyFactory.CompositeFactory(Arrays
            .asList(new BeanPropertyFactory.StandardFactory())),
          getMutatorAccessorResolver(), getPropertyNameResolver(),
          true, true, true) {
          @SuppressWarnings({"unchecked", "rawtypes"})
          protected <T> com.owlike.genson.reflect.BeanDescriptor<T> create(
            java.lang.Class<T> forClass,
            java.lang.reflect.Type ofType,
            com.owlike.genson.reflect.BeanCreator creator,
            java.util.List<com.owlike.genson.reflect.PropertyAccessor> accessors,
            java.util.Map<String, com.owlike.genson.reflect.PropertyMutator> mutators) {
            return new BeanDescriptor(ThatObject.class, ThatObject.class, accessors,
              mutators, creator);
          }
        };
      }
    }.create().getBeanDescriptorFactory();
    try {
      provider.provide(AnotherObject.class, ThatObject.class, genson);
      fail();
    } catch (ClassCastException cce) {
      // OK
    }
  }

  private static class ThatObject {
    @SuppressWarnings("unused")
    String aString;
    @SuppressWarnings("unused")
    int aPrimitive;
    @SuppressWarnings("unused")
    List<Date> listOfDates;

    @SuppressWarnings("unused")
    public ThatObject(AnotherObject anotherObject) {
    }
  }

  private static class AnotherObject {
    @SuppressWarnings("unused")
    public AnotherObject() {
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testConverterChain() {
    Genson genson = new GensonBuilder() {
      @Override
      protected Factory<Converter<?>> createConverterFactory() {
        return new BasicConvertersFactory(
          getSerializersMap(), getDeserializersMap(), getFactories(),
          getBeanDescriptorProvider());
      }
    }.useConstructorWithArguments(true).create();

    @SuppressWarnings("rawtypes")
    BeanDescriptor<ThatObject> pDesc = (BeanDescriptor) genson
      .provideConverter(ThatObject.class);
    assertEquals(DefaultConverters.StringConverter.class,
      pDesc.mutableProperties.get("aString").propertyDeserializer.getClass());
    assertEquals(DefaultConverters.PrimitiveConverterFactory.intConverter.class,
      pDesc.mutableProperties.get("aPrimitive").propertyDeserializer.getClass());
    assertEquals(DefaultConverters.CollectionConverter.class,
      pDesc.mutableProperties.get("listOfDates").propertyDeserializer.getClass());
    @SuppressWarnings("rawtypes")
    CollectionConverter<Object> listOfDateConverter = (CollectionConverter) pDesc.mutableProperties
      .get("listOfDates").propertyDeserializer;
    assertEquals(DefaultConverters.DateConverter.class, listOfDateConverter
      .getElementConverter().getClass());

    assertEquals(BeanDescriptor.class,
      pDesc.mutableProperties.get("anotherObject").propertyDeserializer.getClass());
  }

  @Test
  public void genericTypeTest() {
    BaseBeanDescriptorProvider provider = new BaseBeanDescriptorProvider(
      new ContextualConverterFactory(null), new BeanPropertyFactory.CompositeFactory(
      Arrays.asList(new BeanPropertyFactory.StandardFactory())),
      new BeanMutatorAccessorResolver.StandardMutaAccessorResolver(),
      new PropertyNameResolver.ConventionalBeanPropertyNameResolver(), true, true, true);

    BeanDescriptor<SpecilizedClass> bd = provider.provide(SpecilizedClass.class,
      SpecilizedClass.class, new Genson());
    assertEquals(B.class, getAccessor("t", bd).type);
    assertEquals(B.class,
      ((GenericArrayType) getAccessor("tArray", bd).type).getGenericComponentType());
    assertEquals(Double.class, getAccessor("value", bd).type);
  }

  @Test
  public void jsonWithJsonIgnore() throws SecurityException, NoSuchFieldException {
    BeanMutatorAccessorResolver strategy = new BeanMutatorAccessorResolver.CompositeResolver(
      Arrays.asList(new BeanMutatorAccessorResolver.GensonAnnotationsResolver(),
        new BeanMutatorAccessorResolver.StandardMutaAccessorResolver()));

    assertFalse(strategy.isAccessor(
      ClassWithIgnoredProperties.class.getDeclaredField("ignore"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertFalse(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("ignore"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertTrue(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("a"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertFalse(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("a"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertTrue(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("b"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertFalse(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("b"),
      ClassWithIgnoredProperties.class).booleanValue());
  }

  @Test
  public void jsonInclusionWithJsonProperty() throws SecurityException, NoSuchFieldException {
    BeanMutatorAccessorResolver strategy = new BeanMutatorAccessorResolver.CompositeResolver(
      Arrays.asList(new BeanMutatorAccessorResolver.GensonAnnotationsResolver(),
        new BeanMutatorAccessorResolver.StandardMutaAccessorResolver()));

    assertTrue(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("p"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertTrue(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("p"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertFalse(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("q"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertTrue(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("q"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertFalse(strategy.isMutator(ClassWithIgnoredProperties.class.getDeclaredField("r"),
      ClassWithIgnoredProperties.class).booleanValue());
    assertTrue(strategy.isAccessor(ClassWithIgnoredProperties.class.getDeclaredField("r"),
      ClassWithIgnoredProperties.class).booleanValue());
  }

  PropertyAccessor getAccessor(String name, BeanDescriptor<?> bd) {
    for (PropertyAccessor a : bd.accessibleProperties)
      if (name.equals(a.name)) return a;
    return null;
  }

  @Test
  public void testOneCreatorPerClass() {
    try {
      genson.provideConverter(MultipleCreator.class);
      fail();
    } catch (Exception e) {
    }
  }

  @Test
  public void testUseExplicitMethodCtr() {
    genson.deserialize("{}", ForceMethodCreator.class);
    assertTrue(ForceMethodCreator.usedMethod);
  }

  @Test
  public void testUseExplicitConstructorCtr() {
    genson.deserialize("{}", ForceConstructorCreator.class);
    assertTrue(ForceConstructorCreator.usedCtr);
  }

  static class ForceMethodCreator {
    public static transient boolean usedMethod = false;

    ForceMethodCreator() {
    }

    @JsonCreator
    public static ForceMethodCreator create() {
      usedMethod = true;
      return new ForceMethodCreator();
    }
  }

  static class ForceConstructorCreator {
    public static transient boolean usedCtr = false;

    ForceConstructorCreator() {
    }

    @JsonCreator
    ForceConstructorCreator(@JsonProperty("i") Integer iii) {
      usedCtr = true;
    }
  }

  static class MultipleCreator {
    @JsonCreator
    MultipleCreator() {
    }

    @JsonCreator
    public static MultipleCreator create() {
      return null;
    }
  }

  public static class B {
    public String v;
  }

  public static class ClassWithGenerics<T, E extends Number> {
    public T t;
    public T[] tArray;
    public E value;

    public void setT(T t) {
      this.t = t;
    }
  }

  public static class SpecilizedClass extends ClassWithGenerics<B, Double> {

  }

  public class ClassWithIgnoredProperties {
    @JsonIgnore
    public String ignore;
    @JsonIgnore(serialize = true)
    String a;
    @JsonIgnore(deserialize = true)
    public String b;

    @JsonProperty
    transient int p;
    @JsonProperty(serialize = false)
    private transient int q;
    @JsonProperty(deserialize = false)
    public transient int r;
  }
}
