package com.owlike.genson.reflect;

import com.owlike.genson.annotation.JsonIgnore;

import javax.json.JsonValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience base class for {@link Evolvable} data classes.
 *
 * @author Aleksandar Seovic  2018.05.20
 */
public abstract class EvolvableObject implements Evolvable {
    @JsonIgnore
    private Map<String, JsonValue> unknownProperties;

    @Override
    public void addUnknownProperty(String propName, JsonValue propValue) {
        if (unknownProperties == null) {
            unknownProperties = new HashMap<>();
        }
        unknownProperties.put(propName, propValue);
    }

    @Override
    public Map<String, JsonValue> unknownProperties() {
        return unknownProperties;
    }
}
