package com.owlike.genson.stream;

import java.util.List;
import java.util.Map;

public enum ValueType {
  ARRAY(List.class),
  OBJECT(Map.class),
  STRING(String.class),
  INTEGER(Long.class),
  DOUBLE(Double.class),
  BOOLEAN(Boolean.class),
  NULL(null);

  private Class<?> clazz;

  ValueType(Class<?> clazz) {
    this.clazz = clazz;
  }

  public void setDefaultClass(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Class<?> toClass() {
    return clazz;
  }
}
