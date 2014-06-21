package com.owlike.genson.ext.jsr353;

import javax.json.*;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

class GensonJsonObject extends AbstractMap<String, JsonValue> implements JsonObject {
  private final Map<String, JsonValue> values;

  GensonJsonObject(Map<String, JsonValue> values) {
    this.values = values;
  }

  @Override
  public Set<Entry<String, JsonValue>> entrySet() {
    return values.entrySet();
  }

  @Override
  public JsonArray getJsonArray(String name) {
    return JsonArray.class.cast(values.get(name));
  }

  @Override
  public JsonObject getJsonObject(String name) {
    return JsonObject.class.cast(values.get(name));
  }

  @Override
  public JsonNumber getJsonNumber(String name) {
    return JsonNumber.class.cast(values.get(name));
  }

  @Override
  public JsonString getJsonString(String name) {
    return JsonString.class.cast(values.get(name));
  }

  @Override
  public String getString(String name) {
    return getJsonString(name).getString();
  }

  @Override
  public String getString(String name, String defaultValue) {
    if (isNull(name)) return defaultValue;
    return getString(name);
  }

  @Override
  public int getInt(String name) {
    return getJsonNumber(name).intValue();
  }

  @Override
  public int getInt(String name, int defaultValue) {
    if (isNull(name)) return defaultValue;
    return getInt(name);
  }

  @Override
  public boolean getBoolean(String name) {
    JsonValue value = values.get(name);
    if (JsonValue.TRUE.equals(value)) return true;
    if (JsonValue.FALSE.equals(value)) return false;
    throw new ClassCastException();
  }

  @Override
  public boolean getBoolean(String name, boolean defaultValue) {
    if (isNull(name)) return defaultValue;
    return getBoolean(name);
  }

  @Override
  public boolean isNull(String name) {
    JsonValue value = values.get(name);
    return (JsonValue.NULL.equals(value) || value == null);
  }

  @Override
  public ValueType getValueType() {
    return ValueType.OBJECT;
  }


  @Override
  public int hashCode() {
    return values.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return values.equals(o);
  }

  @Override
  public String toString() {
    return JSR353Bundle.toString(this);
  }
}
