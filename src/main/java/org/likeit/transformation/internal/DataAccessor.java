package org.likeit.transformation.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.likeit.transformation.TransformationException;

public class DataAccessor {
	private Method method;
	private String name;
	
	public final static String GET_PREFIX = "get";
	public final static String IS_PREFIX = "is";
	public final static String SET_PREFIX = "set";
	
	public DataAccessor(Method method) {
		name = method.getName();
		int length = -1;
		
		if ( name.startsWith(GET_PREFIX) ) length = GET_PREFIX.length();
		else if ( name.startsWith(IS_PREFIX) ) length = IS_PREFIX.length();
		else if ( name.startsWith(SET_PREFIX) ) length = SET_PREFIX.length();
		else throw new IllegalArgumentException("Method must begin with getXXX, isXXX and return a boolean type or set.");
		
		String propName = method.getName().substring(length+1);
		this.name = Character.toLowerCase(name.charAt(length)) + propName;
		this.method = method;
	}
	
	public String getName() {
		return name;
	}
	
	
	public Object access(Object target, Object...args) throws TransformationException {
		try {
			return method.invoke(target, args);
		} catch (IllegalArgumentException e) {
			throw new TransformationException(e);
		} catch (IllegalAccessException e) {
			throw new TransformationException(e);
		} catch (InvocationTargetException e) {
			throw new TransformationException(e);
		}
	}

	public Type getParameterType() {
		return method.getGenericParameterTypes()[0];
	}
	
	public Type getReturnType() {
		return method.getGenericReturnType();
	}
}