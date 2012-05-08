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

/**
 * Serializer dynamique, d'ou le chainage pour pouvoir deleguer 
 */
public class BeanViewSerializer extends ChainedSerializer {
	private Map<Type, BeanView<?>> views = new ConcurrentHashMap<Type, BeanView<?>>();
	private Map<Class<?>, List<DataAccessor>> descriptors = new ConcurrentHashMap<Class<?>, List<DataAccessor>>();

	public BeanViewSerializer() {
		
	}
	
	public BeanViewSerializer(Serializer<Object> next) {
		super(next);
	}
	
	@Override
	protected boolean handleSerialization(Object target, Type type,
			ObjectWriter writer, Context ctx) throws TransformationException, IOException {
		boolean handled = false;
		
		if ( ctx.views().size() > 0 ) {
			BeanView<?> view = findViewFor(type, ctx.views());
			
			if ( view != null ) {
				
				final Object params[] = new Object[]{target};
				
				List<DataAccessor> dataAccessors = descriptors.get(view);
				if ( dataAccessors == null ) {
					dataAccessors = IntrospectionUtils.introspectBeanAccessors(view.getClass(), params);
					descriptors.put(view.getClass(), dataAccessors);
				}
				
				if ( dataAccessors.size() > 0 ) {
    				writer.beginObject();
    				for ( DataAccessor daa : dataAccessors ) {
    					writer.name(daa.getName());
    					ctx.serialize(daa.access(view, params), daa.getReturnType(), writer);
    				}
    				writer.endObject();
    				handled = true;
				}
			}
		}
		
		return handled;
	}
	
	private BeanView<?> findViewFor(Type forType, List<Class<? extends BeanView<?>>> viewClasses) throws TransformationException {
		BeanView<?> view = views.get(forType);
		
		if ( view == null ) {
    		for ( Class<? extends BeanView<?>> v : viewClasses ) {
    			if ( IntrospectionUtils.lookupInterfaceWithGenerics(BeanView.class, forType, v, false) != null ) {
    	    		try {
    					view = v.newInstance();
    					views.put(forType, view);
    				} catch (InstantiationException e) {
    					throw new TransformationException("Can not instanciate view " + v.getName(), e);
    				} catch (IllegalAccessException e) {
    					throw new TransformationException("Can not instanciate view " + v.getName(), e);
    				}
    	    		views.put(forType, view);
    				return view;
    			}
    		}
		}
		
		return view;
	}
}
