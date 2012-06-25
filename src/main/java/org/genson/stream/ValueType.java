package org.genson.stream;

import java.util.List;

public enum ValueType {
	ARRAY(List.class),
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
