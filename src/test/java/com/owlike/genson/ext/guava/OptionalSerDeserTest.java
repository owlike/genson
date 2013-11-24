package com.owlike.genson.ext.guava;

import com.google.common.base.Optional;
import com.owlike.genson.GenericType;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OptionalSerDeserTest {
    private Genson genson = new Genson.Builder().with(new GuavaBundle()).create();

    @Test public void roundTripListOfOptionals() throws IOException, TransformationException {
        List<Optional<String>> expected = Arrays.asList(Optional.<String>absent(), Optional.fromNullable("hey"), Optional.of("you"));
        GenericType<List<Optional<String>>> type = new GenericType<List<Optional<String>>>() {};
        String json = genson.serialize(expected, type);
        List<Optional<String>> actual = genson.deserialize(json, type);
        assertEquals(expected, actual);
    }

    @Test public void roundTripPojoWithOptionals() throws IOException, TransformationException {
        Pojo expected = new Pojo(Optional.of(1), Optional.<Pojo>absent(), "foo bar");
        String json = genson.serialize(expected);
        Pojo actual = genson.deserialize(json, Pojo.class);

        assertEquals(expected, actual);
    }

    public static class Pojo {
        public Optional<Integer> optInt;
        public Optional<Pojo> optPojo;
        public String other;

        public Pojo() {}

        public Pojo(Optional<Integer> optInt, Optional<Pojo> optPojo, String other) {
            this.optInt = optInt;
            this.optPojo = optPojo;
            this.other = other;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pojo pojo = (Pojo) o;

            if (optInt != null ? !optInt.equals(pojo.optInt) : pojo.optInt != null) return false;
            if (optPojo != null ? !optPojo.equals(pojo.optPojo) : pojo.optPojo != null) return false;
            if (other != null ? !other.equals(pojo.other) : pojo.other != null) return false;

            return true;
        }
    }
}
