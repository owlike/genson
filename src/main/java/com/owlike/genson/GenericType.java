package com.owlike.genson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.owlike.genson.reflect.TypeUtil;

/**
 * This class is a holder for generic types so we can work around type erasure. You can read <a
 * href="http://gafter.blogspot.fr/2006/12/super-type-tokens.html">this blog post</a> who explains a
 * bit more in details what it is about. For example if you want to use at runtime a
 * List&lt;Integer&gt; :
 * 
 * <pre>
 * GenericType&lt;List&lt;Integer&gt;&gt; genericType = new GenericType&lt;List&lt;Integer&gt;&gt;() {
 * };
 * List&lt;Integer&gt; listOfIntegers = new Genson().deserialize(&quot;[1,2,3]&quot;, genericType);
 * 
 * // if you want to get the standard java.lang.reflect.Type corresponding to List&lt;Integer&gt; from
 * // genericType
 * Type listOfIntegersType = genericType.getType();
 * // listOfIntegersType will be an instance of ParameterizedType with Integer class as type argument
 * </pre>
 * 
 * @author eugen
 * 
 * @param <T>
 *            the real type
 */
public abstract class GenericType<T> {
	private final Type type;
	private final Class<T> rawClass;

	@SuppressWarnings("unchecked")
	protected GenericType() {
		Type superType = getClass().getGenericSuperclass();
		if (superType instanceof Class<?>) {
			throw new IllegalArgumentException("You must specify the parametrized type!");
		}
		type = ((ParameterizedType) superType).getActualTypeArguments()[0];
		rawClass = (Class<T>) TypeUtil.getRawClass(type);
	}

	private GenericType(Class<T> rawClass, Type type) {
		this.type = type;
		this.rawClass = rawClass;
	}

	private final static Map<Type, GenericType<?>> _genericTypesCache = new ConcurrentHashMap<Type, GenericType<?>>();
	@SuppressWarnings("unchecked")
	public static <T> GenericType<T> genericTypeFor(Class<T> rawClass, Type type) {
		Class<?> typeRawClass = TypeUtil.getRawClass(type);
		if (!rawClass.equals(typeRawClass))
			throw new IllegalArgumentException("Argument rawClass " + rawClass
					+ " does not match raw class " + typeRawClass + " of argument type " + type);
		
		GenericType<T> genericType = (GenericType<T>) _genericTypesCache.get(type);
		if (genericType == null) {
			genericType = new GenericType<T>(rawClass, type) {};
			_genericTypesCache.put(type, genericType);
		}
		return genericType;
	}

	public Type getType() {
		return type;
	}

	public Class<T> getRawClass() {
		return rawClass;
	}
}
