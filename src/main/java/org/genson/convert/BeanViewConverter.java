package org.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.genson.BeanView;
import org.genson.Context;
import org.genson.Converter;
import org.genson.Wrapper;
import org.genson.Genson;
import org.genson.TransformationException;
import org.genson.annotation.WithoutBeanView;
import org.genson.reflect.BeanDescriptor;
import org.genson.reflect.BeanViewDescriptorProvider;
import org.genson.reflect.TypeUtil;
import org.genson.stream.ObjectReader;
import org.genson.stream.ObjectWriter;

/**
 * Converter responsible of applying the BeanView mechanism.
 * 
 * @see org.genson.reflect.BeanViewDescriptorProvider BeanViewDescriptorProvider
 * @see org.genson.BeanView BeanView
 * 
 * @author eugen
 * 
 * @param <T> type of objects this BeanViewConverter can handle.
 */
public class BeanViewConverter<T> extends Wrapper<Converter<T>> implements Converter<T> {

	public static class BeanViewConverterFactory extends ChainedFactory {
		private final BeanViewDescriptorProvider provider;

		public BeanViewConverterFactory(BeanViewDescriptorProvider provider) {
			this.provider = provider;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
			if (!Wrapper.toAnnotatedElement(nextConverter).isAnnotationPresent(
					WithoutBeanView.class))
				return new BeanViewConverter(type, provider, nextConverter);
			return nextConverter;
		}
	}

	private final BeanViewDescriptorProvider provider;
	private final Type type;

	public BeanViewConverter(Type type, BeanViewDescriptorProvider provider, Converter<T> next) {
		super(next);
		this.provider = provider;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends BeanView<T>> findViewFor(Type type,
			List<Class<? extends BeanView<?>>> views) {
		for (Class<? extends BeanView<?>> v : views) {
			if (TypeUtil.lookupWithGenerics(BeanView.class, type, v, false) != null) {
				return (Class<? extends BeanView<T>>) v;
			}
		}
		return null;
	}

	@Override
	public void serialize(T obj, ObjectWriter writer, Context ctx)
			throws TransformationException, IOException {
		boolean handled = false;
		List<Class<? extends BeanView<?>>> views = ctx.views();
		if (views != null && views.size() > 0) {
			Class<? extends BeanView<T>> viewClass = findViewFor(type, views);
			if (viewClass != null) {
				@SuppressWarnings("unchecked")
				BeanDescriptor<T> descriptor = (BeanDescriptor<T>) provider.provideBeanDescriptor(
						viewClass, ctx.genson);
				descriptor.serialize(obj, writer, ctx);
				handled = true;
			}
		}
		if (!handled)
			wrapped.serialize(obj, writer, ctx);
	}

	@Override
	public T deserialize(ObjectReader reader, Context ctx)
			throws TransformationException, IOException {
		if (ctx.hasViews()) {
			Class<? extends BeanView<T>> viewClass = findViewFor(type, ctx.views());
			if (viewClass != null) {
				@SuppressWarnings("unchecked")
				BeanDescriptor<T> descriptor = (BeanDescriptor<T>) provider.provideBeanDescriptor(
						viewClass, ctx.genson);
				return descriptor.deserialize(reader, ctx);
			}
		}
		return wrapped.deserialize(reader, ctx);
	}
}
