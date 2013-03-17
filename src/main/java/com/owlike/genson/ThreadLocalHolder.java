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
 * @see Context
 * @see com.owlike.genson.ext.spring.ExtendedReqRespBodyMethodProcessor ExtendedReqRespBodyMethodProcessor
 * @see com.owlike.genson.ext.spring.GensonMessageConverter GensonMessageConverter
 * 
 * @author eugen
 *
 */
public final class ThreadLocalHolder {
	private final static ThreadLocal<Map<String, Object>> _data = new ThreadLocal<Map<String, Object>>() {
		protected java.util.Map<String,Object> initialValue() {
			return new HashMap<String, Object>();
		};
	};
	
	public static Object store(String key, Object parameter) {
		checkNotNull(key);
		return _data.get().put(key, parameter);
	}
	
	public static <T> T remove(String key, Class<T> valueType) {
		checkNotNull(key, valueType);
		T value = valueType.cast(_data.get().get(key));
		_data.get().remove(key);
		return value;
	}
	
	public static <T> T get(String key, Class<T> valueType) {
		checkNotNull(key, valueType);
		return valueType.cast(_data.get().get(key));
	}
}
