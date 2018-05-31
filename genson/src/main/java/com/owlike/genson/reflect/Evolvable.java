package com.owlike.genson.reflect;

import javax.json.JsonValue;
import java.util.Map;

/**
 * An interface that can be implemented by data classes
 * in order to support schema evolution.
 * <p>
 * This interface is used in combination with {@link EvolvableHandler}
 * in order to prevent data loss during serialization across different
 * versions of data classes.
 *
 * @author Aleksandar Seovic  2018.05.20
 */
interface Evolvable {
    /**
     * Add unknown property to this instance.
     *
     * @param propName  property name
     * @param propValue property value
     */
    void addUnknownProperty(String propName, JsonValue propValue);

    /**
     * Return a map of unknown properties.
     *
     * @return a map of unknown properties
     */
    Map<String, JsonValue> unknownProperties();
}
