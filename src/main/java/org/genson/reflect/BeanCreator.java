package org.genson.reflect;

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

import org.genson.Deserializer;
import org.genson.TransformationException;
import org.genson.Wrapper;

public abstract class BeanCreator<T> extends Wrapper<AnnotatedElement> implements Comparable<BeanCreator<T>> {
	// The type of object it can create
	protected final Class<T> ofClass;
	protected final Map<String, BeanCreatorProperty<T, ?>> parameters;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BeanCreator(Class<T> ofClass, String[] parameterNames, Type[] types,
			Annotation[][] anns, Deserializer<?>[] propertiesDeserializers) {
		this.ofClass = ofClass;
		this.parameters = new HashMap<String, BeanCreatorProperty<T, ?>>(parameterNames.length);
		for (int i = 0; i < parameterNames.length; i++) {
			this.parameters.put(parameterNames[i], new BeanCreatorProperty(parameterNames[i],
					types[i], i, anns[i], ofClass, this, propertiesDeserializers[i]));
		}
	}

	public int contains(List<String> properties) {
		int cnt = 0;
		for (String prop : properties)
			if (parameters.containsKey(prop))
				cnt++;
		return cnt;
	}

	public int compareTo(BeanCreator<T> o) {
		int comp = o.priority() - priority();
		return comp != 0 ? comp : parameters.size() - o.parameters.size();
	}

	public abstract T create(Object... args) throws TransformationException;

	protected abstract String signature();

	public abstract int priority();

	protected TransformationException couldNotCreate(Exception e) {
		return new TransformationException("Could not create bean of type " + ofClass.getName()
				+ " using creator " + signature(), e);
	}

	public static class ConstructorBeanCreator<T> extends BeanCreator<T> {
		protected final Constructor<T> constructor;

		public ConstructorBeanCreator(Class<T> ofClass, Constructor<T> constructor,
				String[] parameterNames, Deserializer<?>[] propertiesDeserializers) {
			super(ofClass, parameterNames, constructor.getGenericParameterTypes(), constructor
					.getParameterAnnotations(), propertiesDeserializers);
			this.constructor = constructor;
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			decorate(constructor);
		}

		public T create(Object... args) throws TransformationException {
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

	public static class MethodBeanCreator<T> extends BeanCreator<T> {
		protected final Method _creator;

		@SuppressWarnings("unchecked")
		public MethodBeanCreator(Method method, String[] parameterNames,
				Deserializer<?>[] propertiesDeserializers) {
			super((Class<T>) method.getReturnType(), parameterNames, method
					.getGenericParameterTypes(), method.getParameterAnnotations(),
					propertiesDeserializers);
			if (!Modifier.isStatic(method.getModifiers()))
				throw new IllegalStateException("Only static methods can be used as creators!");
			this._creator = method;
			if (!_creator.isAccessible()) {
				_creator.setAccessible(true);
			}
			decorate(_creator);
		}

		public T create(Object... args) throws TransformationException {
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

	public static class BeanCreatorProperty<T, P> extends PropertyMutator<T, P> {
		protected final int index;
		protected final Annotation[] annotations;
		protected final BeanCreator<T> creator;
		protected final boolean doThrowMutateException;

		protected BeanCreatorProperty(String name, Type type, int index, Annotation[] annotations,
				Class<T> declaringClass, BeanCreator<T> creator,
				Deserializer<P> propertyDeserializer) {
			this(name, type, index, annotations, declaringClass, creator, propertyDeserializer, false);
		}
		
		protected BeanCreatorProperty(String name, Type type, int index, Annotation[] annotations,
				Class<T> declaringClass, BeanCreator<T> creator,
				Deserializer<P> propertyDeserializer, boolean doThrowMutateException) {
			super(name, type, declaringClass, propertyDeserializer);
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
		public void mutate(T target, P value) {
			if (doThrowMutateException) {
    			throw new IllegalStateException(
    					"Method mutate should not be called on a mutator of type "
    							+ getClass().getName()
    							+ ", this property exists only as constructor parameter!");
			}
		}

	}
}
