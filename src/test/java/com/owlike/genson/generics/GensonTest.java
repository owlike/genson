package com.owlike.genson.generics;

import com.owlike.genson.Genson;
import com.owlike.genson.annotation.JsonProperty;
import com.owlike.genson.reflect.TypeUtil;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * artifactId: genson
 * <p/>
 * version: 0.94 & 0.95 
 * Issue 4, Reported by jesper.hammarback
 * http://code.google.com/p/genson/issues/detail?id=4
 */
public class GensonTest {

    @Test
    public void testTypeExpansionCaching() throws SecurityException, NoSuchFieldException {
        Type typeOfFoosField = FooContainer.class.getDeclaredField("foos").getGenericType();
        Type typeOfBarsField = BarContainer.class.getDeclaredField("bars").getGenericType();
        Type typeOfDataField = FooBarContainer.class.getDeclaredField("data").getGenericType();

        ParameterizedType expandedTypeOfFoosDataField = (ParameterizedType) TypeUtil.expandType(typeOfDataField,
                typeOfFoosField);
        ParameterizedType expandedTypeOfBarsDataField = (ParameterizedType) TypeUtil.expandType(typeOfDataField,
                typeOfBarsField);

        assertEquals(List.class, expandedTypeOfFoosDataField.getRawType());
        assertEquals(Foo.class, expandedTypeOfFoosDataField.getActualTypeArguments()[0]);

        // testing that those expanded types are considered as distinct in the cache
        assertEquals(List.class, expandedTypeOfBarsDataField.getRawType());
        assertEquals(Bar.class, expandedTypeOfBarsDataField.getActualTypeArguments()[0]);
    }

    @Test
    public void testFooBar() throws Exception {
        Genson genson = new Genson.Builder().setSkipNull(true).create();

        FooContainer fooContainer = new FooContainer(new FooBarContainer<Foo>(Arrays.asList(new Foo("foo"))));
        BarContainer barContainer = new BarContainer(new FooBarContainer<Bar>(Arrays.asList(new Bar("bar"))));

        assertThat(genson.serialize(fooContainer), is("{\"foos\":{\"data\":[{\"fooId\":\"foo\"}]}}"));
        assertThat(genson.serialize(barContainer), is("{\"bars\":{\"data\":[{\"barId\":\"bar\"}]}}"));
    }

    public static class FooContainer {
        public final FooBarContainer<Foo> foos;

        public FooContainer(@JsonProperty("foos") FooBarContainer<Foo> foos) {
            this.foos = foos;
        }
    }

    public static class BarContainer {
        public final FooBarContainer<Bar> bars;

        public BarContainer(@JsonProperty("bars") FooBarContainer<Bar> bars) {
            this.bars = bars;
        }
    }

    public static class FooBarContainer<T> {
        public final List<T> data;

        public FooBarContainer(@JsonProperty("data") List<T> data) {
            this.data = data;
        }
    }

    public static class Foo {
        public final String fooId;

        public Foo(@JsonProperty("fooId") String fooId) {
            this.fooId = fooId;
        }
    }

    public static class Bar {
        public final String barId;

        public Bar(@JsonProperty("barId") String barId) {
            this.barId = barId;
        }
    }

}
