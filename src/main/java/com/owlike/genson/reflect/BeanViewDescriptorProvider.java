package com.owlike.genson.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.owlike.genson.Trilean.FALSE;
import static com.owlike.genson.Trilean.TRUE;
import static com.owlike.genson.reflect.TypeUtil.*;

import com.owlike.genson.BeanView;
import com.owlike.genson.Deserializer;
import com.owlike.genson.Genson;
import com.owlike.genson.Serializer;
import com.owlike.genson.TransformationRuntimeException;
import com.owlike.genson.Trilean;
import com.owlike.genson.annotation.Creator;
import com.owlike.genson.reflect.PropertyAccessor.MethodAccessor;
import com.owlike.genson.reflect.PropertyMutator.MethodMutator;

/**
 * This class constructs BeanDescriptor for the {@link com.owlike.genson.BeanView BeanView}
 * mechanism. You should not extend it as it may change in the future.
 * 
 * @author eugen
 * 
 */
public class BeanViewDescriptorProvider extends BaseBeanDescriptorProvider {
	private Map<Class<?>, BeanView<?>> views = new HashMap<Class<?>, BeanView<?>>();
	private Map<Class<?>, BeanDescriptor<?>> descriptors = new ConcurrentHashMap<Class<?>, BeanDescriptor<?>>();

	public BeanViewDescriptorProvider(BeanMutatorAccessorResolver mutatorAccessorResolver,
			PropertyNameResolver nameResolver) {
		super(mutatorAccessorResolver, nameResolver, true, false, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> BeanDescriptor<T> provide(Class<T> ofClass, Type ofType, Genson genson) {
		Class<?> rawClass = getRawClass(ofType);
		if (!BeanView.class.isAssignableFrom(rawClass))
			throw new IllegalArgumentException("Expected argument of type "
					+ BeanView.class.getName() + " but provided " + rawClass);

		BeanDescriptor<T> descriptor = (BeanDescriptor<T>) descriptors.get(rawClass);
		if (descriptor == null) {
			Class<?> parameterizedTypeForBeanView = getRawClass(expandType(
					BeanView.class.getTypeParameters()[0], ofType));
			if (!ofClass.isAssignableFrom(parameterizedTypeForBeanView)) {
				throw new IllegalArgumentException("Expected type for ofClass parameter is "
						+ parameterizedTypeForBeanView + " but provided is " + ofClass);
			}

			try {
				Constructor<BeanView<T>> ctr = (Constructor<BeanView<T>>) rawClass
						.getDeclaredConstructor();
				if (!ctr.isAccessible()) ctr.setAccessible(true);
				views.put(rawClass, ctr.newInstance());
				descriptor = super.provide(ofClass, ofType, genson);
				descriptors.put(rawClass, descriptor);
			} catch (SecurityException e) {
				throw couldNotInstantiateBeanView(ofClass, e);
			} catch (NoSuchMethodException e) {
				throw couldNotInstantiateBeanView(ofClass, e);
			} catch (IllegalArgumentException e) {
				throw couldNotInstantiateBeanView(ofClass, e);
			} catch (InstantiationException e) {
				throw couldNotInstantiateBeanView(ofClass, e);
			} catch (IllegalAccessException e) {
				throw couldNotInstantiateBeanView(ofClass, e);
			} catch (InvocationTargetException e) {
				throw couldNotInstantiateBeanView(ofClass, e);
			}
		}
		return descriptor;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected <T> BeanDescriptor<T> create(Type ofType, BeanCreator<T> creator,
			List<PropertyAccessor<T, ?>> accessors, Map<String, PropertyMutator<T, ?>> mutators) {
		Class<T> parameterizedTypeForBeanView = (Class<T>) getRawClass(expandType(
				BeanView.class.getTypeParameters()[0], ofType));
		return new BeanDescriptor(parameterizedTypeForBeanView, getRawClass(ofType), accessors, mutators, creator);
	}

	private TransformationRuntimeException couldNotInstantiateBeanView(Class<?> beanViewClass,
			Exception e) {
		return new TransformationRuntimeException("Could not instantiate BeanView "
				+ beanViewClass.getName()
				+ ", BeanView implementations must have a public no arg constructor.", e);
	}

	@Override
	public <T> List<BeanCreator<T>> provideBeanCreators(Type ofType, Genson genson) {
		List<BeanCreator<T>> creators = new ArrayList<BeanCreator<T>>();
		for (Class<?> clazz = getRawClass(ofType); clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			provideMethodCreators(clazz, creators, ofType, genson);
		}
		Type viewForType = TypeUtil.expandType(BeanView.class.getTypeParameters()[0], ofType);
		List<BeanCreator<T>> oCtrs = super.provideBeanCreators(viewForType, genson);
		creators.addAll(oCtrs);
		return creators;
	}

	@Override
	protected <T> PropertyAccessor<T, ?> createAccessor(String name, Method method, Type ofType,
			Genson genson) {
		// the target bean must be first (and single) parameter for beanview accessors
		@SuppressWarnings("unchecked")
		BeanView<T> beanview = (BeanView<T>) views.get(getRawClass(ofType));
		Type superTypeWithParameter = TypeUtil.lookupGenericType(BeanView.class,
				beanview.getClass());
		@SuppressWarnings("unchecked")
		Class<T> tClass = (Class<T>) TypeUtil.typeOf(0,
				TypeUtil.expandType(superTypeWithParameter, beanview.getClass()));
		Type type = TypeUtil.expandType(method.getGenericReturnType(), ofType);
		return new BeanViewPropertyAccessor<T, Object>(name, method, type, beanview, tClass,
				genson.provideConverter(type));
	}

	@Override
	protected <T> PropertyMutator<T, ?> createMutator(String name, Method method, Type ofType,
			Genson genson) {
		// the target bean must be second parameter for beanview mutators
		@SuppressWarnings("unchecked")
		BeanView<T> beanview = (BeanView<T>) views.get(getRawClass(ofType));
		Type superTypeWithParameter = TypeUtil.lookupGenericType(BeanView.class,
				beanview.getClass());
		@SuppressWarnings("unchecked")
		Class<T> tClass = (Class<T>) TypeUtil.typeOf(0,
				TypeUtil.expandType(superTypeWithParameter, beanview.getClass()));
		Type type = TypeUtil.expandType(method.getGenericParameterTypes()[0], ofType);

		return new BeanViewPropertyMutator<T, Object>(name, method, type, beanview, tClass,
				genson.provideConverter(type));
	}

	public static class BeanViewMutatorAccessorResolver implements BeanMutatorAccessorResolver {

		public Trilean isAccessor(Field field, Class<?> baseClass) {
			return FALSE;
		}

		public Trilean isAccessor(Method method, Class<?> baseClass) {
			int modifiers = method.getModifiers();
			return Trilean.valueOf((method.getName().startsWith("get") || (method.getName()
					.startsWith("is") && (TypeUtil.match(method.getGenericReturnType(),
					Boolean.class, false) || boolean.class.equals(method.getReturnType()))))
					&& TypeUtil.match(
							TypeUtil.typeOf(0,
									TypeUtil.lookupGenericType(BeanView.class, baseClass)),
							baseClass, method.getGenericParameterTypes()[0], baseClass, false)
					&& Modifier.isPublic(modifiers)
					&& !Modifier.isAbstract(modifiers)
					&& !Modifier.isNative(modifiers));
		}

		public Trilean isCreator(Constructor<?> constructor, Class<?> baseClass) {
			int modifier = constructor.getModifiers();
			return Trilean.valueOf(Modifier.isPublic(modifier)
					|| !(Modifier.isPrivate(modifier) || Modifier.isProtected(modifier)));
		}

		public Trilean isCreator(Method method, Class<?> baseClass) {
			if (method.getAnnotation(Creator.class) != null) {
				if (Modifier.isStatic(method.getModifiers())) return TRUE;
				throw new TransformationRuntimeException("Method " + method.toGenericString()
						+ " annotated with @Creator must be static!");
			}
			return FALSE;
		}

		public Trilean isMutator(Field field, Class<?> baseClass) {
			return FALSE;
		}

		public Trilean isMutator(Method method, Class<?> baseClass) {
			int modifiers = method.getModifiers();
			return Trilean.valueOf(method.getName().startsWith("set")
					&& void.class.equals(method.getReturnType())
					&& method.getGenericParameterTypes().length == 2
					&& TypeUtil.match(
							method.getGenericParameterTypes()[1],
							TypeUtil.typeOf(
									0,
									TypeUtil.lookupGenericType(BeanView.class,
											method.getDeclaringClass())), false)
					&& Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)
					&& !Modifier.isNative(modifiers));
		}

	}

	private class BeanViewPropertyAccessor<T, P> extends MethodAccessor<T, P> {
		private final BeanView<?> _view;

		public BeanViewPropertyAccessor(String name, Method getter, Type type, BeanView<T> target,
				Class<T> tClass, Serializer<P> propertySeriliazer) {
			super(name, getter, type, tClass, propertySeriliazer);
			this._view = target;
		}

		@SuppressWarnings("unchecked")
		@Override
		public P access(T target) {
			try {
				return (P) _getter.invoke(_view, target);
			} catch (IllegalArgumentException e) {
				throw couldNotAccess(e);
			} catch (IllegalAccessException e) {
				throw couldNotAccess(e);
			} catch (InvocationTargetException e) {
				throw couldNotAccess(e);
			}
		}
	}

	private class BeanViewPropertyMutator<T, P> extends MethodMutator<T, P> {
		private final BeanView<?> _view;

		public BeanViewPropertyMutator(String name, Method setter, Type type, BeanView<T> target,
				Class<T> tClass, Deserializer<P> propertyDeserializer) {
			super(name, setter, type, tClass, propertyDeserializer);
			this._view = target;
		}

		@Override
		public void mutate(T target, P value) {
			try {
				_setter.invoke(_view, value, target);
			} catch (IllegalArgumentException e) {
				throw couldNotMutate(e);
			} catch (IllegalAccessException e) {
				throw couldNotMutate(e);
			} catch (InvocationTargetException e) {
				throw couldNotMutate(e);
			}
		}
	}
}
