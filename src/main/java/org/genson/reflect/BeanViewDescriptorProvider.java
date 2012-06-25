package org.genson.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.genson.BeanView;
import org.genson.TransformationRuntimeException;
import org.genson.annotation.Creator;
import org.genson.reflect.PropertyAccessor.MethodAccessor;
import org.genson.reflect.PropertyMutator.MethodMutator;


public class BeanViewDescriptorProvider extends BaseBeanDescriptorProvider {
	private Map<Class<?>, BeanView<?>> views = new HashMap<Class<?>, BeanView<?>>();
	private Map<Class<?>, BeanDescriptor> descriptors = new ConcurrentHashMap<Class<?>, BeanDescriptor>();

	public BeanViewDescriptorProvider(BeanMutatorAccessorResolver mutatorAccessorResolver,
			PropertyNameResolver nameResolver) {
		super(mutatorAccessorResolver, nameResolver, true, false);
	}

	@Override
	public BeanDescriptor provideBeanDescriptor(Class<?> ofClass) {
		if (!BeanView.class.isAssignableFrom(ofClass))
			throw new IllegalArgumentException("Expected argument of type "
					+ BeanView.class.getName() + " but provided " + ofClass.getName());

		BeanDescriptor descriptor = descriptors.get(ofClass);
		if (descriptor == null) {
			try {
				@SuppressWarnings("unchecked")
				Constructor<BeanView<?>> ctr = (Constructor<BeanView<?>>) ofClass.getDeclaredConstructor();
				if (!ctr.isAccessible()) ctr.setAccessible(true);
				views.put(ofClass, ctr.newInstance());
				descriptor = super.provideBeanDescriptor(ofClass);
				descriptors.put(ofClass, descriptor);
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

	private TransformationRuntimeException couldNotInstantiateBeanView(Class<?> beanViewClass,
			Exception e) {
		return new TransformationRuntimeException("Could not instantiate BeanView "
				+ beanViewClass.getName()
				+ ", BeanView implementations must have a public no arg constructor.", e);
	}

	@Override
	public List<BeanCreator> provideBeanCreators(Class<?> ofClass) {
		List<BeanCreator> creators = new ArrayList<BeanCreator>();
		for (Class<?> clazz = ofClass; clazz != null && !Object.class.equals(clazz); clazz = clazz
				.getSuperclass()) {
			provideMethodCreators(clazz, creators, ofClass);
		}
		creators.addAll(super.provideBeanCreators(TypeUtil.getRawClass(BeanView.class.getTypeParameters()[0], ofClass)));
		return creators;
	}
	
	@Override
	protected PropertyAccessor createAccessor(String name, Method method, Class<?> baseClass) {
		// the target bean must be first (and single) parameter for beanview accessors
		return new BeanViewPropertyAccessor(name, method, views.get(baseClass));
	}

	@Override
	protected PropertyMutator createMutator(String name, Method method, Class<?> baseClass) {
		// the target bean must be second parameter for beanview mutators
		return new BeanViewPropertyMutator(name, method, views.get(baseClass));
	}

	public static class BeanViewMutatorAccessorResolver implements BeanMutatorAccessorResolver {

		@Override
		public boolean isAccessor(Field field, Class<?> baseClass) {
			return false;
		}

		@Override
		public boolean isAccessor(Method method, Class<?> baseClass) {
			int modifiers = method.getModifiers();
			return (method.getName().startsWith("get") || (method.getName().startsWith("is") && (TypeUtil
					.match(method.getGenericReturnType(), Boolean.class, false) || boolean.class
					.equals(method.getReturnType()))))
					&& TypeUtil.match(
							TypeUtil.typeOf(0, TypeUtil.lookupGenericType(BeanView.class, baseClass)), baseClass,
							method.getGenericParameterTypes()[0], baseClass, false)
					&& Modifier.isPublic(modifiers)
					&& !Modifier.isAbstract(modifiers) && !Modifier.isNative(modifiers);
		}

		@Override
		public boolean isCreator(Constructor<?> constructor, Class<?> baseClass) {
			int modifier = constructor.getModifiers();
			return Modifier.isPublic(modifier) || 
					!(Modifier.isPrivate(modifier) || Modifier.isProtected(modifier));
		}

		@Override
		public boolean isCreator(Method method, Class<?> baseClass) {
			if (method.getAnnotation(Creator.class) != null) {
				if (Modifier.isStatic(method.getModifiers())) return true;
				throw new TransformationRuntimeException("Method " + method.toGenericString() + " annotated with @Creator must be static!");
			}
			return false;
		}

		@Override
		public boolean isMutator(Field field, Class<?> baseClass) {
			return false;
		}

		@Override
		public boolean isMutator(Method method, Class<?> baseClass) {
			int modifiers = method.getModifiers();
			return method.getName().startsWith("set")
					&& void.class.equals(method.getReturnType())
					&& method.getGenericParameterTypes().length == 2
					&& TypeUtil.match(method.getGenericParameterTypes()[1], TypeUtil.typeOf(0,
							TypeUtil.lookupGenericType(BeanView.class, method.getDeclaringClass())), false)
					&& Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)
					&& !Modifier.isNative(modifiers);
		}

	}

	private class BeanViewPropertyAccessor extends MethodAccessor {
		private final BeanView<?> _view;

		public BeanViewPropertyAccessor(String name, Method getter, BeanView<?> target) {
			super(name, getter, target.getClass());
			this._view = target;
		}

		@Override
		public Object access(Object target) {
			try {
				return _getter.invoke(_view, target);
			} catch (IllegalArgumentException e) {
				throw couldNotAccess(e);
			} catch (IllegalAccessException e) {
				throw couldNotAccess(e);
			} catch (InvocationTargetException e) {
				throw couldNotAccess(e);
			}
		}
	}

	private class BeanViewPropertyMutator extends MethodMutator {
		private final BeanView<?> _view;

		public BeanViewPropertyMutator(String name, Method setter, BeanView<?> target) {
			super(name, setter, target.getClass());
			this._view = target;
		}

		@Override
		public void mutate(Object target, Object value) {
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
