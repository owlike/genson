package com.owlike.genson.reflect;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import com.owlike.genson.annotation.JsonCreator;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Aleksandar Seovic  2018.05.09
 */
public class UnknownPropertyHandlerTest {
    private static final Genson GENSON = new GensonBuilder()
                    .useClassMetadata(true)
                    .useConstructorWithArguments(true)
                    .useUnknownPropertyHandler(new EvolvableHandler())
                    .useIndentation(true)
                    .create();

    @Test
    public void testDeserialization() {
        String json = "{\n" +
                "  \"@class\":\"com.owlike.genson.reflect.UnknownPropertyHandlerTest$EvolvablePerson\",\n" +
                "  \"age\":50,\n" +
                "  \"name\":\"Homer\",\n" +
                "  \"spouse\":{\n" +
                "    \"@class\":\"com.owlike.genson.reflect.UnknownPropertyHandlerTest$EvolvablePerson\",\n" +
                "    \"age\":40,\n" +
                "    \"name\":\"Marge\"\n" +
                "  },\n" +
                "  \"children\":[\n" +
                "    \"Bart\",\n" +
                "    \"Lisa\",\n" +
                "    \"Maggie\"\n" +
                "  ],\n" +
                "  \"salary\":10000.0,\n" +
                "  \"donutLover\":true\n" +
                "}";

        EvolvablePerson homer = GENSON.deserialize(json, EvolvablePerson.class);
        assertEquals("Homer", homer.name);
        assertEquals(50, homer.age);
        assertEquals(Arrays.asList("Bart", "Lisa", "Maggie"), homer.unknownProperties.get("children"));
        assertEquals(10_000d, homer.unknownProperties.get("salary"));
        assertEquals(true, homer.unknownProperties.get("donutLover"));
    }

    @Test
    public void testCtorDeserialization() {
        String json = "{\n" +
                "  \"@class\":\"com.owlike.genson.reflect.UnknownPropertyHandlerTest$CtorEvolvablePerson\",\n" +
                "  \"age\":50,\n" +
                "  \"name\":\"Homer\",\n" +
                "  \"spouse\":{\n" +
                "    \"@class\":\"com.owlike.genson.reflect.UnknownPropertyHandlerTest$CtorEvolvablePerson\",\n" +
                "    \"age\":40,\n" +
                "    \"name\":\"Marge\"\n" +
                "  },\n" +
                "  \"children\":[\n" +
                "    \"Bart\",\n" +
                "    \"Lisa\",\n" +
                "    \"Maggie\"\n" +
                "  ],\n" +
                "  \"salary\":10000.0,\n" +
                "  \"donutLover\":true\n" +
                "}";

        EvolvablePerson homer = GENSON.deserialize(json, CtorEvolvablePerson.class);
        assertEquals("Homer", homer.name);
        assertEquals(50, homer.age);
        assertEquals(Arrays.asList("Bart", "Lisa", "Maggie"), homer.unknownProperties.get("children"));
        assertEquals(10_000d, homer.unknownProperties.get("salary"));
        assertEquals(true, homer.unknownProperties.get("donutLover"));
    }

    @Test
    public void testRoundTrip() {
        EvolvablePerson homer = new EvolvablePerson("Homer", 50);
        homer.unknownProperties().put("spouse", new EvolvablePerson("Marge", 40));
        homer.unknownProperties().put("children", Arrays.asList("Bart", "Lisa", "Maggie"));
        homer.unknownProperties().put("salary", 10_000d);
        homer.unknownProperties().put("donutLover", true);

        String json = GENSON.serialize(homer);
        EvolvablePerson homer2 = GENSON.deserialize(json, EvolvablePerson.class);

        assertEquals(homer, homer2);
    }

    interface Evolvable {
        Map<String, Object> unknownProperties();
    }

    static class EvolvableHandler implements UnknownPropertyHandler {

        @Override
        public void onUnknownProperty(Object target, String propName, Object propValue) {
            if (target instanceof Evolvable) {
                ((Evolvable) target).unknownProperties().put(propName, propValue);
            }
        }

        @Override
        public Map<String, Object> getUnknownProperties(Object source) {
            return source instanceof Evolvable
                    ? ((Evolvable) source).unknownProperties()
                    : null;
        }
    }

    static class EvolvablePerson implements Evolvable {
        private Map<String, Object> unknownProperties = new LinkedHashMap<String, Object>();
        private String name;
        private int age;

        public EvolvablePerson() {
        }

        public EvolvablePerson(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public Map<String, Object> unknownProperties() {
            return unknownProperties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EvolvablePerson that = (EvolvablePerson) o;
            return age == that.age &&
                    Objects.equals(unknownProperties, that.unknownProperties) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unknownProperties, name, age);
        }

        @Override
        public String toString() {
            return "EvolvablePerson{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", unknownProperties=" + unknownProperties +
                    '}';
        }
    }

    static class CtorEvolvablePerson extends EvolvablePerson {
        private CtorEvolvablePerson() {
            throw new RuntimeException("shouldn't be called");
        }

        @JsonCreator
        public CtorEvolvablePerson(String name, int age) {
            super(name, age);
        }
    }
}
