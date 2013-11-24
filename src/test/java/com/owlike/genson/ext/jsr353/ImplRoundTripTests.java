
package com.owlike.genson.ext.jsr353;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class ImplRoundTripTests {
    @Test public void testComplexStructure() {

        JsonObject expected = Json.createObjectBuilder()
                .add("k1", 1)
                .add("k2", false)
                .add("k3", true)
                .add("k4",
                        Json.createArrayBuilder()
                                .add(3.2e-33)
                ).addNull("k5")
                .add("k6",
                        Json.createObjectBuilder()
                ).add("k7",
                    Json.createObjectBuilder()
                            .add("k1", true)
                            .add("k2", "oooo")
                            .add("k3", "!")
                            .addNull("k4")
                ).add("array",
                    Json.createArrayBuilder()
                            .add(Json.createObjectBuilder())
                            .add(Json.createObjectBuilder())
                            .add(Json.createObjectBuilder())
                ).build();

        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.writeObject(expected);
        writer.close();


        JsonObject actual = Json.createReader(new StringReader(sw.toString())).readObject();

        assertEquals(expected, actual);
    }
}
