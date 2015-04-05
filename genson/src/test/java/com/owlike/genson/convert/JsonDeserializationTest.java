package com.owlike.genson.convert;

import static org.junit.Assert.*;

import java.awt.Rectangle;
import java.awt.Shape;
import java.io.StringReader;
import java.util.*;

import com.owlike.genson.*;
import com.owlike.genson.stream.ObjectReader;
import org.junit.Test;

import com.owlike.genson.annotation.JsonProperty;
import com.owlike.genson.bean.ComplexObject;
import com.owlike.genson.bean.Primitives;
import com.owlike.genson.bean.Media.Player;
import com.owlike.genson.reflect.BeanDescriptor;
import com.owlike.genson.stream.JsonReader;

public class JsonDeserializationTest {
  final Genson genson = new GensonBuilder().useConstructorWithArguments(true).create();


  @Test public void testReadMultipleRootObjectsNotEnclosedInArrayAndMapManually() {
    Genson genson = new GensonBuilder().usePermissiveParsing(true).create();
    GenericType<Pojo> type = GenericType.of(Pojo.class);
    Context ctx = new Context(genson);

    int i = 1;
    for (ObjectReader reader = genson.createReader(new StringReader("{\"a\":1}{\"a\":2}"));
      reader.hasNext(); reader.next(), i++) {
      Pojo p = genson.deserialize(type, reader, ctx);
      assertEquals(p.a, i);
    }
    assertEquals(i, 3);
  }

  @Test public void testReadMultipleRootObjectsNotEnclosedInArrayAndBind() {
    Genson genson = new GensonBuilder().usePermissiveParsing(true).create();
    ObjectReader reader = genson.createReader(new StringReader("{\"a\":1}{\"a\":2}"));
    int i = 1;
    for (Iterator<Pojo> it = genson.deserializeValues(reader, GenericType.of(Pojo.class));
      it.hasNext(); i++) {
      Pojo p = it.next();
      assertEquals(p.a, i);
    }
    assertEquals(i, 3);
  }

  @Test
  public void testASMResolverShouldNotFailWhenUsingBootstrapClassloader() {
    assertNotNull(genson.deserialize("{}", Exception.class));
  }

  @Test
  public void testJsonEmptyObject() {
    String src = "{}";
    Primitives p = genson.deserialize(src, Primitives.class);
    assertNull(p.getText());
  }

  @Test
  public void testJsonEmptyArray() {
    String src = "[]";
    Integer[] p = genson.deserialize(src, Integer[].class);
    assertTrue(p.length == 0);
  }

  @Test
  public void testJsonComplexObjectEmpty() {
    String src = "{\"primitives\":null,\"listOfPrimitives\":[], \"arrayOfPrimitives\": null}";
    ComplexObject co = genson.deserialize(src, ComplexObject.class);

    assertNull(co.getPrimitives());
    assertTrue(co.getListOfPrimitives().size() == 0);
    assertNull(co.getArrayOfPrimitives());
  }

  @Test
  public void testDeserializeEmptyJson() {
    Integer i = genson.deserialize("\"\"", Integer.class);
    assertNull(i);
    i = genson.deserialize("", Integer.class);
    assertNull(i);
    i = genson.deserialize("null", Integer.class);
    assertNull(i);

    int[] arr = genson.deserialize("", int[].class);
    assertNull(arr);
    arr = genson.deserialize("null", int[].class);
    assertNull(arr);
    arr = genson.deserialize("[]", int[].class);
    assertNotNull(arr);

    Primitives p = genson.deserialize("", Primitives.class);
    assertNull(p);
    p = genson.deserialize("null", Primitives.class);
    assertNull(p);
    p = genson.deserialize("{}", Primitives.class);
    assertNotNull(p);
  }

  @Test
  public void testJsonNumbersLimit() {
    String src = "[" + String.valueOf(Long.MAX_VALUE) + "," + String.valueOf(Long.MIN_VALUE)
      + "]";
    long[] arr = genson.deserialize(src, long[].class);
    assertTrue(Long.MAX_VALUE == arr[0]);
    assertTrue(Long.MIN_VALUE == arr[1]);

    src = "[" + String.valueOf(Double.MAX_VALUE) + "," + String.valueOf(Double.MIN_VALUE) + "]";
    double[] arrD = genson.deserialize(src, double[].class);
    assertTrue(Double.MAX_VALUE == arrD[0]);
    assertTrue(Double.MIN_VALUE == arrD[1]);
  }

  @Test
  public void testJsonPrimitivesObject() {
    String src = "{\"intPrimitive\":1, \"integerObject\":7, \"doublePrimitive\":1.01,"
      + "\"doubleObject\":2.003,\"text\": \"HEY...YA!\", "
      + "\"booleanPrimitive\":true,\"booleanObject\":false}";
    Primitives p = genson.deserialize(src, Primitives.class);
    assertEquals(p.getIntPrimitive(), 1);
    assertEquals(p.getIntegerObject(), new Integer(7));
    assertEquals(p.getDoublePrimitive(), 1.01, 0);
    assertEquals(p.getDoubleObject(), new Double(2.003));
    assertEquals(p.getText(), "HEY...YA!");
    assertEquals(p.isBooleanPrimitive(), true);
    assertEquals(p.isBooleanObject(), Boolean.FALSE);
  }

  @Test
  public void testJsonDoubleArray() {
    String src = "[5,      0.006, 9.0E-11 ]";
    double[] array = genson.deserialize(src, double[].class);
    assertEquals(array[0], 5, 0);
    assertEquals(array[1], 0.006, 0);
    assertEquals(array[2], 0.00000000009, 0);
  }

  @Test
  public void testJsonComplexObject() {
    ComplexObject coo = new ComplexObject(createPrimitives(), Arrays.asList(createPrimitives(),
      createPrimitives()), new Primitives[]{createPrimitives(), createPrimitives()});
    ComplexObject co = genson.deserialize(coo.jsonString(), ComplexObject.class);
    ComplexObject.assertCompareComplexObjects(co, coo);
  }

  @Test
  public void testJsonComplexObjectSkipValue() {
    ComplexObject coo = new ComplexObject(createPrimitives(), Arrays.asList(createPrimitives(),
      createPrimitives(), createPrimitives(), createPrimitives(), createPrimitives(),
      createPrimitives()), new Primitives[]{createPrimitives(), createPrimitives()});

    DummyWithFieldToSkip dummy = new DummyWithFieldToSkip(coo, coo, createPrimitives(),
      Arrays.asList(coo));
    DummyWithFieldToSkip dummy2 = genson.deserialize(dummy.jsonString(),
      DummyWithFieldToSkip.class);

    ComplexObject.assertCompareComplexObjects(dummy.getO1(), dummy2.getO1());
    ComplexObject.assertCompareComplexObjects(dummy.getO2(), dummy2.getO2());
    Primitives.assertComparePrimitives(dummy.getP(), dummy2.getP());
  }

  @Test
  public void testJsonToBeanWithConstructor() {
    String json = "{\"other\":{\"name\":\"TITI\", \"age\": 13}, \"name\":\"TOTO\", \"age\":26}";
    BeanWithConstructor bean = genson.deserialize(json, BeanWithConstructor.class);
    assertEquals(bean.age, 26);
    assertEquals(bean.name, "TOTO");
    assertEquals(bean.other.age, 13);
    assertEquals(bean.other.name, "TITI");
  }

  public static class BeanWithConstructor {
    final String name;
    final int age;
    final BeanWithConstructor other;

    public BeanWithConstructor(@JsonProperty(value = "name") String name,
                               @JsonProperty(value = "age") int age,
                               @JsonProperty(value = "other") BeanWithConstructor other) {
      this.name = name;
      this.age = age;
      this.other = other;
    }

    public void setName(String name) {
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testJsonToUntypedList() {
    String src = "[1, 1.1, \"aa\", true, false]";
    List<Object> l = genson.deserialize(src, List.class);
    assertArrayEquals(new Object[]{1L, 1.1, "aa", true, false},
      l.toArray(new Object[l.size()]));
  }

  @Test
  public void testMultidimensionalArray() {
    String json = "[[[42,24]],[[43,34]]]";
    long[][][] array = genson.deserialize(json, long[][][].class);
    assertArrayEquals(new long[]{42L, 24L}, array[0][0]);
    assertArrayEquals(new long[]{43L, 34L}, array[1][0]);

    String json3 = "[[[\"abc\"],[42,24],[\"def\"],[43,34]]]";
    genson.deserialize(json3, Object[][][].class);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testContentDrivenDeserialization() {
    String src = "{\"list\":[1, 2.3, 5, null]}";
    TypeVariableList<Number> tvl = genson.deserialize(src, TypeVariableList.class);
    assertArrayEquals(tvl.list.toArray(new Number[tvl.list.size()]), new Number[]{1, 2.3, 5,
      null});

    String json = "[\"hello\",5,{\"name\":\"GREETINGS\",\"source\":\"guest\"}]";
    Map<String, String> map = new HashMap<String, String>();
    map.put("name", "GREETINGS");
    map.put("source", "guest");
    assertEquals(Arrays.asList("hello", 5L, map), genson.deserialize(json, Collection.class));

    // doit echouer du a la chaine et que list est de type <E extends Number>
    src = "{\"list\":[1, 2.3, 5, \"a\"]}";
    try {
      tvl = genson.deserialize(src, TypeVariableList.class);
      fail();
    } catch (Exception e) {
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testUntypedDeserializationToMap() {
    String src = "{\"key1\": 1, \"key2\": 1.2, \"key3\": true, \"key4\": \"string\", \"key5\": null, \"list\":[1,2.0005,3]}";
    Map<String, Object> map = genson.deserialize(src, Map.class);
    assertEquals(map.get("key1"), 1L);
    assertEquals(map.get("key2"), 1.2);
    assertEquals(map.get("key3"), true);
    assertEquals(map.get("key4"), "string");
    assertNull(map.get("key5"));
    List<Number> list = (List<Number>) map.get("list");
    assertEquals(Arrays.asList(1L, 2.0005, 3L), list);
  }

  @Test
  public void testDeserializeWithConstructorAndFieldsAndSetters() {
    String json = "{\"p0\":0,\"p1\":1,\"p2\":2,\"shouldSkipIt\":55, \"nameInJson\": 3}";
    ClassWithConstructorFieldsAndGetters c = genson.deserialize(json,
      ClassWithConstructorFieldsAndGetters.class);
    assertEquals(c.p0, new Integer(0));
    assertEquals(c.p1, new Integer(1));
    assertEquals(c.p2, new Integer(2));
    assertTrue(c.constructorCalled);
  }

  @Test
  public void testDeserializeWithConstructorAndMissingFields() {
    String json = "{\"p0\":0,\"p1\":1, \"nameInJson\": 3}";
    ClassWithConstructorFieldsAndGetters c = genson.deserialize(json,
      ClassWithConstructorFieldsAndGetters.class);
    assertTrue(c.constructorCalled);
    assertEquals(new Integer(0), c.p0);
    assertEquals(new Integer(1), c.p1);
    assertEquals(null, c.p2);
    assertEquals(new Integer(3), c.hidden);
  }

  @Test
  public void testDeserializeWithConstructorMixedAnnotation() {
    String json = "{\"p0\":0,\"p1\":1,\"p2\":2,\"shouldSkipIt\":55,   \"nameInJson\":\"125\"}";
    ClassWithConstructorFieldsAndGetters c = genson.deserialize(json,
      ClassWithConstructorFieldsAndGetters.class);
    assertTrue(c.constructorCalled);
    assertEquals(c.p0, new Integer(0));
    assertEquals(c.p1, new Integer(1));
    assertEquals(c.p2, new Integer(2));
    assertEquals(c.hidden, new Integer(125));
  }

  @Test
  public void testDeserializeJsonWithClassAlias() {
    Genson genson = new GensonBuilder().addAlias("rect", Rectangle.class).create();
    Shape p = genson.deserialize("{\"@class\":\"rect\"}", Shape.class);
    assertTrue(p instanceof Rectangle);
    p = genson.deserialize("{\"@class\":\"java.awt.Rectangle\"}", Shape.class);
    assertTrue(p instanceof Rectangle);
  }

  @Test
  public void testDeserealizeIntoExistingBean() {
    BeanDescriptor<ClassWithConstructorFieldsAndGetters> desc = (BeanDescriptor<ClassWithConstructorFieldsAndGetters>) genson
      .getBeanDescriptorProvider().provide(ClassWithConstructorFieldsAndGetters.class,
        ClassWithConstructorFieldsAndGetters.class, genson);
    ClassWithConstructorFieldsAndGetters c = new ClassWithConstructorFieldsAndGetters(1, 2, "3") {
      @Override
      public void setP2(Integer p2) {
        this.p2 = p2;
      }
    };
    c.constructorCalled = false;
    String json = "{\"p0\":0,\"p1\":1,\"p2\":2,\"shouldSkipIt\":55,   \"nameInJson\":\"125\"}";
    desc.deserialize(c, new JsonReader(json), new Context(genson));
    assertFalse(c.constructorCalled);
    assertEquals(c.p0, new Integer(0));
    assertEquals(c.p1, new Integer(1));
    assertEquals(c.p2, new Integer(2));
  }

  @Test
  public void testDeserializeEnum() {
    assertEquals(Player.JAVA, genson.deserialize("\"JAVA\"", Player.class));
  }

  @Test(expected = JsonBindingException.class) public void testDeserWithMissingPropertyShouldFail() {
    new GensonBuilder()
      .failOnMissingProperty(true)
      .create()
      .deserialize("{\"missingKey\": 1}", Empty.class);
  }

  @Test public void testDeserInExistingInstance() {
    Pojo pojo = new Pojo();
    pojo.a = 1;
    pojo.b = 2;
    genson.deserializeInto("{\"b\":3,\"str\":\"foo\"}", pojo);

    assertEquals(1, pojo.a);
    assertEquals(3, pojo.b);
    assertEquals("foo", pojo.str);
  }

  public static class Empty {}

  public static class Pojo {
    public String str;
    public int a;
    public int b;
  }

  @SuppressWarnings("unused")
  private static class ClassWithConstructorFieldsAndGetters {
    final Integer p0;
    private Integer p1;
    protected Integer p2;
    transient final Integer hidden;
    // should not be serialized
    public transient boolean constructorCalled = false;

    public ClassWithConstructorFieldsAndGetters(Integer p0, Integer p2,
                                                @JsonProperty(value = "nameInJson") String dontCareOfTheName) {
      constructorCalled = true;
      this.p0 = p0;
      this.p2 = p2;
      this.hidden = Integer.parseInt(dontCareOfTheName);
    }

    public void setP1(Integer p1) {
      this.p1 = p1;
    }

    // use the constructor
    public void setP2(Integer p2) {
      fail();
      this.p2 = p2;
    }
  }

  @SuppressWarnings("unused")
  private static class TypeVariableList<E extends Number> {
    List<E> list;

    public TypeVariableList() {
    }

    public List<E> getList() {
      return list;
    }

    public void setList(List<E> list) {
      this.list = list;
    }

  }

  @SuppressWarnings("unused")
  private static class DummyWithFieldToSkip {
    ComplexObject o1;
    ComplexObject o2;
    Primitives p;
    List<ComplexObject> list;

    public DummyWithFieldToSkip() {
    }

    public DummyWithFieldToSkip(ComplexObject o1, ComplexObject o2, Primitives p,
                                List<ComplexObject> list) {
      super();
      this.o1 = o1;
      this.o2 = o2;
      this.p = p;
      this.list = list;
    }

    public String jsonString() {
      StringBuilder sb = new StringBuilder();

      sb.append("{\"list\":[");
      if (list != null) {
        for (int i = 0; i < list.size(); i++)
          sb.append(list.get(i).jsonString()).append(',');
        sb.append(list.get(list.size() - 1).jsonString());
      }
      sb.append("],\"o1\":").append(o1.jsonString()).append(",\"o2\":")
        .append(o2.jsonString()).append(",\"ooooooSkipMe\":").append(o2.jsonString())
        .append(",\"p\":").append(p.jsonString()).append('}');

      return sb.toString();
    }

    public ComplexObject getO1() {
      return o1;
    }

    public void setO1(ComplexObject o1) {
      this.o1 = o1;
    }

    public ComplexObject getO2() {
      return o2;
    }

    public void setO2(ComplexObject o2) {
      this.o2 = o2;
    }

    public Primitives getP() {
      return p;
    }

    public void setP(Primitives p) {
      this.p = p;
    }
  }

  private Primitives createPrimitives() {
    return new Primitives(1, new Integer(10), 1.00001, new Double(0.00001), "TEXT ...  HEY!",
      true, new Boolean(false));
  }
}
