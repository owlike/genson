package org.genson.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.genson.TransformationRuntimeException;


public abstract class PropertyMutator extends BeanProperty implements Comparable<PropertyMutator> {
	
	protected PropertyMutator(String name, Type type, Class<?> declaringClass) {
		super(name, type, declaringClass);
	}
	
	public abstract void mutate(Object target, Object value);

	@Override
	public int compareTo(PropertyMutator o) {
		return o.priority() - priority();
	}
	
	protected TransformationRuntimeException couldNotMutate(Exception e) {
		return new TransformationRuntimeException("Could not mutate value of property named '"
				+ name + "' using mutator " + signature(), e);
	}
	
	public static class MethodMutator extends PropertyMutator {
		protected final Method _setter;

		public MethodMutator(String name, Method setter, Class<?> declaringClass) {
			super(name, TypeUtil.expandType(setter.getGenericParameterTypes()[0], declaringClass), declaringClass);
			this._setter = setter;
			if (!_setter.isAccessible()) {
				_setter.setAccessible(true);
			}
		}

		@Override
		public void mutate(Object target, Object value) {
			try {
				_setter.invoke(target, value);
			} catch (IllegalArgumentException e) {
				throw couldNotMutate(e);
			} catch (IllegalAccessException e) {
				throw couldNotMutate(e);
			} catch (InvocationTargetException e) {
				throw couldNotMutate(e);
			}
		}

		@Override
		public String signature() {
			return _setter.toGenericString();
		}

		@Override
		public int priority() {
			return 100;
		}
	}

	public static class FieldMutator extends PropertyMutator {
		protected final Field _field;

		public FieldMutator(String name, Field field, Class<?> declaringClass) {
			super(name, TypeUtil.expandType(field.getGenericType(), declaringClass), declaringClass);
			this._field = field;
			if (!_field.isAccessible()) {
				_field.setAccessible(true);
			}
		}

		@Override
		public void mutate(Object target, Object value) {
			try {
				_field.set(target, value);
			} catch (IllegalArgumentException e) {
				throw couldNotMutate(e);
			} catch (IllegalAccessException e) {
				throw couldNotMutate(e);
			}
		}

		@Override
		public String signature() {
			return _field.toGenericString();
		}

		@Override
		public int priority() {
			return 0;
		}
	}
}
