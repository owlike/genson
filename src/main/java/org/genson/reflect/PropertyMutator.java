package org.genson.reflect;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.genson.Context;
import org.genson.Deserializer;
import org.genson.TransformationException;
import org.genson.TransformationRuntimeException;
import org.genson.stream.ObjectReader;

public abstract class PropertyMutator<T, P> extends BeanProperty<T> implements
		Comparable<PropertyMutator<T, ?>> {
	// package visibility for testing
	final Deserializer<P> propertyDeserializer;

	protected PropertyMutator(String name, Type type, Class<T> declaringClass,
			Deserializer<P> propertyDeserializer) {
		super(name, type, declaringClass);
		this.propertyDeserializer = propertyDeserializer;
	}

	public P deserialize(ObjectReader reader, Context ctx) throws TransformationException,
			IOException {
		return propertyDeserializer.deserialize(reader, ctx);
	}

	public void deserialize(T into, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		P propValue = propertyDeserializer.deserialize(reader, ctx);
		mutate(into, propValue);
	}

	public abstract void mutate(T target, P value);

	@Override
	public int compareTo(PropertyMutator<T, ?> o) {
		return o.priority() - priority();
	}

	protected TransformationRuntimeException couldNotMutate(Exception e) {
		return new TransformationRuntimeException("Could not mutate value of property named '"
				+ name + "' using mutator " + signature(), e);
	}

	public static class MethodMutator<T, P> extends PropertyMutator<T, P> {
		protected final Method _setter;

		public MethodMutator(String name, Method setter, Class<T> declaringClass,
				Deserializer<P> propertyDeserializer) {
			this(name, setter, TypeUtil.expandType(setter.getGenericParameterTypes()[0],
					declaringClass), declaringClass, propertyDeserializer);
		}

		public MethodMutator(String name, Method setter, Type type, Class<T> declaringClass,
				Deserializer<P> propertyDeserializer) {
			super(name, type, declaringClass, propertyDeserializer);
			this._setter = setter;
			if (!_setter.isAccessible()) {
				_setter.setAccessible(true);
			}
		}

		@Override
		public void mutate(T target, P value) {
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

	public static class FieldMutator<T, P> extends PropertyMutator<T, P> {
		protected final Field _field;

		public FieldMutator(String name, Field field, Class<T> declaringClass,
				Deserializer<P> propertyDeserializer) {
			super(name, TypeUtil.expandType(field.getGenericType(), declaringClass),
					declaringClass, propertyDeserializer);
			this._field = field;
			if (!_field.isAccessible()) {
				_field.setAccessible(true);
			}
		}

		@Override
		public void mutate(T target, P value) {
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
