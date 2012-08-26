package com.owlike.genson;

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
	
	public static void store(String key, Object parameter) {
		if (key == null)
		throw new IllegalArgumentException();
		_data.get().put(key, parameter);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T remove(String key) {
		if (key == null)
			throw new IllegalArgumentException();
		return (T) _data.get().remove(key);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		if (key == null)
			throw new IllegalArgumentException();
		return (T) _data.get().get(key);
	}
}
