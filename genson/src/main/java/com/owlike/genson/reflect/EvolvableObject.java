package com.owlike.genson.reflect;

import com.owlike.genson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience base class for {@link Evolvable} data classes.
 *
 * @author Aleksandar Seovic  2018.05.20
 */
public abstract class EvolvableObject implements Evolvable {
    @JsonIgnore
    private Map<String, Object> unknownProperties;

    @Override
    public void addUnknownProperty(String propName, Object propValue) {
        if (unknownProperties == null) {
            unknownProperties = new HashMap<>();
        }
        unknownProperties.put(propName, propValue);
    }

    @Override
    public Map<String, Object> unknownProperties() {
        return unknownProperties;
    }
}
