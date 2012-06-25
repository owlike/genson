package org.genson.deserialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.genson.BeanView;
import org.genson.Context;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptor;
import org.genson.reflect.BeanViewDescriptorProvider;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;


public class BeanViewDeserializer extends ChainedDeserializer {
	private final BeanViewDescriptorProvider provider;
	
	public BeanViewDeserializer(BeanViewDescriptorProvider provider) {
		this(provider, null);
	}
	
	public BeanViewDeserializer(BeanViewDescriptorProvider provider, Deserializer<Object> next) {
		super(next);
		this.provider = provider;
	}
	
	@Override
	protected Object handleDeserialization(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		Object bean = NULL;
		List<Class<? extends BeanView<?>>> views = ctx.views();
		if ( views != null && views.size() > 0 ) {
			Class<? extends BeanView<?>> viewClass = null;
			for ( Class<? extends BeanView<?>> v : views ) {
    			if ( TypeUtil.lookupWithGenerics(BeanView.class, type, v, false) != null ) {
    				viewClass = v;
    				break;
    			}
			}
			
			if ( viewClass != null ) {
				BeanDescriptor descriptor = provider.provideBeanDescriptor(viewClass);
				bean = descriptor.deserialize(type, reader, ctx);
			}
		}
		return bean;
	}

}
