package com.owlike.genson.ext.jsr353;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;

public class JsonValueTest {
    private final Genson genson = new Genson.Builder().with(new JSR353Bundle()).create();

    @Test public void testSerArrayOfLiterals() throws TransformationException, IOException {
        String json =
                genson.serialize(JSR353Bundle.factory.createArrayBuilder().addNull().add(1.22)
                        .add(false).add("str").build());
        assertEquals("[null,1.22,false,\"str\"]", json);
    }

    @Test public void testSerObjectAndArray() throws TransformationException, IOException {
        String json =
                genson.serialize(JSR353Bundle.factory.createObjectBuilder().add("int", 98)
                        .addNull("null")
                        .add("array", JSR353Bundle.factory.createArrayBuilder().build()).build());
        assertEquals("{\"int\":98,\"null\":null,\"array\":[]}", json);
    }
}
