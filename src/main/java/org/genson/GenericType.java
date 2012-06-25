package org.genson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is a holder for generic types so we can work around type erasure. You can read <a
 * href="http://gafter.blogspot.fr/2006/12/super-type-tokens.html">this blog post</a> who explains a
 * bit more in details what it is about. For example if you want to use at runtime a
 * List&lt;Integer&gt; :
 * 
 * <pre>
 * GenericType&lt;List&lt;Integer&gt;&gt; genericType = new GenericType&lt;List&lt;Integer&gt;&gt;() {};
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
 * @param <T> the real type
 */
public abstract class GenericType<T> {
	private final Type type;

	protected GenericType() {
		Type superType = getClass().getGenericSuperclass();
		if (superType instanceof Class<?>) {
			throw new IllegalArgumentException("You must specify the parametrized type!");
		}
		type = ((ParameterizedType) superType).getActualTypeArguments()[0];
	}

	public Type getType() {
		return type;
	}
}
