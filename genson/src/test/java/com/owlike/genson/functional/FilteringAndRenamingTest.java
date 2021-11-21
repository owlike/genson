package com.owlike.genson.functional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.owlike.genson.GensonBuilder;
import com.owlike.genson.Trilean;
import com.owlike.genson.annotation.JsonProperty;
import com.owlike.genson.reflect.BeanMutatorAccessorResolver;
import org.junit.Test;

import static org.junit.Assert.*;

import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonIgnore;

public class FilteringAndRenamingTest {

  @Test
  public void excludeAllExceptJsonProperty() {
    ClassWithOneJsonProperty value = new ClassWithOneJsonProperty();
    value.a = 1;
    value.b = 2;

    Genson genson = new GensonBuilder()
      .exclude(Object.class)
      .with(new BeanMutatorAccessorResolver.GensonAnnotationsResolver())
      .create();


    assertEquals("{\"b\":2}", genson.serialize(value));

    ClassWithOneJsonProperty actual = genson.deserialize("{\"a\":1,\"b\":2}", ClassWithOneJsonProperty.class);
    assertEquals(0, actual.a);
    assertEquals(2, actual.b);
  }

  @Test
  public void testSetterWithoutArgs() {
    // bug https://groups.google.com/forum/?fromgroups=#!topic/genson/9rE026i7Vhg
    assertNotNull(new GensonBuilder().exclude("any").create()
      .provideConverter(BeanWithVoidSetter.class));
  }

  static class BeanWithVoidSetter {
    public void setXX() {
    }

    public void getXX() {

    }
  }

  @Test
  public void testRenameProperty() {
    MyAClass mac = new MyAClass();
    mac.myname = "toto";
    String expectedSuccess = "{\"name\":\"toto\"}";
    String expectedFailure = "{\"myname\":\"toto\"}";

    String json = new GensonBuilder().rename("myname", "name").create().serialize(mac);
    assertEquals(expectedSuccess, json);

    json = new GensonBuilder().rename(String.class, "name").create().serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().rename(int.class, "name").create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().rename("myname", MyAClass.class, "name").create()
      .serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().rename("myname", List.class, "name").create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().rename("myname", MyAClass.class, "name", String.class).create()
      .serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().rename("myname", MyAClass.class, "name", Integer.class)
      .create().serialize(mac);
    assertEquals(expectedFailure, json);
  }

  @Test
  public void testExcludeProperty() {
    MyAClass mac = new MyAClass();
    mac.myname = "toto";
    String expectedSuccess = "{}";
    String expectedFailure = "{\"myname\":\"toto\"}";

    String json = new GensonBuilder().exclude("myname").create().serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().exclude("xxx").create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().exclude(String.class).create().serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().exclude(Integer.class).create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().exclude("myname", MyAClass.class).create().serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().exclude("myname", List.class).create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().exclude("myname", MyAClass.class, String.class).create()
      .serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().exclude("myname", MyAClass.class, Integer.class).create()
      .serialize(mac);
    assertEquals(expectedFailure, json);
  }

  @Test
  public void testIncludeProperty() {
    ClassWithIncludedProperty mac = new ClassWithIncludedProperty();
    mac.prop = "toto";
    String expectedSuccess = "{\"prop\":\"toto\"}";
    String expectedFailure = "{}";

    String json = new GensonBuilder().include("prop").create().serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().include("xxx").create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().include(String.class).create().serialize(mac);
    assertEquals(expectedSuccess, json);

    json = new GensonBuilder().include("prop", ClassWithIncludedProperty.class).create()
      .serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().include("prop", MyAClass.class).create().serialize(mac);
    assertEquals(expectedFailure, json);

    json = new GensonBuilder().include("prop", ClassWithIncludedProperty.class, String.class)
      .create().serialize(mac);
    assertEquals(expectedSuccess, json);
    json = new GensonBuilder().include("prop", ClassWithIncludedProperty.class, Integer.class)
      .create().serialize(mac);
    assertEquals(expectedFailure, json);
  }

  @Test
  public void testExcludePropertyFromSuperClass() {
    AnotherClass mac = new AnotherClass();
    mac.transientLong = 11;
    mac.prop2 = "hi";
    String expectedSuccess = "{\"prop2\":\"hi\"}";

    String json = new GensonBuilder().exclude("transientLong", ClassWithTransient.class)
      .create().serialize(mac);
    assertEquals(expectedSuccess, json);
  }

  @Test // https://github.com/owlike/genson/issues/169
  public void testMultipleAliases() {
    ClassWithMultipleAliases o = new Genson().deserialize("{\"first\":true, \"second\":true}", ClassWithMultipleAliases.class);

    assertTrue(o.first);
    assertTrue(o.second);
  }

  public static class ClassWithMultipleAliases {
    @JsonProperty(aliases = {"first","first_"})
    protected boolean first;
    @JsonProperty(aliases = {"second"})
    protected boolean second;
  }

  public static class ClassWithOneJsonProperty {
    public String[] array;
    public int a;
    @JsonProperty
    public int b;
  }

  static class AnotherClass extends ClassWithTransient {
    public String prop2;
  }

  static class ClassWithIncludedProperty {
    @JsonIgnore
    private String prop;
  }

  static class ClassWithTransient {
    public transient long transientLong;

    public long getTransientLong() {
      return transientLong;
    }
  }

  static class MyAClass {
    private String myname;

    public String getMyname() {
      return myname;
    }

    public void setMyname(String myname) {
      this.myname = myname;
    }
  }
}
