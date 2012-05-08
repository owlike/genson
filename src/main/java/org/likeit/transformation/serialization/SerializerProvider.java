package org.likeit.transformation.serialization;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.likeit.transformation.TransformationException;
import org.likeit.transformation.internal.IntrospectionUtils;

public class SerializerProvider {
	private final List<Serializer<?>> _serializers;
	private final Map<Type, Serializer<?>> _serializersCache = new ConcurrentHashMap<Type, Serializer<?>>();

	private final List<SerializerFactory<? extends Serializer<?>>> _serializersFactories;
	
	private final Serializer<?> _dynamicSerializer;
	
	
	public SerializerProvider(List<Serializer<?>> serializers,
			List<SerializerFactory<? extends Serializer<?>>> serializersFactories,
			Serializer<?> dynamicSerializer) {
		_serializers = serializers;
		_serializersFactories = serializersFactories;
		_dynamicSerializer = dynamicSerializer;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Serializer<T> resolveSerializer(Type forType) throws TransformationException {
		Serializer<?> serializer = _serializersCache.get(forType);
		
		if ( serializer == null ) {
			for ( Iterator<SerializerFactory<? extends Serializer<?>>> it = _serializersFactories.iterator(); it.hasNext(); ) {
				if ( (serializer = it.next().create(forType)) != null ) {
    				_serializersCache.put(forType, serializer);
    				return (Serializer<T>) serializer;
				}
			}
			
    		for ( Serializer<?> s : _serializers ) {
    			
    			if ( IntrospectionUtils.lookupInterfaceWithGenerics(Serializer.class, forType, s.getClass(), false) != null ) {
    				_serializersCache.put(forType, s);
    	    		
    				return (Serializer<T>) s;
    			}
    		}
    		
    		if ( serializer == null ) {
    			serializer = _dynamicSerializer;
    			_serializersCache.put(forType, _dynamicSerializer);
    		}
		} 
		
		if ( serializer != null ) return (Serializer<T>) serializer;
		
		throw new TransformationException("No serializer found for type " + forType);
	}
}
