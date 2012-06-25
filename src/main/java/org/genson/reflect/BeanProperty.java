package org.genson.reflect;

import java.lang.reflect.Type;

public abstract class BeanProperty {
	protected final String name;
	protected final Type type;
	protected final Class<?> declaringClass;
	
	protected BeanProperty(String name, Type type, Class<?> declaringClass) {
		super();
		this.name = name;
		this.type = type;
		this.declaringClass = declaringClass;
	}
	
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}
	
	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}
	
	/**
	 * Used to give priority to implementations, for example
	 * by default a method would be prioritary to a field because it can do some logic.
	 * The Greater the priority value is the more important is this beanproperty. 
	 * @return
	 */
	public abstract int priority();
	
	public abstract String signature();
}
