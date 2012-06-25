package org.genson.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.genson.TransformationRuntimeException;


public abstract class PropertyAccessor extends BeanProperty implements Comparable<PropertyAccessor> {
	protected PropertyAccessor(String name, Type type, Class<?> declaringClass) {
		super(name, type, declaringClass);
	}

	public abstract Object access(final Object target);

	@Override
	public int compareTo(PropertyAccessor o) {
		return o.priority() - priority();
	}
	
	protected TransformationRuntimeException couldNotAccess(Exception e) {
		return new TransformationRuntimeException("Could not access value of property named '"
				+ name + "' using accessor " + signature() + " from class "
				+ declaringClass.getName(), e);
	}

	public static class MethodAccessor extends PropertyAccessor {
		protected final Method _getter;

		public MethodAccessor(String name, Method getter, Class<?> declaringClass) {
			super(name, TypeUtil.expandType(getter.getGenericReturnType(), declaringClass), declaringClass);
			this._getter = getter;
			if (!_getter.isAccessible()) {
				_getter.setAccessible(true);
			}
		}

		@Override
		public Object access(final Object target) {
			try {
				return _getter.invoke(target);
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

	public static class FieldAccessor extends PropertyAccessor {
		protected final Field _field;

		public FieldAccessor(String name, Field field, Class<?> declaringClass) {
			super(name, TypeUtil.expandType(field.getGenericType(), declaringClass), declaringClass);
			this._field = field;
			if (!_field.isAccessible()) {
				_field.setAccessible(true);
			}
		}

		@Override
		public Object access(final Object target) {
			try {
				return _field.get(target);
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
