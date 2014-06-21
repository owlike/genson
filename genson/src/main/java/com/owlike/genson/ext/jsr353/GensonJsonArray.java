package com.owlike.genson.ext.jsr353;

import javax.json.*;
import java.util.*;

class GensonJsonArray extends AbstractList<JsonValue> implements JsonArray {
  private final List<JsonValue> values;

  GensonJsonArray(List<JsonValue> values) {
    this.values = values;
  }

  @Override
  public JsonObject getJsonObject(int index) {
    return JsonObject.class.cast(values.get(index));
  }

  @Override
  public JsonArray getJsonArray(int index) {
    return JsonArray.class.cast(values.get(index));
  }

  @Override
  public JsonNumber getJsonNumber(int index) {
    return JsonNumber.class.cast(values.get(index));
  }

  @Override
  public JsonString getJsonString(int index) {
    return JsonString.class.cast(values.get(index));
  }

  @Override
  public <T extends JsonValue> List<T> getValuesAs(Class<T> clazz) {
    return (List<T>) values;
  }

  @Override
  public String getString(int index) {
    return getJsonString(index).getString();
  }

  @Override
  public String getString(int index, String defaultValue) {
    if (isNull(index)) return defaultValue;
    return getString(index);
  }

  @Override
  public int getInt(int index) {
    return getJsonNumber(index).intValue();
  }

  @Override
  public int getInt(int index, int defaultValue) {
    if (isNull(index)) return defaultValue;
    return getInt(index);
  }

  @Override
  public boolean getBoolean(int index) {
    JsonValue value = values.get(index);
    if (JsonValue.TRUE.equals(value)) return true;
    if (JsonValue.FALSE.equals(value)) return false;
    throw new ClassCastException();
  }

  @Override
  public boolean getBoolean(int index, boolean defaultValue) {
    if (isNull(index)) return defaultValue;
    return getBoolean(index);
  }

  @Override
  public boolean isNull(int index) {
    return JsonValue.NULL.equals(values.get(index));
  }

  @Override
  public JsonValue get(int index) {
    return values.get(index);
  }

  @Override
  public int size() {
    return values.size();
  }

  @Override
  public ValueType getValueType() {
    return ValueType.ARRAY;
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
