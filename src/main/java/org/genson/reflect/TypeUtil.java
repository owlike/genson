package org.genson.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * TODO restructure and clean
 * TODO ExpandedType do we need a reference to an original type?=> Not nice to provide 2 implementations for ParameterizedType...
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
	private final static Map<TypeAndRootClassKey, Type> _cache = new HashMap<TypeUtil.TypeAndRootClassKey, Type>(
			32);

	public final static Class<?> wrap(Class<?> clazz) {
		Class<?> wrappedClass = _wrappedPrimitives.get(clazz);
		return wrappedClass == null ? clazz : wrappedClass;
	}
	
	public final static Type expandType(Type type, Class<?> rootClass) {
		if (type instanceof ExpandedType || type instanceof Class)
			return type;
		TypeAndRootClassKey key = new TypeAndRootClassKey(type, rootClass);
		Type expandedType = _cache.get(key);

		if (expandedType == null) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				Type[] args = pType.getActualTypeArguments();
				int len = args.length;
				Type[] expandedArgs = new Type[len];
				for (int i = 0; i < len; i++) {
					expandedArgs[i] = expandType(args[i], rootClass);
				}
				expandedType = new ExpandedParameterizedType(pType, rootClass, expandedArgs);
			} else if (type instanceof TypeVariable) {
				TypeVariable<Class<?>> tvType = (TypeVariable<Class<?>>) type; // TODO
				expandedType = resolveTypeVariable(tvType, rootClass);
				if (type == expandedType)
					expandedType = expandType(tvType.getBounds()[0], rootClass);
			} else if (type instanceof GenericArrayType) {
				GenericArrayType genArrType = (GenericArrayType) type;
				Type cType = expandType(genArrType.getGenericComponentType(), rootClass);
				if (genArrType.getGenericComponentType() == cType)
					cType = Object.class;
				expandedType = new ExpandedGenericArrayType(genArrType, cType, rootClass);
			} else if (type instanceof WildcardType) {
				WildcardType wType = (WildcardType) type;
				Type[] lowerBounds = new Type[wType.getLowerBounds().length];
				Type[] upperBounds = new Type[wType.getUpperBounds().length];
				int i = 0;
				for (Type bound : wType.getLowerBounds()) {
					lowerBounds[i++] = expandType(bound, rootClass);
				}
				i = 0;
				for (Type bound : wType.getUpperBounds()) {
					upperBounds[i++] = expandType(bound, rootClass);
				}
				expandedType = new ExpandedWildcardType(wType, rootClass, lowerBounds, upperBounds);
			} else
				throw new IllegalArgumentException("Type " + type + " not supported for expansion!");
			_cache.put(key, expandedType);
		}

		return expandedType;
	}

	public final static Type lookupGenericType(Class<?> ofClass, Class<?> inClass) {
		if (ofClass == null || inClass == null || !ofClass.isAssignableFrom(inClass))
			return null;
		if (ofClass.equals(inClass))
			return inClass;

		if (ofClass.isInterface()) {
			// lets look if the interface is directly implemented by fromClass
			Class<?>[] interfaces = inClass.getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				// do they match?
				if (ofClass.equals(interfaces[i])) {
					return inClass.getGenericInterfaces()[i];
				} else {
					Type superType = lookupGenericType(ofClass, interfaces[i]);
					if (superType != null)
						return superType;
				}
			}
		}

		// ok it's not one of the directly implemented interfaces, lets try extended class
		Class<?> superClass = inClass.getSuperclass();
		if (ofClass.equals(superClass))
			return inClass.getGenericSuperclass();
		return lookupGenericType(ofClass, inClass.getSuperclass());
	}

	public final static Class<?> getRawClass(Type type) {
		return getRawClass(type, null);
	}

	public final static Class<?> getRawClass(Type type, Class<?> inClass) {
		if (type instanceof Class<?>)
			return (Class<?>) type;
		else if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return (Class<?>) pType.getRawType();
		} else if (type instanceof GenericArrayType) {
			GenericArrayType genericArrayType = (GenericArrayType) type;
			Class<?> c = getRawClass(expand(genericArrayType.getGenericComponentType(), inClass));
			return Array.newInstance(c, 0).getClass();
		} else {
			return getRawClass(expand(type, inClass), inClass);
		}
	}

	public final static Type getCollectionType(Type type) {
		return getCollectionType(type, null);
	}

	public final static Type getCollectionType(Type type, Class<?> inClass) {
		if (type instanceof GenericArrayType) {
			return expand(((GenericArrayType) type).getGenericComponentType(), inClass);
		} else if (type instanceof Class<?>) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isArray())
				return clazz.getComponentType();
			else {
				TypeVariable<?>[] tvs = clazz.getTypeParameters();
				if (tvs.length > 0)
					return expand(tvs[0], inClass);
			}
		} else {
			return expand(typeOf(0, type), inClass);
		}

		throw new IllegalArgumentException(
				"Could not extract parametrized type, are you sure it is a Collection or an Array?");
	}

	// TODO ensure that only typevar and wilcardtype ???
	public final static Type expand(Type type, Class<?> inClass) {
		Type expandedType = null;
		if (type instanceof TypeVariable<?>) {
			try {
				@SuppressWarnings("unchecked")
				// for the moment we assume it is a class, we can later handle ctr and methods
				TypeVariable<? extends Class<?>> tvType = (TypeVariable<? extends Class<?>>) type;
				if (inClass == null)
					inClass = tvType.getGenericDeclaration();
				expandedType = resolveTypeVariable(tvType, inClass);
				if (type.equals(expandedType))
					expandedType = tvType.getBounds()[0];
			} catch (ClassCastException cce) {
				throw new UnsupportedOperationException();
			}
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
	 * @return
	 */
	public final static Type resolveTypeVariable(TypeVariable<? extends Class<?>> type,
			Class<?> inClass) {
		return resolveTypeVariable(type, type.getGenericDeclaration(), inClass);
	}

	private final static Type resolveTypeVariable(TypeVariable<?> type, Class<?> declaringClass,
			Class<?> inClass) {

		if (inClass == null)
			return null;

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
				// TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! test with same class
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
	 * Regarde si oClazz a implemente une interface de type clazz avec un seul argument generique
	 * (le type parameter), par exemple Serializer<List<Integer>>
	 * 
	 * @param clazz la classe que l'on cherche (ex Serializer)
	 * @param parameter le type generique attendu List<Integer>
	 * @param oClazz - la classe implementant p-e cette interface
	 * @return le type trouve qui en l'occurence serait Serializer<Collection<Number>> si la classe
	 *         implemente l'interface Serializer de cette facon, null sinon
	 */
	public final static Type lookupWithGenerics(Class<?> clazz, Type parameter, Class<?> oClazz,
			boolean strictMatch) {
		if (oClazz == null)
			return null;

		Type type = lookupGenericType(clazz, oClazz);
		if (type != null) {
			type = expandType(type, oClazz);
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				return match(parameter, pType.getActualTypeArguments()[0], strictMatch)? type
						: null;
			}
		}
		return null;
	}

	/**
	 * Compare les deux types en profondeur, si ce sont des ParameterizedType on va comparer de
	 * facon recursive. On compare les classes avec isAssignableFrom, donc si type est List<Integer>
	 * et oType Collection<Number>, cette methode va regarder que les types de type peuvent etre
	 * castes en oType. Donc List en Collection et Integer en Number.
	 * 
	 * @param type le type connu
	 * @param oType un type qui serait egal ou atteignable a partir de type (voir exemple)
	 * @return vrai ou faux
	 */
	public final static boolean match(Type type, Type oType, boolean strictMatch) {
		Class<?> typeCtx = type instanceof Class ? (Class<?>) type : null;
		Class<?> OTypeCtx = oType instanceof Class ? (Class<?>) oType : null;
		return match(type, typeCtx, oType, OTypeCtx, strictMatch);
	}

	public final static boolean match(Type type, Class<?> typeCtx, Type oType, Class<?> oTypeCtx,
			boolean strictMatch) {
		if (type == null || oType == null)
			return type == null && oType == null;
		Class<?> clazz = getRawClass(type, typeCtx);
		Class<?> oClazz = getRawClass(oType, oTypeCtx);
		boolean match = strictMatch ? oClazz.equals(clazz) : oClazz.isAssignableFrom(clazz);
		
		if (clazz.isArray() && !oClazz.isArray()) return match;
		
		Type[] types = getTypes(expand(type, typeCtx));
		Type[] oTypes = getTypes(expand(oType, oTypeCtx));

		match = match && (types.length == oTypes.length);

		for (int i = 0; i < types.length && match; i++)
			match = match(types[i], typeCtx, oTypes[i], oTypeCtx, strictMatch);

		return match;
	}

	private final static Type[] getTypes(Type type) {
		if (type instanceof Class) {
			Class<?> tClass = (Class<?>) type;
			if (tClass.isArray())
				return new Type[] { tClass.getComponentType() };
			else
				return ((Class<?>) type).getTypeParameters();
		} else if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments();
		} else if (type instanceof GenericArrayType) {
			return new Type[] { ((GenericArrayType) type).getGenericComponentType() };
		} else if (type instanceof WildcardType) {
			return union(Type[].class, ((WildcardType) type).getUpperBounds(),
					((WildcardType) type).getLowerBounds());
		} else if (type instanceof TypeVariable<?>) {
			@SuppressWarnings("unchecked")
			TypeVariable<Class<?>> tvType = (TypeVariable<Class<?>>) type;
			Type resolvedType = resolveTypeVariable(tvType, tvType.getGenericDeclaration());
			return tvType.equals(resolvedType) ? new Type[] { resolvedType } : tvType.getBounds();
		} else
			return new Type[0];
	}

	public final static <T> T[] union(Class<T[]> tClass, T[]... values) {
		int size = 0;
		for (T[] value : values)
			size += value.length;
		T[] arr = tClass.cast(Array.newInstance(tClass.getComponentType(), size));
		for (int i = 0, len = 0; i < values.length; len += values[i].length, i++)
			System.arraycopy(values[i], 0, arr, len, values[i].length);
		return arr;
	}

	/**
	 * Searches in the hierarchy of inClass for the parameter type of implementedInterface. For
	 * example: <br>
	 * <code>
	 *  interface MyInterface&lt;T&gt; {}
	 *  <br>class MyClass implements MyInterface&lt;Integer&gt; {}
	 *  <br>parameterOf(0, MyClass.getGenericInterfaces()[0], MyClass.class) 
	 *  <br>will return Integer 
	 *  <br>
	 * 	<br>class MySecondClass&lt;T&gt; implements MyInterface&lt;T&gt; {}
	 * 	<br>class MyMostSpecificClass extends MySecondClass&lt;Integer&gt; {}
	 *  <br>parameterOf(0, MyMostSpecificClass.getSuperclass().getGenericInterfaces()[0], MyMostSpecificClass.class)
	 *  <br>will also return Integer 
	 * 
	 * </code>
	 * 
	 * @param index of the parameter
	 * @param implementedInterface the interface implemented by inClass from which we want to
	 *        resolve one of the parameters
	 * @param rootClass is the interface implementation (maybe declaring the concrete type if
	 *        interface parameter is a TypeVariable)
	 * @return the resolved type
	 */
	public final static Type typeOf(int parameterIdx, Type fromType) {
		if (fromType instanceof Class<?>) {
			Class<?> tClass = (Class<?>) fromType;
			TypeVariable<?>[] tvs = tClass.getTypeParameters();
			if (tvs.length > parameterIdx)
				return tvs[parameterIdx];
		} else if (fromType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) fromType;
			Type[] ts = pType.getActualTypeArguments();
			if (ts.length > parameterIdx)
				return ts[parameterIdx];
		} else
			throw new IllegalArgumentException("Type " + fromType + " not supported!");
		return null;
	}

	
	/*
	 * Should subclasses implement equals and hashcode ? its not necessary as the same combinaison
	 * of rootclass+type should always correspond to the same expanded type...
	 */
	private static abstract class ExpandedType<T extends Type> {
		protected final T originalType;
		protected final Class<?> rootClass;
		private int hash;

		private ExpandedType(T originalType, Class<?> rootClass) {
			if (originalType == null || rootClass == null)
				throw new IllegalArgumentException("Null arg not allowed!");
			this.originalType = originalType;
			this.rootClass = rootClass;
			hash = 31 + rootClass.hashCode();
			hash = 31 * hash + originalType.hashCode();
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
			return hash;
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
			return originalType.equals(other.originalType) && rootClass.equals(other.rootClass);
		}
	}

	private final static class ExpandedGenericArrayType extends ExpandedType<GenericArrayType>
			implements GenericArrayType {
		private final Type componentType;

		public ExpandedGenericArrayType(GenericArrayType originalType, Type componentType,
				Class<?> rootClass) {
			super(originalType, rootClass);
			if (componentType == null)
				throw new IllegalArgumentException("Null arg not allowed!");
			this.componentType = componentType;
		}

		@Override
		public Type getGenericComponentType() {
			return componentType;
		}
	}

	public static ParameterizedType createParameterizedType(Type ofRawType, Type inOwnerType,
			Type... withActualTypeArguments) {
		return new ParameterizedTypeImpl(withActualTypeArguments, inOwnerType, ofRawType);
	}

	private final static class ParameterizedTypeImpl implements ParameterizedType {
		private final Type[] actualTypeArguments;
		private final Type ownerType;
		private final Type rawType;
		private int hash;

		public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
			super();
			if (actualTypeArguments == null || actualTypeArguments.length == 0 || rawType == null)
				throw new IllegalArgumentException();
			this.actualTypeArguments = actualTypeArguments;
			this.ownerType = ownerType;
			this.rawType = rawType;

			hash = 31 + (ownerType != null ? ownerType.hashCode() : 0);
			hash = 31 * hash + rawType.hashCode();
			hash = 31 * hash + Arrays.hashCode(actualTypeArguments);
		}

		public Type[] getActualTypeArguments() {
			return actualTypeArguments;
		}

		public Type getOwnerType() {
			return ownerType;
		}

		public Type getRawType() {
			return rawType;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ParameterizedType)) {
				return false;
			}
			ParameterizedType other = (ParameterizedType) obj;
			if (!Arrays.equals(actualTypeArguments, other.getActualTypeArguments())) {
				return false;
			}
			if (ownerType == null) {
				if (other.getOwnerType() != null) {
					return false;
				}
			} else if (!ownerType.equals(other.getOwnerType())) {
				return false;
			}
			if (rawType == null) {
				if (other.getRawType() != null) {
					return false;
				}
			} else if (!rawType.equals(other.getRawType())) {
				return false;
			}
			return true;
		}
	}

	private final static class ExpandedParameterizedType extends ExpandedType<ParameterizedType>
			implements ParameterizedType {
		private final Type[] typeArgs;

		public ExpandedParameterizedType(ParameterizedType originalType, Class<?> rootClass,
				Type[] typeArgs) {
			super(originalType, rootClass);
			if (typeArgs == null)
				throw new IllegalArgumentException("Null arg not allowed!");
			this.typeArgs = typeArgs;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArgs;
		}

		@Override
		public Type getOwnerType() {
			return originalType.getOwnerType();
		}

		@Override
		public Type getRawType() {
			return originalType.getRawType();
		}
	}

	private final static class ExpandedWildcardType extends ExpandedType<WildcardType> implements
			WildcardType {
		private final Type[] lowerBounds;
		private final Type[] upperBounds;

		public ExpandedWildcardType(WildcardType originalType, Class<?> rootClass,
				Type[] lowerBounds, Type[] upperBounds) {
			super(originalType, rootClass);
			if (lowerBounds == null || upperBounds == null)
				throw new IllegalArgumentException("Null arg not allowed!");
			this.lowerBounds = lowerBounds;
			this.upperBounds = upperBounds;
		}

		@Override
		public Type[] getLowerBounds() {
			return lowerBounds;
		}

		@Override
		public Type[] getUpperBounds() {
			return upperBounds;
		}
	}

	private final static class TypeAndRootClassKey {
		private final Type type;
		private final Class<?> rootClass;
		private int _hash;

		public TypeAndRootClassKey(Type type, Class<?> rootClass) {
			super();
			if (type == null || rootClass == null)
				throw new IllegalArgumentException("type and rootClass must be not null!");
			this.type = type;
			this.rootClass = rootClass;
			_hash = 31 + rootClass.hashCode();
			_hash = 31 * _hash + type.hashCode();
		}

		@Override
		public int hashCode() {
			return _hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof TypeAndRootClassKey))
				return false;
			TypeAndRootClassKey other = (TypeAndRootClassKey) obj;
			return rootClass.equals(other.rootClass) && type.equals(other.type);
		}
	}
}
