package com.owlike.genson;

import static com.owlike.genson.Operations.checkNotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Just another data holder that stores data in a threadlocal map.
 * If you only want to share data across serializers and deserializers prefer using {@link Context}.
 * Internally Genson uses it for the spring webmvc integration, so it can pass method signatures and
 * extract its annotations, etc.
 *
 * @author eugen
 * @see Context
 * @see com.owlike.genson.ext.spring.ExtendedReqRespBodyMethodProcessor ExtendedReqRespBodyMethodProcessor
 * @see com.owlike.genson.ext.spring.GensonMessageConverter GensonMessageConverter
 */
public final class ThreadLocalHolder {
  private final static ThreadLocal<Map<String, Object>> _data = new ThreadLocal<Map<String, Object>>();

  public static Object store(String key, Object parameter) {
    checkNotNull(key);
    return getPutIfMissing().put(key, parameter);
  }

  public static <T> T remove(String key, Class<T> valueType) {
    checkNotNull(key, valueType);
    Map<String, Object> map = getPutIfMissing();
    T value = valueType.cast(map.get(key));
    map.remove(key);
    return value;
  }

  public static <T> T get(String key, Class<T> valueType) {
    checkNotNull(key, valueType);
    return valueType.cast(getPutIfMissing().get(key));
  }

  private static Map<String, Object> getPutIfMissing() {
    Map<String, Object> map = _data.get();
    if (map == null) {
      map = new HashMap<String, Object>();
      _data.set(map);
    }
    return map;
  }
}
