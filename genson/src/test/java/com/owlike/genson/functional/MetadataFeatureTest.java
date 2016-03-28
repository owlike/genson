package com.owlike.genson.functional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.owlike.genson.GensonBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.owlike.genson.Genson;

public class MetadataFeatureTest {
  private Genson genson;

  @Before
  public void setUp() {
    genson = new GensonBuilder().useClassMetadata(true).addAlias("bean", Bean.class).create();
  }

  @Test
  public void testSerializeUnknownType() {
    Bean bean = new Bean();
    bean.value = new Date();
    assertEquals("{\"@class\":\"bean\",\"value\":" + ((Date) bean.value).getTime() + "}", genson.serialize(bean));
  }

  @Test
  public void testDeserializeToUnknownType() {
    Bean bean = (Bean) genson.deserialize("{\"@class\":\"bean\",\"value\":{\"@class\":\"bean\"}}", Object.class);
    assertTrue(bean.value instanceof Bean);

    bean = genson.deserialize("{\"@class\":\"bean\",\"value\":{\"@class\":\"bean\"}}", Bean.class);
    assertTrue(bean.value instanceof Bean);
  }

  @Test public void testClassMetadataShouldNotBeSerializedForStaticTypes() {
    Genson genson = new GensonBuilder().addAlias("beanHolder", BeanHolder.class).useClassMetadataWithStaticType(false).create();

    BeanHolder v = new BeanHolder();
    v.bean = new Bean();

    assertEquals("{\"@class\":\"beanHolder\",\"bean\":{\"value\":null}}", genson.serialize(v));
  }

  @Test public void testClassMetadataShouldBeSerializedOnceWhenUsingUntypedConverter() {
    Bean bean = new Bean();
    bean.value = new Bean();
    assertEquals("{\"@class\":\"bean\",\"value\":{\"@class\":\"bean\",\"value\":null}}", genson.serialize(bean));
  }

  @Test public void testUseClassMetadataForAllTypes() {
    List l = Arrays.asList(
      new Date(1, 1, 1), 2l, true, null, new Bean()
    );

    BeanHolder v = new BeanHolder();
    v.bean = new Bean();
    v.bean.value = l;
    String json = new GensonBuilder().useIndentation(true).useRuntimeType(true).useClassMetadataWithStaticType(false).useClassMetadataForAllTypes(true).create().serialize(v);

    System.out.println(json);
  }

  @Test public void testDontWriteClassForConcreteTypeWhenUsedWithFullMetadata() {


    String json = new GensonBuilder().useRuntimeType(true)
                    .useClassMetadataWithStaticType(false)
                    .useClassMetadataForAllTypes(true)
                    .create()
                    .serialize(new Object() {
                      Bean bean = new Bean();
                      {{ bean.value = new Bean(); }}
                    });

    System.out.println(json);
  }

  static class Bean {
    Object value;
  }

  class BeanHolder {
    Bean bean;
  }
}
