package com.owlike.genson.stream;

public enum ValueType {
	ARRAY(Object[].class),
	OBJECT(Object.class),
	STRING(String.class),
	INTEGER(Integer.class),
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
