package com.owlike.genson.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.owlike.genson.Operations;

/**
 * This class provides utilities to work with java Types. Its main goal is to provide tools for
 * working with generic types. 
 * 
 * @author eugen
 */
/*
 * TODO ExpandedType do we need a reference to an original type?=> Not nice to provide 2
 * implementations for ParameterizedType...
 */
public final class TypeUtil {
	private final static Map<Class<?>, Class<?>> _wrappedPrimitives = new HashMap<Class<?>, Class<?>>();
	static {
		_wrappedPrimitives.put(int.class, Integer.class);
		_wrappedPrimitives.put(double.class, Double.class);
		_wrappedPrimitives.put(long.class, Long.class);
		_wrappedPrimitives.put(float.class, Float.class);
		_wrappedPrimitives.put(short.class, Short.class);
		_wrappedPrimitives.put(boolean.class, Boolean.class);
		_wrappedPrimitives.put(char.class, Character.class);
		_wrappedPrimitives.put(byte.class, Byte.class);
		_wrappedPrimitives.put(void.class, Void.class);
	}
	private final static Map<TypeAndRootClassKey, Type> _cache = new ConcurrentHashMap<TypeUtil.TypeAndRootClassKey, Type>(
			32);

	public final static Class<?> wrap(Class<?> clazz) {
		Class<?> wrappedClass = _wrappedPrimitives.get(clazz);
		return wrappedClass == null ? clazz : wrappedClass;
	}

	/**
	 * Expands type in the type rootType to Class, ParameterizedType or GenericArrayType. Useful for
	 * generic types. rootType is used to get the specialization information for expansion.
	 */
	public final static Type expandType(final Type type, final Type rootType) {
		if (type instanceof ExpandedType || type instanceof Class) return type;
		TypeAndRootClassKey key = new TypeAndRootClassKey(type, rootType);
		Type expandedType = _cache.get(key);

		if (expandedType == null) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				Type[] args = pType.getActualTypeArguments();
				int len = args.length;
				Type[] expandedArgs = new Type[len];
				for (int i = 0; i < len; i++) {
					expandedArgs[i] = expandType(args[i], rootType);
				}
				expandedType = new ExpandedParameterizedType(pType, getRawClass(rootType),
						expandedArgs);
			} else if (type instanceof TypeVariable) {
				@SuppressWarnings("unchecked")
				TypeVariable<GenericDeclaration> tvType = (TypeVariable<GenericDeclaration>) type;
				if (rootType instanceof ParameterizedType) {
					ParameterizedType rootPType = (ParameterizedType) rootType;
					Type[] typeArgs = rootPType.getActualTypeArguments();
					String typeName = tvType.getName();
					int idx = 0;
					for (TypeVariable<?> parameter : genericDeclarationToClass(
							tvType.getGenericDeclaration()).getTypeParameters()) {
						if (typeName.equals(parameter.getName())) {
							expandedType = typeArgs[idx];
							break;
						}
						idx++;
					}
				} else
					expandedType = resolveTypeVariable(tvType, getRawClass(rootType));

				if (type == expandedType)
					expandedType = expandType(tvType.getBounds()[0], rootType);
			} else if (type instanceof GenericArrayType) {
				GenericArrayType genArrType = (GenericArrayType) type;
				Type cType = expandType(genArrType.getGenericComponentType(), rootType);
				if (genArrType.getGenericComponentType() == cType) cType = Object.class;
				expandedType = new ExpandedGenericArrayType(genArrType, cType,
						getRawClass(rootType));
			} else if (type instanceof WildcardType) {
				WildcardType wType = (WildcardType) type;
				// let's expand wildcards to their upper bound or object if no upper bound defined
				// it will simplify things to accept only one upper bound and ignore lower bounds
				// as it is equivalent to Object.
				expandedType = wType.getUpperBounds().length > 0 ? expandType(
						wType.getUpperBounds()[0], rootType) : Object.class;
			}
			
			if (expandedType == null) throw new IllegalArgumentException("Type " + type + " not supported for expansion!");
			
			_cache.put(key, expandedType);
		}

		return expandedType;
	}

	/**
	 * Searches for ofClass in the inherited classes and interfaces of inClass. If ofClass has been
	 * found in the super classes/interfaces of inClass, then the generic type corresponding to
	 * inClass and its TypeVariables is returned, otherwise null. For example :
	 * 
	 * <pre>
	 * abstract class MyClass implements Serializer&lt;Number&gt; {
	 * 
	 * }
	 * 
	 * // type value will be the parameterized type Serializer&lt;Number&gt;
	 * Type type = lookupGenericType(Serializer.class, MyClass.class);
	 * </pre>
	 */
	public final static Type lookupGenericType(Class<?> ofClass, Class<?> inClass) {
		if (ofClass == null || inClass == null || !ofClass.isAssignableFrom(inClass)) return null;
		if (ofClass.equals(inClass)) return inClass;

		if (ofClass.isInterface()) {
			// lets look if the interface is directly implemented by fromClass
			Class<?>[] interfaces = inClass.getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				// do they match?
				if (ofClass.equals(interfaces[i])) {
					return inClass.getGenericInterfaces()[i];
				} else {
					Type superType = lookupGenericType(ofClass, interfaces[i]);
					if (superType != null) return superType;
				}
			}
		}

		// ok it's not one of the directly implemented interfaces, lets try extended class
		Class<?> superClass = inClass.getSuperclass();
		if (ofClass.equals(superClass)) return inClass.getGenericSuperclass();
		return lookupGenericType(ofClass, inClass.getSuperclass());
	}

	public final static Class<?> getRawClass(Type type) {
		if (type instanceof Class<?>)
			return (Class<?>) type;
		else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return (Class<?>) pType.getRawType();
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			return Array.newInstance(getRawClass(componentType), 0).getClass();
		} else
			return getRawClass(expand(type, null));
	}

	/**
	 * Returns the type of this Collection or Array.
	 * 
	 * @throws IllegalArgumentException
	 *             if type is not a Collection, not a generic array and not a primitive array.
	 */
	public final static Type getCollectionType(Type type) {
		if (type instanceof GenericArrayType) {
			return ((GenericArrayType) type).getGenericComponentType();
		} else if (type instanceof Class<?>) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isArray())
				return clazz.getComponentType();
			else if (Collection.class.isAssignableFrom(clazz)) {
				return Object.class;
			}
		} else if (type instanceof ParameterizedType
				&& Collection.class.isAssignableFrom(getRawClass(type))) {
			return typeOf(0, type);
		}

		throw new IllegalArgumentException(
				"Could not extract parametrized type, are you sure it is a Collection or an Array?");
	}

	// protected for testing.
	final static Type expand(Type type, Class<?> inClass) {
		Type expandedType = null;
		if (type instanceof TypeVariable) {
			@SuppressWarnings("unchecked")
			// for the moment we assume it is a class, we can later handle ctr and methods
			TypeVariable<GenericDeclaration> tvType = (TypeVariable<GenericDeclaration>) type;
			if (inClass == null)
				inClass = genericDeclarationToClass(tvType.getGenericDeclaration());
			expandedType = resolveTypeVariable(tvType, inClass);
			if (type.equals(expandedType)) expandedType = tvType.getBounds()[0];
		} else if (type instanceof WildcardType) {
			WildcardType wType = (WildcardType) type;
			expandedType = wType.getUpperBounds().length > 0 ? expand(wType.getUpperBounds()[0],
					inClass) : Object.class;
		} else
			return type;

		return expandedType == null || type.equals(expandedType) ? Object.class : expandedType;
	}

	/**
	 * Searches for the typevariable definition in the inClass hierarchy.
	 * 
	 * @param type
	 * @param inClass
	 * @return the resolved type or type if unable to resolve it.
	 */
	public final static Type resolveTypeVariable(TypeVariable<? extends GenericDeclaration> type,
			Class<?> inClass) {
		return resolveTypeVariable(type, genericDeclarationToClass(type.getGenericDeclaration()),
				inClass);
	}

	private final static Type resolveTypeVariable(TypeVariable<? extends GenericDeclaration> type,
			Class<?> declaringClass, Class<?> inClass) {

		if (inClass == null) return null;

		Class<?> superClass = null;
		Type resolvedType = null;
		Type genericSuperClass = null;

		if (!declaringClass.equals(inClass)) {
			if (declaringClass.isInterface()) {
				// the declaringClass is an interface
				Class<?>[] interfaces = inClass.getInterfaces();
				for (int i = 0; i < interfaces.length && resolvedType == null; i++) {
					superClass = interfaces[i];
					resolvedType = resolveTypeVariable(type, declaringClass, superClass);
					genericSuperClass = inClass.getGenericInterfaces()[i];
				}
			}

			if (resolvedType == null) {
				superClass = inClass.getSuperclass();
				resolvedType = resolveTypeVariable(type, declaringClass, superClass);
				genericSuperClass = inClass.getGenericSuperclass();
			}
		} else {
			resolvedType = type;
			genericSuperClass = superClass = inClass;

		}

		if (resolvedType != null) {
			// if its another type this means we have finished
			if (resolvedType instanceof TypeVariable<?>) {
				type = (TypeVariable<?>) resolvedType;
				TypeVariable<?>[] parameters = superClass.getTypeParameters();
				int positionInClass = 0;
				for (; positionInClass < parameters.length
						&& !type.equals(parameters[positionInClass]); positionInClass++) {
				}

				// we located the position of the typevariable in the superclass
				if (positionInClass < parameters.length) {
					// let's look if we have type specialization information in the current class
					if (genericSuperClass instanceof ParameterizedType) {
						ParameterizedType pGenericType = (ParameterizedType) genericSuperClass;
						Type[] args = pGenericType.getActualTypeArguments();
						return positionInClass < args.length ? args[positionInClass] : null;
					}
				}

				// we didnt find typevariable specialization in the class, so it's the best we can
				// do, lets return the resolvedType...
			}
		}

		return resolvedType;
	}

	/**
	 * Deep comparison between type and oType. If parameter strictMatch is true, then type and oType
	 * will be strictly compared otherwise this method checks whether oType is assignable from type.
	 */
	public final static boolean match(Type type, Type oType, boolean strictMatch) {
		if (type == null || oType == null) return type == null && oType == null;
		Class<?> clazz = getRawClass(type);
		Class<?> oClazz = getRawClass(oType);
		boolean match = strictMatch ? oClazz.equals(clazz) : oClazz.isAssignableFrom(clazz);
		
		if (Object.class.equals(oClazz) && !strictMatch) return match;
		
		if (clazz.isArray() && !oClazz.isArray()) return match;

		Type[] types = getTypes(type);
		Type[] oTypes = getTypes(oType);

		match = match && (types.length == oTypes.length || types.length == 0);

		for (int i = 0; i < types.length && match; i++)
			match = match(types[i], oTypes[i], strictMatch);

		return match;
	}

	/**
	 * Convenient method that returns the type of the parameter at position parameterIdx in the type
	 * fromType.
	 * 
	 * @exception UnsupportedOperationException
	 *                thrown if fromType is not a Class nor a ParameterizedType.
	 */
	public final static Type typeOf(int parameterIdx, Type fromType) {
		if (fromType instanceof Class<?>) {
			Class<?> tClass = (Class<?>) fromType;
			TypeVariable<?>[] tvs = tClass.getTypeParameters();
			if (tvs.length > parameterIdx) return expandType(tvs[parameterIdx], fromType);
		} else if (fromType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) fromType;
			Type[] ts = pType.getActualTypeArguments();
			if (ts.length > parameterIdx) return ts[parameterIdx];
		} else
			throw new UnsupportedOperationException("Type " + fromType + " not supported!");
		return null;
	}

	private static Class<?> genericDeclarationToClass(GenericDeclaration declaration) {
		if (declaration instanceof Class) return (Class<?>) declaration;
		if (declaration instanceof Method) return ((Method) declaration).getDeclaringClass();
		if (declaration instanceof Constructor)
			return ((Constructor<?>) declaration).getDeclaringClass();
		throw new UnsupportedOperationException();
	}

	private final static Type[] getTypes(Type type) {
		if (type instanceof Class) {
			Class<?> tClass = (Class<?>) type;
			if (tClass.isArray())
				return new Type[] { tClass.getComponentType() };
			else {
				TypeVariable<?>[] tvs = ((Class<?>) type).getTypeParameters();
				Type[] types = new Type[tvs.length];
				int i = 0;
				for (TypeVariable<?> tv : tvs) {
					types[i++] = tv.getBounds()[0];
				}
				return types;
			}
		} else if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments();
		} else if (type instanceof GenericArrayType) {
			return new Type[] { ((GenericArrayType) type).getGenericComponentType() };
		} else if (type instanceof WildcardType) {
			return Operations.union(Type[].class, ((WildcardType) type).getUpperBounds(),
					((WildcardType) type).getLowerBounds());
		} else if (type instanceof TypeVariable<?>) {
			@SuppressWarnings("unchecked")
			TypeVariable<Class<?>> tvType = (TypeVariable<Class<?>>) type;
			Type resolvedType = resolveTypeVariable(tvType, tvType.getGenericDeclaration());
			return tvType.equals(resolvedType) ? tvType.getBounds() : new Type[] { resolvedType };
		} else
			return new Type[0];
	}

	/*
	 * Should subclasses implement equals and hashcode ? its not necessary as the same combinaison
	 * of rootclass+type should always correspond to the same expanded type...
	 */
	private static abstract class ExpandedType<T extends Type> {
		protected final T originalType;
		protected final Class<?> rootClass;

		private ExpandedType(T originalType, Class<?> rootClass) {
			if (originalType == null || rootClass == null)
				throw new IllegalArgumentException("Null arg not allowed!");
			this.originalType = originalType;
			this.rootClass = rootClass;
		}

		@SuppressWarnings("unused")
		public T getOriginalType() {
			return originalType;
		}

		@SuppressWarnings("unused")
		public Class<?> getRootClass() {
			return rootClass;
		}

		@Override
		public int hashCode() {
			return originalType.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ExpandedType)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			ExpandedType<Type> other = (ExpandedType<Type>) obj;
			return originalType.equals(other.originalType);
		}
	}

	private final static class ExpandedGenericArrayType extends ExpandedType<GenericArrayType>
			implements GenericArrayType {
		private final Type componentType;

		public ExpandedGenericArrayType(GenericArrayType originalType, Type componentType,
				Class<?> rootClass) {
			super(originalType, rootClass);
			if (componentType == null) throw new IllegalArgumentException("Null arg not allowed!");
			this.componentType = componentType;
		}

		public Type getGenericComponentType() {
			return componentType;
		}
	}

	private final static class ExpandedParameterizedType extends ExpandedType<ParameterizedType>
			implements ParameterizedType {
		private final Type[] typeArgs;

		public ExpandedParameterizedType(ParameterizedType originalType, Class<?> rootClass,
				Type[] typeArgs) {
			super(originalType, rootClass);
			if (typeArgs == null) throw new IllegalArgumentException("Null arg not allowed!");
			this.typeArgs = typeArgs;
		}

		public Type[] getActualTypeArguments() {
			return typeArgs;
		}

		public Type getOwnerType() {
			return originalType.getOwnerType();
		}

		public Type getRawType() {
			return originalType.getRawType();
		}
	}

	private final static class TypeAndRootClassKey {
		private final Type type;
		private final Type rootType;
		private int _hash;

		public TypeAndRootClassKey(Type type, Type rootType) {
			super();
			if (type == null || rootType == null)
				throw new IllegalArgumentException("type and rootType must be not null!");
			this.type = type;
			this.rootType = rootType;
			_hash = 31 + rootType.hashCode();
			_hash = 31 * _hash + type.hashCode();
		}

		@Override
		public int hashCode() {
			return _hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof TypeAndRootClassKey)) return false;
			TypeAndRootClassKey other = (TypeAndRootClassKey) obj;
			return rootType.equals(other.rootType) && type.equals(other.type);
		}
	}
}
