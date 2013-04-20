package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * 
 * @author eugen
 */
public abstract class BeanProperty {
	protected final String name;
	protected final Type type;
	protected final Class<?> declaringClass;
	protected final Annotation[] annotations;
	
	protected BeanProperty(String name, Type type, Class<?> declaringClass, Annotation[] annotations) {
		this.name = name;
		this.type = type;
		this.declaringClass = declaringClass;
		this.annotations = annotations;
	}
	
	/**
	 * 
	 * @return The class in which this property is declared
	 */
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}
	
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return the type of the property
	 */
	public Type getType() {
		return type;
	}
	
	public Class<?> getRawClass() {
		return TypeUtil.getRawClass(type);
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for (Annotation ann : annotations) 
			if (annotationClass.isInstance(ann))
				return annotationClass.cast(ann);
		return null;
	}
	
	/**
	 * Used to give priority to implementations, for example
	 * by default a method would have a higher priority than a field because it can do some logic.
	 * The greater the priority value is the more important is this BeanProperty. 
	 * @return the priority of this BeanProperty
	 */
	abstract int priority();
	
	abstract String signature();
}
