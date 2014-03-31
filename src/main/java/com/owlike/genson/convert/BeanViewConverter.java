package com.owlike.genson.convert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.owlike.genson.BeanView;
import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.TransformationException;
import com.owlike.genson.Wrapper;
import com.owlike.genson.annotation.WithoutBeanView;
import com.owlike.genson.reflect.BeanDescriptor;
import com.owlike.genson.reflect.BeanViewDescriptorProvider;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

/**
 * Converter responsible of applying the BeanView mechanism.
 * 
 * @see com.owlike.genson.reflect.BeanViewDescriptorProvider BeanViewDescriptorProvider
 * @see com.owlike.genson.BeanView BeanView
 * 
 * @author eugen
 * 
 * @param <T>
 *            type of objects this BeanViewConverter can handle.
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
				// TODO as we link an instance to a type, we may optimize things, but for the moment it is okay
				// lets see if this feature is used
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
			Type searchedType = TypeUtil.lookupGenericType(BeanView.class, v);
			searchedType = TypeUtil.expandType(searchedType, v);
			searchedType = TypeUtil.typeOf(0, searchedType);
			if (TypeUtil.match(type, searchedType, false)) {
				return (Class<? extends BeanView<T>>) v;
			}
		}
		return null;
	}

	public void serialize(T obj, ObjectWriter writer, Context ctx) throws IOException {
		boolean handled = false;
		List<Class<? extends BeanView<?>>> views = ctx.views();
		if (views != null && views.size() > 0) {
			Class<? extends BeanView<T>> viewClass = findViewFor(type, views);
			if (viewClass != null) {
				Type viewForType = TypeUtil.expandType(BeanView.class.getTypeParameters()[0],
						viewClass);
				@SuppressWarnings("unchecked")
				Class<T> viewForClass = (Class<T>) TypeUtil.getRawClass(viewForType);
				BeanDescriptor<T> descriptor = provider
						.provide(viewForClass, viewClass, ctx.genson);
				descriptor.serialize(obj, writer, ctx);
				handled = true;
			}
		}
		if (!handled) wrapped.serialize(obj, writer, ctx);
	}

	public T deserialize(ObjectReader reader, Context ctx) throws IOException {
		if (ctx.hasViews()) {
			Class<? extends BeanView<T>> viewClass = findViewFor(type, ctx.views());
			if (viewClass != null) {
				Type viewForType = TypeUtil.expandType(BeanView.class.getTypeParameters()[0],
						viewClass);
				@SuppressWarnings("unchecked")
				Class<T> viewForClass = (Class<T>) TypeUtil.getRawClass(viewForType);
				BeanDescriptor<T> descriptor = provider
						.provide(viewForClass, viewClass, ctx.genson);
				return descriptor.deserialize(reader, ctx);
			}
		}
		return wrapped.deserialize(reader, ctx);
	}
}
