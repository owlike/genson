package com.owlike.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.owlike.genson.JsonBindingException;
import com.owlike.genson.Wrapper;

public abstract class BeanCreator extends Wrapper<AnnotatedElement> implements Comparable<BeanCreator> {
	// The type of object it can create
	protected final Class<?> ofClass;
	protected final Map<String, BeanCreatorProperty> parameters;

	public BeanCreator(Class<?> ofClass, String[] parameterNames, Type[] types, Annotation[][] anns) {
		this.ofClass = ofClass;
		this.parameters = new HashMap<String, BeanCreatorProperty>(parameterNames.length);
		for (int i = 0; i < parameterNames.length; i++) {
			this.parameters.put(parameterNames[i], new BeanCreatorProperty(parameterNames[i],
					types[i], i, anns[i], ofClass, this));
		}
	}

	public int contains(List<String> properties) {
		int cnt = 0;
		for (String prop : properties)
			if (parameters.containsKey(prop)) cnt++;
		return cnt;
	}

	public int compareTo(BeanCreator o) {
		int comp = o.priority() - priority();
		return comp != 0 ? comp : parameters.size() - o.parameters.size();
	}

	public abstract Object create(Object... args);

	protected abstract String signature();

	public abstract int priority();

	protected JsonBindingException couldNotCreate(Exception e) {
		return new JsonBindingException("Could not create bean of type " + ofClass.getName()
				+ " using creator " + signature(), e);
	}

	public static class ConstructorBeanCreator extends BeanCreator {
		protected final Constructor<?> constructor;

		public ConstructorBeanCreator(Class<?> ofClass, Constructor<?> constructor,
				String[] parameterNames, Type[] expandedParameterTypes) {
			super(ofClass, parameterNames, expandedParameterTypes, constructor
					.getParameterAnnotations());
			this.constructor = constructor;
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			decorate(constructor);
		}

		public Object create(Object... args) {
			try {
				return constructor.newInstance(args);
			} catch (IllegalArgumentException e) {
				throw couldNotCreate(e);
			} catch (InstantiationException e) {
				throw couldNotCreate(e);
			} catch (IllegalAccessException e) {
				throw couldNotCreate(e);
			} catch (InvocationTargetException e) {
				throw couldNotCreate(e);
			}
		}

		@Override
		protected String signature() {
			return constructor.toGenericString();
		}

		@Override
		public int priority() {
			return 50;
		}
	}

	public static class MethodBeanCreator extends BeanCreator {
		protected final Method _creator;

		public MethodBeanCreator(Method method, String[] parameterNames,
				Type[] expandedParameterTypes) {
			super(method.getReturnType(), parameterNames, expandedParameterTypes, method
					.getParameterAnnotations());
			if (!Modifier.isStatic(method.getModifiers()))
				throw new IllegalStateException("Only static methods can be used as creators!");
			this._creator = method;
			if (!_creator.isAccessible()) {
				_creator.setAccessible(true);
			}
			decorate(_creator);
		}

		public Object create(Object... args) {
			try {
				// we will handle only static method creators
				return ofClass.cast(_creator.invoke(null, args));
			} catch (IllegalArgumentException e) {
				throw couldNotCreate(e);
			} catch (IllegalAccessException e) {
				throw couldNotCreate(e);
			} catch (InvocationTargetException e) {
				throw couldNotCreate(e);
			}
		}

		@Override
		protected String signature() {
			return _creator.toGenericString();
		}

		@Override
		public int priority() {
			return 100;
		}
	}

	public static class BeanCreatorProperty extends PropertyMutator {
		protected final int index;
		protected final Annotation[] annotations;
		protected final BeanCreator creator;
		protected final boolean doThrowMutateException;

		protected BeanCreatorProperty(String name, Type type, int index, Annotation[] annotations,
				Class<?> declaringClass, BeanCreator creator) {
			this(name, type, index, annotations, declaringClass, creator, false);
		}

		protected BeanCreatorProperty(String name, Type type, int index, Annotation[] annotations,
				Class<?> declaringClass, BeanCreator creator, boolean doThrowMutateException) {
			super(name, type, declaringClass, annotations);
			this.index = index;
			this.annotations = annotations;
			this.creator = creator;
			this.doThrowMutateException = doThrowMutateException;
		}

		public int getIndex() {
			return index;
		}

		public Annotation[] getAnnotations() {
			return annotations;
		}

		@Override
		public int priority() {
			return -1000;
		}

		@Override
		public String signature() {
			return new StringBuilder(type.toString()).append(' ').append(name).append(" from ")
					.append(creator.signature()).toString();
		}

		@Override
		public void mutate(Object target, Object value) {
			if (doThrowMutateException) {
				throw new IllegalStateException(
						"Method mutate should not be called on a mutator of type "
								+ getClass().getName()
								+ ", this property exists only as constructor parameter!");
			}
		}

	}
}
