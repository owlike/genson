package org.genson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.genson.reflect.TypeUtil;


public abstract class ObjectProvider<T> {
	private List<T> _objects;
	private Map<Type, T> _objectsCache = new HashMap<Type, T>();

	private List<Factory<? extends T>> _objectFactories;
	
	private T _defaultObject;
	private final Class<?> _tClass;
	
	public ObjectProvider(Class<?> tClass, List<T> objects,
			List<Factory<? extends T>> objectFactories,
			T dynamicObject) {
		this._tClass = tClass;
		this._objects = objects;
		this._objectFactories = objectFactories;
		this._defaultObject = dynamicObject;
	}
	
	public T findOrCreate(Type forType) {
		if (forType instanceof Class<?>) forType = TypeUtil.wrap((Class<?>) forType);
		
		T object = _objectsCache.get(forType);
		
		if ( object == null ) {			
    		for ( T s : _objects ) {
    			if ( TypeUtil.lookupWithGenerics(_tClass, forType, s.getClass(), false) != null ) {
    				_objectsCache.put(forType, s);
    				return s;
    			}
    		}
    		
    		Type searchForType = TypeUtil.createParameterizedType(_tClass, _tClass.getDeclaringClass(), forType);
			for ( Iterator<Factory<? extends T>> it = _objectFactories.iterator(); it.hasNext(); ) {
				Factory<? extends T> factory = it.next();
				if ( TypeUtil.lookupWithGenerics(Factory.class, searchForType, factory.getClass(), false) != null 
						&& (object = factory.create(forType)) != null ) {
					_objectsCache.put(forType, object);
    				return object;
				}
			}
    		
			object = _defaultObject;
			_objectsCache.put(forType, object);
		}
		
		return object;
	}
}
