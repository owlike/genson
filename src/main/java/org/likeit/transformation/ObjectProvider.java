package org.likeit.transformation;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.likeit.transformation.internal.IntrospectionUtils;

public abstract class ObjectProvider<T, F extends Factory<? extends T>> {
	private List<T> _objects;
	private Map<Type, T> _objectsCache = new ConcurrentHashMap<Type, T>();

	private List<F> _objectFactories;
	
	private T _dynamicObject;
	private final Class<? super T> _tClass;
	
	public ObjectProvider(Class<? super T> tClass, List<T> objects,
			List<F> objectFactories,
			T dynamicObject) {
		this._tClass = tClass;
		this._objects = objects;
		this._objectFactories = objectFactories;
		this._dynamicObject = dynamicObject;
	}
	
	public <R> T resolveObject(Type forType) throws TransformationException {
		T object = _objectsCache.get(forType);
		
		if ( object == null ) {
			for ( Iterator<F> it = _objectFactories.iterator(); it.hasNext(); ) {
				if ( (object = it.next().create(forType)) != null ) {
					_objectsCache.put(forType, object);
    				return (T) object;
				}
			}
			
    		for ( T s : _objects ) {
    			
    			if ( IntrospectionUtils.lookupInterfaceWithGenerics(_tClass, forType, s.getClass(), false) != null ) {
    				_objectsCache.put(forType, s);
    	    		
    				return (T) s;
    			}
    		}
    		
    		if ( object == null ) {
    			object = _dynamicObject;
    			_objectsCache.put(forType, object);
    		}
		} 
		
		return (T) object;
	}
}
