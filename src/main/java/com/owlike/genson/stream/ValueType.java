package com.owlike.genson.stream;

import java.util.Map;

public enum ValueType {
	ARRAY(Object[].class),
	OBJECT(Map.class),
	STRING(String.class),
	INTEGER(Long.class),
	DOUBLE(Double.class),
	BOOLEAN(Boolean.class),
	NULL(null);
	
	private final Class<?> clazz;
	ValueType(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	public Class<?> toClass() {
		return clazz;
	}
}
