package org.likeit.transformation.deserialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.internal.DataAccessor;
import org.likeit.transformation.internal.IntrospectionUtils;
import org.likeit.transformation.stream.ObjectReader;

public class BeanDeserializer extends ChainedDeserializer {
	// map type par nom par accessor
	private Map<Class<?>, Map<String, DataAccessor>> descriptors = new ConcurrentHashMap<Class<?>, Map<String, DataAccessor>>();
	
	public BeanDeserializer() {
		super();
	}

	public BeanDeserializer(Deserializer<Object> next) {
		super(next);
	}

	@Override
	protected Object handleDeserialization(Type type, ObjectReader reader, Context ctx) throws TransformationException,
			IOException {
		
		Object value = null;
		Class<?> rawType = IntrospectionUtils.getRawType(type);
		
		Map<String, DataAccessor> setters = descriptors.get(type);
		if ( setters == null ) {
			List<DataAccessor> accessors = IntrospectionUtils.introspectBeanModifiers(rawType);
			setters = new HashMap<String, DataAccessor>(accessors.size());
			for ( DataAccessor acc : accessors )
				setters.put(acc.getName(), acc);
			descriptors.put(rawType, setters);
		}
		
		if ( !setters.isEmpty() ) {
		
			reader.beginObject();
			
			try {
				value = rawType.newInstance();
				
				for ( ; reader.hasNext(); reader.next() ) {
					String propName = reader.name();
					DataAccessor da = setters.get(propName);
					Object param = ctx.deserialize(da.getParameterType(), reader);
					da.access(value, param);
				}
				
			} catch (InstantiationException e) {
				throw new TransformationException("Could not instantiate type " + rawType, e);
			} catch (IllegalAccessException e) {
				throw new TransformationException("Could not instantiate type " + rawType, e);
			} finally {
				reader.endObject();
			}
		}
		
		return value;
	}

}
