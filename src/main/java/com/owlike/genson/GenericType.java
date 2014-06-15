package com.owlike.genson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.owlike.genson.reflect.TypeUtil;

/**
 * This class is a holder for generic types so we can work around type erasure. You can read <a
 * href="http://gafter.blogspot.fr/2006/12/super-type-tokens.html">this blog post</a> who explains a
 * bit more in details what it is about. For example if you want to use at runtime a
 * List&lt;Integer&gt; :
 * <p/>
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
 * @param <T> the real type
 * @author eugen
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

  private GenericType(Class<T> rawClass) {
    this.type = rawClass;
    this.rawClass = rawClass;
  }

  @SuppressWarnings("unchecked")
  private GenericType(Type type) {
    this.type = type;
    this.rawClass = (Class<T>) TypeUtil.getRawClass(type);
  }

  public static <T> GenericType<T> of(Class<T> rawClass) {
    return new GenericType<T>(rawClass) {
    };
  }

  public static GenericType<Object> of(Type type) {
    return new GenericType<Object>(type) {
    };
  }

  public Type getType() {
    return type;
  }

  public Class<T> getRawClass() {
    return rawClass;
  }
}
