package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.genson.BeanView;
import org.genson.Context;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.reflect.BeanDescriptor;
import org.genson.reflect.BeanViewDescriptorProvider;
import org.genson.reflect.ChainedFactory;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

public class BeanViewConverter<T> implements Converter<T> {
	
	public static class BeanViewConverterFactory extends ChainedFactory {
		private final BeanViewDescriptorProvider provider;
		
		public BeanViewConverterFactory(BeanViewDescriptorProvider provider) {
			this.provider = provider;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Converter<?> create(Type type, Genson genson) {
			return new BeanViewConverter(provider, next().create(type, genson));
		}
	}
	
	private final BeanViewDescriptorProvider provider;
	private final Converter<T> next;

	public BeanViewConverter(BeanViewDescriptorProvider provider, Converter<T> next) {
		this.provider = provider;
		this.next = next;
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends BeanView<T>> findViewFor(Type type, List<Class<? extends BeanView<?>>> views) {
		for (Class<? extends BeanView<?>> v : views) {
			if (TypeUtil.lookupWithGenerics(BeanView.class, type, v, false) != null) {
				return (Class<? extends BeanView<T>>) v;
			}
		}
		return null;
	}

	@Override
	public void serialize(T obj, Type type, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		boolean handled = false;
		List<Class<? extends BeanView<?>>> views = ctx.views();
		if (views != null && views.size() > 0) {
			Class<? extends BeanView<T>> viewClass = findViewFor(type, views);
			if (viewClass != null) {
				@SuppressWarnings("unchecked")
				BeanDescriptor<T> descriptor = (BeanDescriptor<T>) provider.provideBeanDescriptor(viewClass, ctx.genson);
				descriptor.serialize(obj, type, writer, ctx);
				handled = true;
			}
		}
		if (!handled) next.serialize(obj, type, writer, ctx);
	}

	@Override
	public T deserialize(Type type, ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		List<Class<? extends BeanView<?>>> views = ctx.views();
		if (views != null && views.size() > 0) {
			Class<? extends BeanView<T>> viewClass = findViewFor(type, views);
			if (viewClass != null) {
				@SuppressWarnings("unchecked")
				BeanDescriptor<T> descriptor = (BeanDescriptor<T>) provider.provideBeanDescriptor(viewClass, ctx.genson);
				return descriptor.deserialize(type, reader, ctx);
			}
		}
		return next.deserialize(type, reader, ctx);
	}
}
