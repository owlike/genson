package com.owlike.genson.reflect;

import com.owlike.genson.Context;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import java.util.function.Consumer;

/**
 * An interface that defines callbacks that will be called when an
 * unknown properties are encountered during deserialization, as well
 * as to check if there are any unknown properties that should be
 * written out during serialization.
 * <p>
 * The main purpose of this interface is to support schema evolution
 * of objects that use JSON as a long term storage format, without
 * loss of unknown properties across clients and severs using different
 * versions of Java classes.
 *
 * @author Aleksandar Seovic  2018.05.09
 */
public interface UnknownPropertyHandler {
    /**
     * Called whenever a property is encountered in a JSON document
     * that doesn't have a corresponding {@link PropertyMutator}.
     * <p>
     * Typically, the implementation of this interface concerned
     * with schema evolution will handle this event by storing
     * property value somewhere so it can be written later by the
     * {@link #writeUnknownProperties} method.
     *
     * @param target    the object we are deserializing JSON into, if known
     * @param propName  the name of the unknown property
     * @param reader    the ObjectReader to read property value from
     * @param ctx       deserialization context
     *
     * @return the optional Consumer that will be called once the target object is known
     */
    <T> Consumer<T> onUnknownProperty(T target, String propName, ObjectReader reader, Context ctx);

    /**
     * Write unknown properties encountered during deserialization.
     * <p>
     * This method can be optionally implemented by {@code UnknownPropertyHandler}s
     * that want to write unknown properties during serialization. The default
     * implementation is a no-op.
     *
     * @param source  the object we are serializing into JSON
     * @param writer  the ObjectReader to read property value from
     * @param ctx     serialization context
     *
     * @return a map of unknown properties
     */
    default <T> void writeUnknownProperties(T source, ObjectWriter writer, Context ctx) {
    }
}
