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
     * @param propName  the name of the unknown property
     * @param reader    the ObjectReader to read property value from
     * @param ctx       deserialization context
     *
     * @return the Consumer that will be called with the target bean,
     *         once the target bean is known
     */
    <T> Consumer<T> readUnknownProperty(String propName, ObjectReader reader, Context ctx);

    /**
     * Write unknown properties encountered during deserialization.
     * <p>
     * This method can be optionally implemented by {@code UnknownPropertyHandler}s
     * that want to write unknown properties during serialization. The default
     * implementation is a no-op.
     *
     * @param bean    the object we are serializing into JSON
     * @param writer  the ObjectReader to read property value from
     * @param ctx     serialization context
     */
    default <T> void writeUnknownProperties(T bean, ObjectWriter writer, Context ctx) {
    }
}
