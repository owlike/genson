package org.genson.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.genson.TransformationException;


public abstract class BeanCreator implements Comparable<BeanCreator> {
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
			if (parameters.containsKey(prop))
				cnt++;
		return cnt;
	}

	@Override
	public int compareTo(BeanCreator o) {
		int comp = o.priority() - priority();
		return comp != 0 ? comp : parameters.size() - o.parameters.size();
	}
	
	public abstract Object create(Object... args) throws TransformationException;

	protected abstract String signature();
	
	public abstract int priority();
	
	protected TransformationException couldNotCreate(Exception e) {
		return new TransformationException("Could not create bean of type "
				+ ofClass.getName() + " using creator " + signature(),
				e);
	}
	
	public static class ConstructorBeanCreator extends BeanCreator {
		protected final Constructor<?> constructor;
		
		public ConstructorBeanCreator(Class<?> ofClass, Constructor<?> constructor,
				String[] parameterNames) {
			super(ofClass, parameterNames, constructor.getGenericParameterTypes(), constructor.getParameterAnnotations());
			this.constructor = constructor;
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
		}
		
		public Object create(Object... args) throws TransformationException {
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
		
		public MethodBeanCreator(Class<?> ofClass, Method method,
				String[] parameterNames) {
			super(method.getReturnType(), parameterNames, method.getGenericParameterTypes(), method.getParameterAnnotations());
			if (!Modifier.isStatic(method.getModifiers()))
					throw new IllegalStateException("Only static methods can be used as creators!");
			this._creator = method;
			if (!_creator.isAccessible()) {
				_creator.setAccessible(true);
			}
		}
		
		public Object create(Object... args) throws TransformationException {
			try {
				// we will handle only static method creators
				return _creator.invoke(null, args);
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

		protected BeanCreatorProperty(String name, Type type, int index, Annotation[] annotations,
				Class<?> declaringClass, BeanCreator creator) {
			super(name, type, declaringClass);
			this.index = index;
			this.annotations = annotations;
			this.creator = creator;
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
			throw new IllegalStateException(
					"Method mutate should not be called on a mutator of type "
							+ getClass().getName()
							+ ", this property exists only as constructor parameter!");
		}

	}
}
