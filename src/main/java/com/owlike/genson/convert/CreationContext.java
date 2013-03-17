package com.owlike.genson.convert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public final class CreationContext {
	private final CreationContext parent;
	private final Type type;
	private final String name;
	private final Annotation[] annotations;
	private final String originalName;
	private final Class<?> declaringClass;

	public CreationContext(CreationContext parent, Type type, String name, String originalName, Annotation[] annotations,
			Class<?> declaringClass) {
		super();
		this.parent = parent;
		this.type = type;
		this.name = name;
		this.annotations = annotations;
		this.originalName = originalName;
		this.declaringClass = declaringClass;
	}

	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for (Annotation ann : annotations) 
			if (annotationClass.isInstance(ann))
				return annotationClass.cast(ann);
		return null;
	}
	
	public CreationContext getParent() {
		return parent;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOriginalName() {
		return originalName;
	}
	
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}
	
	// TODO move to class responsible of building the context and resolving the converter
	public static CreationContext newContext(String name, Type type, Field field, CreationContext parent) {
		return new CreationContext(parent, type, name, field.getName(), field.getDeclaredAnnotations(), field.getDeclaringClass());
	}
	
	public static CreationContext newContext(String name, Type type, Method method, CreationContext parent) {
		return new CreationContext(parent, type, name, method.getName(), method.getAnnotations(), method.getDeclaringClass());
	}
	
	public static CreationContext newContext(String name, Type type, Constructor<?> ctr, int index, CreationContext parent) {
		if (ctr.getParameterAnnotations().length <= index) throw new IllegalArgumentException();
		return new CreationContext(parent, type, name, name, ctr.getParameterAnnotations()[index], ctr.getDeclaringClass());
	}
	
	public static CreationContext newContext(String name, Type type, Method ctr, int index, CreationContext parent) {
		if (ctr.getParameterAnnotations().length <= index) throw new IllegalArgumentException();
		return new CreationContext(parent, type, name, name, ctr.getParameterAnnotations()[index], ctr.getDeclaringClass());
	}
}
