package com.owlike.genson.reflect;

import java.util.Map;

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
     * property value somewhere so it can be returned by the
     * {@link #getUnknownProperties} method.
     *
     * @param target    the object we are deserializing JSON into
     * @param propName  the name of the unknown property
     * @param propValue the value of the unknown property
     */
    void onUnknownProperty(Object target, String propName, Object propValue);

    /**
     * Return a map of unknown properties encountered during
     * deserialization, keyed by property name.
     *
     * @param source  the object we are serializing into JSON
     *
     * @return a map of unknown properties
     */
    Map<String, Object> getUnknownProperties(Object source);
}
