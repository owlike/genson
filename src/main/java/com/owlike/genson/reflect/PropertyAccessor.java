package com.owlike.genson.reflect;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.owlike.genson.Context;
import com.owlike.genson.Serializer;
import com.owlike.genson.TransformationException;
import com.owlike.genson.TransformationRuntimeException;
import com.owlike.genson.stream.ObjectWriter;

public abstract class PropertyAccessor<T, P> extends BeanProperty<T> implements
		Comparable<PropertyAccessor<T, ?>> {
	// package visibility for testing
	final Serializer<P> propertySerializer;

	protected PropertyAccessor(String name, Type type, Class<T> declaringClass,
			Serializer<P> propertySerializer) {
		super(name, type, declaringClass);
		this.propertySerializer = propertySerializer;
	}

	public void serialize(T propertySource, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		P propertyValue = access(propertySource);
		writer.writeName(name);
		propertySerializer.serialize(propertyValue, writer, ctx);
	}

	public abstract P access(final T target);

	public int compareTo(PropertyAccessor<T, ?> o) {
		return o.priority() - priority();
	}

	protected TransformationRuntimeException couldNotAccess(Exception e) {
		return new TransformationRuntimeException("Could not access value of property named '"
				+ name + "' using accessor " + signature() + " from class "
				+ declaringClass.getName(), e);
	}

	public static class MethodAccessor<T, P> extends PropertyAccessor<T, P> {
		protected final Method _getter;

		public MethodAccessor(String name, Method getter, Class<T> declaringClass,
				Serializer<P> propertySerializer) {
			this(name, getter, TypeUtil.expandType(getter.getGenericReturnType(), declaringClass),
					declaringClass, propertySerializer);
		}

		public MethodAccessor(String name, Method getter, Type type, Class<T> declaringClass,
				Serializer<P> propertySerializer) {
			super(name, type, declaringClass, propertySerializer);
			this._getter = getter;
			if (!_getter.isAccessible()) {
				_getter.setAccessible(true);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public P access(final T target) {
			try {
				return (P) _getter.invoke(target);
			} catch (IllegalArgumentException e) {
				throw couldNotAccess(e);
			} catch (IllegalAccessException e) {
				throw couldNotAccess(e);
			} catch (InvocationTargetException e) {
				throw couldNotAccess(e);
			}
		}

		@Override
		public String signature() {
			return _getter.toGenericString();
		}

		@Override
		public int priority() {
			return 100;
		}
	}

	public static class FieldAccessor<T, P> extends PropertyAccessor<T, P> {
		protected final Field _field;
		
		public FieldAccessor(String name, Field field, Class<T> declaringClass,
				Serializer<P> propertySerializer) {
			super(name, TypeUtil.expandType(field.getGenericType(), declaringClass),
					declaringClass, propertySerializer);
			this._field = field;
			if (!_field.isAccessible()) {
				_field.setAccessible(true);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public P access(final T target) {
			try {
				return (P) _field.get(target);
			} catch (IllegalArgumentException e) {
				throw couldNotAccess(e);
			} catch (IllegalAccessException e) {
				throw couldNotAccess(e);
			}
		}

		@Override
		public String signature() {
			return _field.toGenericString();
		}

		@Override
		public int priority() {
			return 50;
		}
	}
}
