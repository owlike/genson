package org.likeit.transformation.serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.likeit.transformation.Context;
import org.likeit.transformation.TransformationException;
import org.likeit.transformation.internal.DataAccessor;
import org.likeit.transformation.internal.IntrospectionUtils;
import org.likeit.transformation.stream.ObjectWriter;

public class BeanSerializer extends ChainedSerializer {
	private Map<Class<?>, List<DataAccessor>> descriptors = new ConcurrentHashMap<Class<?>, List<DataAccessor>>();

	public BeanSerializer() {
		super();
	}
	
	public BeanSerializer(Serializer<Object> next) {
		super(next);
	}
	
	@Override
	protected boolean handleSerialization(Object target, Type type,
			ObjectWriter writer, Context ctx) throws TransformationException, IOException {
		boolean handled = true;
		
		List<DataAccessor> dataAccessors = descriptors.get(type);
		if ( dataAccessors == null ) {
			Class<?> rawType = IntrospectionUtils.getRawType(type);
			dataAccessors = IntrospectionUtils.introspectBeanAccessors(rawType, null);
			descriptors.put(rawType, dataAccessors);
		}
		
		if ( dataAccessors.size() > 0 ) {
			writer.beginObject();
			for ( DataAccessor daa : dataAccessors ) {
				writer.name(daa.getName());
				ctx.serialize(daa.access(target), daa.getReturnType(), writer);
			}
			writer.endObject();
		} else handled = false;
		
		return handled;
	}
}
