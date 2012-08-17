package com.owlike.genson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The context class is intended to be a statefull class shared across a single execution. Its main
 * purpose is to hold local data and to pass it to others contributors of the
 * serialization/deserialization chain.
 * <p>
 * For example if you have computed in a first serializer a value and you need it later in another
 * one, you can put it in the context by using {@link Context#store(String, Object)} method and
 * later retrieve it with {@link #get(String)} or remove it with {@link #remove(String)}.
 * <p>
 * You can achieve the same thing with {@link ThreadLocalHolder} that stores the data in a thread
 * local map but it is cleaner to use this Context class as you wont have to worry about removing
 * values from it. Indeed java web servers reuse created threads, so if you store data in a thread
 * local variable and don't remove it, it will still be present when another request comes in and is
 * bound to that thread! This can also lead to memory leaks! Don't forget the
 * try-store-finally-remove block if you stick with ThreadLocalHolder.
 * 
 * <p>
 * This class stores also the views present in the current context, those views will be applied to
 * the matching objects during serialization and deserialization.
 * 
 * @see com.owlike.genson.BeanView BeanView
 * @see com.owlike.genson.convert.BeanViewConverter BeanViewConverter
 * @see com.owlike.genson.ThreadLocalHolder ThreadLocalHolder
 * 
 * @author eugen
 * 
 */
public class Context {
	public final Genson genson;
	private List<Class<? extends BeanView<?>>> views;
	private Map<String, Object> _ctxData = new HashMap<String, Object>();

	public Context(Genson genson) {
		this(genson, null);
	}

	public Context(Genson genson, List<Class<? extends BeanView<?>>> views) {
		this.genson = genson;
		this.views = views;
	}

	public boolean hasViews() {
		return views != null && !views.isEmpty();
	}

	public Context withView(Class<? extends BeanView<?>> view) {
		if (views == null) views = new ArrayList<Class<? extends BeanView<?>>>();
		views.add(view);
		return this;
	}
	
	public List<Class<? extends BeanView<?>>> views() {
		return views;
	}

	/**
	 * Puts the object o in the current context indexed by key.
	 * 
	 * @param key must be not null
	 * @param o
	 * @return the old object associated with that key or null.
	 */
	public Object store(String key, Object o) {
		if (key == null)
			throw new IllegalArgumentException();
		Object old = _ctxData.get(key);
		_ctxData.put(key, o);
		return old;
	}

	/**
	 * Gets the object associated with that key. There is no guarantee that this object is of type
	 * T, however we allow that so the code of the user can be more fluent and freed from a lot of
	 * casts.
	 * 
	 * @param key must be not null
	 * @return the object associated with key or null
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		if (key == null)
			throw new IllegalArgumentException();
		return (T) _ctxData.get(key);
	}

	/**
	 * Same as get, but in addition removes the object with the associated key.
	 * 
	 * @param key must be not null
	 * @return
	 * @see #get(String)
	 */
	@SuppressWarnings("unchecked")
	public <T> T remove(String key) {
		if (key == null)
			throw new IllegalArgumentException();
		return (T) _ctxData.remove(key);
	}
}
