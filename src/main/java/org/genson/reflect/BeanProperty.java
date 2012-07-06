package org.genson.reflect;

import java.lang.reflect.Type;

/**
 * 
 * @author eugen
 *
 * @param <T> the type from which this BeanProperty is property
 */
public abstract class BeanProperty<T> {
	protected final String name;
	protected final Type type;
	protected final Class<T> declaringClass;
	
	protected BeanProperty(String name, Type type, Class<T> declaringClass) {
		super();
		this.name = name;
		this.type = type;
		this.declaringClass = declaringClass;
	}
	
	/**
	 * 
	 * @return The class in which this property is declared
	 */
	public Class<T> getDeclaringClass() {
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
	
	/**
	 * Used to give priority to implementations, for example
	 * by default a method would have a higher priority than a field because it can do some logic.
	 * The Greater the priority value is the more important is this BeanProperty. 
	 * @return the priority of this BeanProperty
	 */
	public abstract int priority();
	
	public abstract String signature();
}
