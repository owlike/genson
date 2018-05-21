package com.owlike.genson.reflect;

import com.owlike.genson.Context;
import com.owlike.genson.GenericType;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import javax.json.JsonValue;
import java.util.Map;
import java.util.function.Consumer;

/**
 * An implementation of an {@link UnknownPropertyHandler} that supports
 * evolution of data classes via {@link Evolvable} interface.
 * <p>
 * If the target object we are deserializing into is {@link Evolvable},
 * this handler will add any unknown properties encountered during
 * deserialization into {@link Evolvable#unknownProperties()} map,
 * and will write them out along with all known properties during
 * subsequent serialization.
 * <p>
 * This prevents data loss when serializing and deserializing the same
 * JSON payload using different versions of Java data classes.
 *
 * @author Aleksandar Seovic  2018.05.20
 */
public class EvolvableHandler implements UnknownPropertyHandler {
    private static final GenericType<JsonValue> UNKNOWN = new GenericType<JsonValue>() {};

    @Override
    public <T> Consumer<T> onUnknownProperty(T target, String propName, ObjectReader reader, Context ctx) {
        // TODO: change this to read property as an opaque value, using ObjectReader directly
        Object propValue = ctx.genson.deserialize(UNKNOWN, reader, ctx);

        if (target == null) {
            // this is a bit ugly...
            // the issue is that we may not have a target object while parsing JSON when using creators,
            // so we need to store the parsed value somewhere and apply it later
            return objTarget -> {
                if (objTarget instanceof Evolvable) {
                    ((Evolvable) objTarget).addUnknownProperty(propName, propValue);
                }
            };
        }

        if (target instanceof Evolvable) {
            ((Evolvable) target).addUnknownProperty(propName, propValue);
        }
        return null;
    }

    @Override
    public <T> void writeUnknownProperties(T source, ObjectWriter writer, Context ctx) {
        if (source instanceof Evolvable) {
            Map<String, Object> props = ((Evolvable) source).unknownProperties();
            if (props != null) {
                for (String propName : props.keySet()) {
                    writer.writeName(propName);
                    // TODO: change this to write property as an opaque value, using ObjectWriter directly
                    ctx.genson.serialize(props.get(propName), writer, ctx);
                }
            }
        }
    }
}