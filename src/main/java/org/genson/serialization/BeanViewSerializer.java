package org.genson.serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.genson.BeanView;
import org.genson.Context;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptor;
import org.genson.reflect.BeanViewDescriptorProvider;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectWriter;


public class BeanViewSerializer extends ChainedSerializer {
	private final BeanViewDescriptorProvider provider;
	
	public BeanViewSerializer(BeanViewDescriptorProvider provider) {
		this(provider, null);
	}
	
	public BeanViewSerializer(BeanViewDescriptorProvider provider, Serializer<Object> next) {
		super(next);
		this.provider = provider;
	}
	
	@Override
	protected boolean handleSerialization(Object target, Type type,
			ObjectWriter writer, Context ctx) throws TransformationException, IOException {
		boolean handled = false;
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
				writer.beginObject();
				if (ctx.genson.isWithClassMetadata())
					writer.metadata("class", ctx.genson.aliasFor(target.getClass()));
				BeanDescriptor descriptor = provider.provideBeanDescriptor(viewClass);
				descriptor.serialize(target, type, writer, ctx);
    			handled = true;
    			writer.endObject();
			}
		}
		
		return handled;
	}
}
